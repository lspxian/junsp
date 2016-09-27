package protectionProba;

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

public class ShortestPathLocalPT extends AbstractProbaLinkMapping {

	boolean share;
	public ShortestPathLocalPT(SubstrateNetwork sNet, boolean share) {
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
			List<SubstrateLink> tmpBackup = new ArrayList<SubstrateLink>();
			SubstrateNode sn1 = nodeMapping.get(vNet.getEndpoints(vl).getFirst());
			SubstrateNode sn2 = nodeMapping.get(vNet.getEndpoints(vl).getSecond());
			List<SubstrateLink> primary = computeShortestPath(sNet,sn1,sn2,vl);
			
			System.out.println(vl+" "+primary);
			if(!primary.isEmpty()){
				resultP.put(vl, primary);
				if(!NodeLinkAssignation.vlmSimple(vl, primary))
					throw new AssertionError("But we checked before!");
				for(SubstrateLink sl: primary){
					List<SubstrateLink> backup = this.ComputeLocalBackupPath(sNet, sl, vl,share);
					System.out.println(sl+" "+backup);
					usedLinksForProba.add(sl);
					if(!backup.isEmpty()){
						tmpBackup.addAll(backup);
						List<SubstrateLink> tmpsl = new ArrayList<SubstrateLink>();
						tmpsl.add(sl);
						if(!NodeLinkAssignation.backup(vl,tmpsl, backup, share))
							throw new AssertionError("But we checked before!");
					}
					else{
						System.out.println("no backup link");
						NodeLinkDeletion.linkFreeBackup(vl, tmpBackup, share);
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
			}
			else{
				System.out.println("no primary link");
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
			
			resultB.put(vl, tmpBackup);
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
	
	private List<SubstrateLink> ComputeLocalBackupPath(SubstrateNetwork sn, SubstrateLink sl, VirtualLink vl, boolean share){
		SubstrateNode node1 = sn.getEndpoints(sl).getFirst();
		SubstrateNode node2 = sn.getEndpoints(sl).getSecond();
		BandwidthDemand bwd = vl.getBandwidthDemand();
		
		//block the links without enough available capacities
		EdgePredicateFilter<SubstrateNode,SubstrateLink> filter = new EdgePredicateFilter<SubstrateNode,SubstrateLink>(
				new Predicate<SubstrateLink>() {
					@Override
					public boolean evaluate(SubstrateLink slink) {
						BandwidthResource bdsrc = slink.getBandwidthResource();
						if((bdsrc.getAvailableBandwidth()< bwd.getDemandedBandwidth())||slink.equals(sl))
							return false;
						return true;
					}
				});
		Graph<SubstrateNode, SubstrateLink> tmp = filter.transform(sn);
		
		Transformer<SubstrateLink, Number> weight=null;;
		if(share){	//optimize additional bw
			weight = new Transformer<SubstrateLink,Number>(){
				public Double transform(SubstrateLink link){
					BandwidthResource bdsrc = link.getBandwidthResource();
					for(Risk risk:bdsrc.getRisks()){
						if(risk.getNe().equals(sl)){
							double origTotal = bdsrc.maxRiskTotal();
							risk.addDemand(bwd);
							double newTotal = bdsrc.maxRiskTotal();
							risk.removeDemand(bwd);
							return newTotal-origTotal;
						}
					}
					//new risk
					double additional = bwd.getDemandedBandwidth()-bdsrc.getReservedBackupBw();
					if(additional<=0) return 0.0;
					else return additional;
				}
			};
		}
		else{	//optimize residual bandwidth
			weight = new Transformer<SubstrateLink,Number>(){
				public Number transform(SubstrateLink link){
					BandwidthResource bdsrc = link.getBandwidthResource();
					return 1/(bdsrc.getAvailableBandwidth()+0.0001);
				}
			};
		}
		
		DijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode, SubstrateLink>(tmp,weight);	//dijkstra
		return dijkstra.getPath(node1, node2);
	}
	
}
