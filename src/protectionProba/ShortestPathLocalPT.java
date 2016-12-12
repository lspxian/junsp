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
import li.gt_itm.DrawGraph;
import vnreal.algorithms.AbstractLinkMapping;
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

public class ShortestPathLocalPT extends AbstractLinkMapping {

	boolean share;
	public ShortestPathLocalPT(SubstrateNetwork sNet, boolean share) {
		super(sNet);
		this.share=share;
	}
	
	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		//backupLinks are used to calculate the probability , for each substrate link, all of the protection links
		//(no duplicate) are presented by set. 
		Map<SubstrateLink,Set<SubstrateLink>> backupLinks=new HashMap<SubstrateLink,Set<SubstrateLink>>();
		Map<VirtualLink,List<SubstrateLink>> resultP = new HashMap<VirtualLink,List<SubstrateLink>>();
		Map<VirtualLink,List<SubstrateLink>> resultB = new HashMap<VirtualLink,List<SubstrateLink>>();
		for(VirtualLink vl: vNet.getEdges()){
			//save temporary allocated backup path to free in case of no backup
			List<SubstrateLink> tmpBackup = new ArrayList<SubstrateLink>();
			SubstrateNode sn1 = nodeMapping.get(vNet.getEndpoints(vl).getFirst());
			SubstrateNode sn2 = nodeMapping.get(vNet.getEndpoints(vl).getSecond());
			List<SubstrateLink> primary = computeShortestPath(sNet,sn1,sn2,vl);
			
			System.out.println(vl+" "+primary);
			if(!primary.isEmpty()){	//primary path
				resultP.put(vl, primary);
				if(!NodeLinkAssignation.vlmSimple(vl, primary))
					throw new AssertionError("But we checked before!");
				for(SubstrateLink sl: primary){	//calculate local backup path for each link
					List<SubstrateLink> backup = this.ComputeLocalBackupPath(sNet, sl, vl,share);
					System.out.println(sl+" "+backup);
					if(!backup.isEmpty()){	
						tmpBackup.addAll(backup);
						if(!NodeLinkAssignation.backup(vl,sl, backup, share))
							throw new AssertionError("But we checked before!");
						
					}
					else{
						System.out.println("no backup link");
						NodeLinkDeletion.linkFreeBackup(vl.getBandwidthDemand(), tmpBackup, share);	//free temporary backup
						for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){	//free node mapping
							NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
						}
						for(Map.Entry<VirtualLink, List<SubstrateLink>> entry: resultP.entrySet()){	// free primary path
							NodeLinkDeletion.linkFree(entry.getKey(), entry.getValue());
						}
						for(Map.Entry<VirtualLink, List<SubstrateLink>> entry: resultB.entrySet()){	//free backup path of other virtual links
							NodeLinkDeletion.linkFreeBackup(entry.getKey().getBandwidthDemand(), entry.getValue(),share);
						}
						return false;
					}
					
					if(backupLinks.containsKey(sl)){	//if the primary link is in the list, add just the the protection link
						Set<SubstrateLink> tmpSet = backupLinks.get(sl);
						for(SubstrateLink slink : backup)
							tmpSet.add(slink);
					}
					else{	//if not, add a new map element for the primary link
						Set<SubstrateLink> tmpSet=new HashSet<SubstrateLink>(backup);
						backupLinks.put(sl, tmpSet);
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
					NodeLinkDeletion.linkFreeBackup(entry.getKey().getBandwidthDemand(), entry.getValue(),share);
				}
				return false;
			}
			
			resultB.put(vl, tmpBackup);
			
		}
		//probability computation
		double temproba=1;
		for(Map.Entry<SubstrateLink, Set<SubstrateLink>> entry : backupLinks.entrySet()){
			
			double backupProba=1;
			for(SubstrateLink slink:entry.getValue()){
				backupProba=backupProba*(1-slink.getProbability());
			}
			temproba = temproba * (1-entry.getKey().getProbability()*(1-backupProba));
		}
		this.probability=1-temproba;
		
		return true;
	}
	
	//primary path
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
		
		DijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode, SubstrateLink>(tmp,weight);
		return dijkstra.getPath(substrateNode, substrateNode2);
	}
	
	private List<SubstrateLink> ComputeLocalBackupPath(SubstrateNetwork sn, SubstrateLink sl, VirtualLink vl, boolean share){
		SubstrateNode node1 = sn.getEndpoints(sl).getFirst();
		SubstrateNode node2 = sn.getEndpoints(sl).getSecond();
		BandwidthDemand bwd = vl.getBandwidthDemand();
		
		//block the links without enough available capacities
		//calculate additional bandwidth
		Predicate<SubstrateLink> pre=null;
		if(share){
			pre=new Predicate<SubstrateLink>(){
				@Override
				public boolean evaluate(SubstrateLink link) {
					if(link.equals(sl)) return false;
					BandwidthResource bdsrc = link.getBandwidthResource();
					for(Risk risk:bdsrc.getRisks()){
						if(risk.getNe().equals(sl)){
							double origTotal = bdsrc.maxRiskTotal();
							risk.addDemand(bwd);
							double newTotal = bdsrc.maxRiskTotal();
							risk.removeDemand(bwd);
							if((newTotal-origTotal)>bdsrc.getAvailableBandwidth())
								return false;
							else return true;
						}
					}
					double additional = bdsrc.getAvailableBandwidth()+bdsrc.getReservedBackupBw()-bwd.getDemandedBandwidth();
					if(additional<0) return false;
					else return true;
				}
			};
		}
		else{
			pre=new Predicate<SubstrateLink>() {
				@Override
				public boolean evaluate(SubstrateLink slink) {
					BandwidthResource bdsrc = slink.getBandwidthResource();
					if((bdsrc.getAvailableBandwidth()< bwd.getDemandedBandwidth())||slink.equals(sl))
						return false;
					return true;
				}
			};
		}
		
		EdgePredicateFilter<SubstrateNode,SubstrateLink> filter = new EdgePredicateFilter<SubstrateNode,SubstrateLink>(pre);
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
		
		List<SubstrateLink> result = dijkstra.getPath(node1, node2);
		//TODO
	/*	if(result.isEmpty()){
			DrawGraph dg = new DrawGraph(tmp);
			dg.draw();			
		}*/
		return result;
	}
	
}
