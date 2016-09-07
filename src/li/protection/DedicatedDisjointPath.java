package li.protection;

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
import mulavito.algorithms.shortestpath.disjoint.SuurballeTarjan;
import probabilityBandwidth.AbstractProbaLinkMapping;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;

public class DedicatedDisjointPath extends AbstractProbaLinkMapping {

	protected DedicatedDisjointPath(SubstrateNetwork sNet) {
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
			List<List<SubstrateLink>> disjointShortest = computeShortestPath(sNet,sn1,sn2,vl);
			if(!disjointShortest.isEmpty()){
				List<SubstrateLink> primary = disjointShortest.get(0);
				List<SubstrateLink> backup = disjointShortest.get(1);
				
				System.out.println(vl);
				System.out.println("primary: "+primary);
				System.out.println("backup: "+backup);
				for(SubstrateLink sl : primary)
					usedLinksForProba.add(sl);
				//TODO
				result.put(vl, primary);
				if(!NodeLinkAssignation.vlmSimple(vl, primary))
					throw new AssertionError("But we checked before!");
				if(!NodeLinkAssignation.backup(vl, backup, false))
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

	
	private List<List<SubstrateLink>> computeShortestPath(SubstrateNetwork sn, SubstrateNode substrateNode,
			SubstrateNode substrateNode2, VirtualLink vl) {
		//block the links without enough available capacities
		EdgePredicateFilter<SubstrateNode,SubstrateLink> filter = new EdgePredicateFilter<SubstrateNode,SubstrateLink>(
				new Predicate<SubstrateLink>() {
					@Override
					public boolean evaluate(SubstrateLink sl) {
				
						BandwidthResource bdsrc = null;
						for(AbstractResource asrc : sl)
							if(asrc instanceof BandwidthResource){
								bdsrc = (BandwidthResource) asrc;
								break;
							}
						BandwidthDemand bwd = null;
						for(AbstractDemand abd : vl){
							if(abd instanceof BandwidthDemand){
								bwd = (BandwidthDemand)abd;
								break;
							}
						}
						if(bdsrc.getAvailableBandwidth()< bwd.getDemandedBandwidth())
							return false;
						return true;
					}
				});
		Graph<SubstrateNode, SubstrateLink> tmp = filter.transform(sn);
		
		Transformer<SubstrateLink, Double> weight = new Transformer<SubstrateLink,Double>(){
			public Double transform(SubstrateLink link){
				BandwidthResource bdsrc = null;
				for(AbstractResource asrc : link)
					if(asrc instanceof BandwidthResource){
						bdsrc = (BandwidthResource) asrc;
						break;
					}
				return 1/(bdsrc.getAvailableBandwidth()+0.0001);
			}
		};
		
		SuurballeTarjan<SubstrateNode, SubstrateLink> dijkstra = new SuurballeTarjan(tmp,weight);	//dijkstra weight=1
		return dijkstra.getDisjointPaths(substrateNode, substrateNode2);
	}
}
