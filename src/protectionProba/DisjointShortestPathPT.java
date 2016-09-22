package protectionProba;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.filters.EdgePredicateFilter;
import edu.uci.ics.jung.graph.Graph;
import mulavito.algorithms.shortestpath.disjoint.SuurballeTarjan;
import probabilityBandwidth.AbstractProbaLinkMapping;
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

public class DisjointShortestPathPT extends AbstractProbaLinkMapping {
	
	boolean share;
	public DisjointShortestPathPT(SubstrateNetwork sNet, boolean share) {
		super(sNet);
		this.share=share;
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		//TODO calculate proba
		
		double temproba=1;
		Set<SubstrateLink> usedLinksForProba=new HashSet<SubstrateLink>();
		Map<VirtualLink,List<SubstrateLink>> resultP = new HashMap<VirtualLink,List<SubstrateLink>>();
		Map<VirtualLink,List<SubstrateLink>> resultB = new HashMap<VirtualLink,List<SubstrateLink>>();
		for(VirtualLink vl: vNet.getEdges()){
			SubstrateNode sn1 = nodeMapping.get(vNet.getEndpoints(vl).getFirst());
			SubstrateNode sn2 = nodeMapping.get(vNet.getEndpoints(vl).getSecond());
			List<List<SubstrateLink>> shortest = computeShortestPath(sNet,sn1,sn2,vl);
			if(shortest.size()==2){
				List<SubstrateLink> primary = shortest.get(0);
				List<SubstrateLink> backup = shortest.get(1);
//				System.out.println(vl);
//				System.out.println(shortest);
				
				for(SubstrateLink sl : primary)
					usedLinksForProba.add(sl);
				
				resultP.put(vl, primary);
				resultB.put(vl, backup);
				if(!NodeLinkAssignation.vlmSimple(vl, primary))
					throw new AssertionError("But we checked before!");
				if(NodeLinkAssignation.backup(vl, backup, share))
					throw new AssertionError("But we checked before!");
			}
			else{
				System.out.println("no disjoint link");
				for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
					NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
				}
				for(Map.Entry<VirtualLink, List<SubstrateLink>> entry: resultP.entrySet()){
					NodeLinkDeletion.linkFree(entry.getKey(), entry.getValue());
				}
				for(Map.Entry<VirtualLink, List<SubstrateLink>> entry: resultB.entrySet()){
					NodeLinkDeletion.linkFreeBackup(entry.getKey(), entry.getValue(),share);
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
						BandwidthResource bdsrc = sl.getBandwidthResource();
						BandwidthDemand bwd = vl.getBandwidthDemand();
						if(bdsrc.getAvailableBandwidth()< bwd.getDemandedBandwidth())
							return false;
						return true;
					}
				});
		Graph<SubstrateNode, SubstrateLink> tmp = filter.transform(sn);
		
		Transformer<SubstrateLink, Number> weight = new Transformer<SubstrateLink,Number>(){
			public Number transform(SubstrateLink link){
				BandwidthResource bdsrc = link.getBandwidthResource();
				return 1/(bdsrc.getAvailableBandwidth()+0.0001);
			}
			
			public String toString(){
				return "transformer substrate link";
			}
		};
		
		SuurballeTarjan<SubstrateNode, SubstrateLink> suur = new SuurballeTarjan<SubstrateNode, SubstrateLink>(tmp, weight);
		return suur.getDisjointPaths(substrateNode, substrateNode2);
	}


}
