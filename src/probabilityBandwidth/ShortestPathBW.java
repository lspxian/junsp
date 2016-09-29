package probabilityBandwidth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.filters.EdgePredicateFilter;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.resources.BandwidthResource;

public class ShortestPathBW extends AbstractProbaLinkMapping {

	public ShortestPathBW(SubstrateNetwork sNet) {
		super(sNet);
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		double temproba=1;
		Set<SubstrateLink> usedLinksForProba=new HashSet<SubstrateLink>();
		Map<VirtualLink,List<SubstrateLink>> result = new HashMap<VirtualLink,List<SubstrateLink>>();
		for(VirtualLink vl: vNet.getEdges()){
			SubstrateNode sn1 = nodeMapping.get(vNet.getEndpoints(vl).getFirst());
			SubstrateNode sn2 = nodeMapping.get(vNet.getEndpoints(vl).getSecond());
			List<SubstrateLink> shortest = new ArrayList<SubstrateLink>(
					computeShortestPath(sNet,sn1,sn2,vl));
			if(!shortest.isEmpty()){
				System.out.println(vl);
				System.out.println(shortest);
				
				for(SubstrateLink sl : shortest)
					usedLinksForProba.add(sl);
				
				result.put(vl, shortest);
				if(!NodeLinkAssignation.vlmSimple(vl, shortest))
					throw new AssertionError("But we checked before!");
			}
			else{
				for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
					NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
				}
				for(Map.Entry<VirtualLink, List<SubstrateLink>> entry: result.entrySet()){
					NodeLinkDeletion.linkFree(entry.getKey(), entry.getValue());
				}
				return false;
			}
			
		}
		for(SubstrateLink sl : usedLinksForProba){
			temproba = temproba * (1-sl.getProbability());			
		}
		this.probability=1-temproba;
		
		return true;
	}
	
	private List<SubstrateLink> computeShortestPath(SubstrateNetwork sn, SubstrateNode substrateNode,
			SubstrateNode substrateNode2, VirtualLink vl) {
		//block the links without enough available capacities
		EdgePredicateFilter<SubstrateNode,SubstrateLink> filter = new EdgePredicateFilter<SubstrateNode,SubstrateLink>(
				new Predicate<SubstrateLink>() {
					@Override
					public boolean evaluate(SubstrateLink sl) {
						BandwidthResource bdsrc = sl.getBandwidthResource();
						BandwidthDemand bwd = vl.getBandwidthDemand();
						if(bdsrc.getAvailableBandwidth()< bwd.getDemandedBandwidth())
							return false;
						return true;
					}
				});
		Graph<SubstrateNode, SubstrateLink> tmp = filter.transform(sn);
		
		Transformer<SubstrateLink, Double> weight = new Transformer<SubstrateLink,Double>(){
			public Double transform(SubstrateLink link){
				BandwidthResource bdsrc = link.getBandwidthResource();
				return 1/(bdsrc.getAvailableBandwidth()+0.0001);
			}
		};
		
		DijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode, SubstrateLink>(tmp,weight);	//dijkstra weight=1
		return dijkstra.getPath(substrateNode, substrateNode2);
	}

}
