package protectionProba;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
import vnreal.network.virtual.VirtualNetwork;
import vnreal.resources.BandwidthResource;
/*
 * Shortest path local share backup, with admission control(bandwidth), optimize additional bandwidth,
 * if no backup resource, accept the VN and calculate probability
 */
public class CSP_Proba extends AbstractBackupMapping {

	public CSP_Proba(SubstrateNetwork sNet) {
		super(sNet);
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<BandwidthDemand, SubstrateLink> primary) {
		
		Map<SubstrateLink,TreeSet<BandwidthDemand>> priMapping=new TreeMap<SubstrateLink, TreeSet<BandwidthDemand>>(new Comparator<SubstrateLink>(){
			@Override
			public int compare(SubstrateLink o1, SubstrateLink o2) {
				if(o1.getProbability()>o2.getProbability())
					return -100;
				else if(o2.getProbability()<o1.getProbability())
					return 100;
				else return (int) (o1.getId()-o2.getId());
			}
		});
		
		for(Map.Entry<BandwidthDemand, SubstrateLink> e:primary.entrySet()){
			SubstrateLink sl=e.getValue();
			if(!priMapping.containsKey(sl))
				priMapping.put(sl, new TreeSet<BandwidthDemand>());
			priMapping.get(sl).add(e.getKey());
		}
		
		Set<SubstrateLink> noProtected=new TreeSet<SubstrateLink>();
		for(Map.Entry<SubstrateLink,TreeSet<BandwidthDemand>> entry:priMapping.entrySet()){
			SubstrateLink sl=entry.getKey();
			for(BandwidthDemand bwd:entry.getValue()){
				List<SubstrateLink> backup = this.ComputeLocalBackupPath(sNet, sl, bwd, true);
				System.out.println(sl+"#"+bwd+" "+backup);
				if(!backup.isEmpty()){
					if(!NodeLinkAssignation.backup(bwd,sl, backup, true))
						throw new AssertionError("But we checked before!");
					sl.getBandwidthResource().getMapping(bwd).setProtection(true);
				}
				else{
					System.out.println("no backup link "+sl);
					/*for(BandwidthDemand bwdtmp:entry.getValue()){
						sl.getBandwidthResource().backupFree(bwdtmp, true);
					}*/
					noProtected.add(sl);
					sl.getBandwidthResource().getMapping(bwd).setProtection(false);
//					break;
				}
			}
		}
		double tmpProba=1;
		for(SubstrateLink sl:noProtected){
			tmpProba=tmpProba*(1-sl.getProbability());
		}
		this.probability=1-tmpProba;
		
		return true;
	}
	
	private List<SubstrateLink> ComputeLocalBackupPath(SubstrateNetwork sn, SubstrateLink sl, BandwidthDemand bwd, boolean share){
		SubstrateNode node1 = sn.getEndpoints(sl).getFirst();
		SubstrateNode node2 = sn.getEndpoints(sl).getSecond();
		
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
