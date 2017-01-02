package protectionProba;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.filters.EdgePredicateFilter;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
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

public class SPWithoutBackupVF extends AbstractLinkMapping {

	public SPWithoutBackupVF(SubstrateNetwork sNet) {
		super(sNet);
	}
	
	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		Set<SubstrateLink> noProtected=new TreeSet<SubstrateLink>();
		Map<BandwidthDemand,List<SubstrateLink>> resultB = new HashMap<BandwidthDemand,List<SubstrateLink>>();
		for(VirtualLink vl: vNet.getEdges()){
			SubstrateNode sn1 = nodeMapping.get(vNet.getEndpoints(vl).getFirst());
			SubstrateNode sn2 = nodeMapping.get(vNet.getEndpoints(vl).getSecond());
			List<SubstrateLink> primary = new ArrayList<SubstrateLink>(
					computeShortestPath(sNet,sn1,sn2,vl.getBandwidthDemand()));
			if(!primary.isEmpty()){
				System.out.println(vl+" "+primary);
				for(SubstrateLink sl:primary){
					BandwidthDemand bwd=new BandwidthDemand(vl);
					bwd.setDemandedBandwidth(vl.getBandwidthDemand().getDemandedBandwidth());
					if(!NodeLinkAssignation.vlmSingleLinkSimple(bwd, sl)){
						throw new AssertionError("But we checked before!");
					}
					this.mapping.put(bwd, sl);
					//backup here
					List<SubstrateLink> backup = this.computeLocalBackupPath(sNet, sl, vl);
					System.out.println(sl+"#"+bwd+" "+backup);
					if(!backup.isEmpty()){
						resultB.put(bwd, backup);
						if(!NodeLinkAssignation.backup(vl,sl, backup, true))
							throw new AssertionError("But we checked before!");
						sl.getBandwidthResource().getMapping(bwd).setProtection(true);
					}
					else{
						System.out.println("no backup link "+sl);
//						noProtected.add(sl);
//						sl.getBandwidthResource().getMapping(bwd).setProtection(false);
						
						for(Map.Entry<BandwidthDemand, List<SubstrateLink>> ent: resultB.entrySet()){	//free backup path of other virtual links
							NodeLinkDeletion.linkFreeBackup(ent.getKey(), ent.getValue(),true);
						}
						NodeLinkDeletion.freeResource(vNet, sNet);	//free primary
						return false;
					}
					
				}
			}
			else{
				System.out.println("no primary link"+vl);
				for(Map.Entry<BandwidthDemand, List<SubstrateLink>> ent: resultB.entrySet()){	//free backup path of other virtual links
					NodeLinkDeletion.linkFreeBackup(ent.getKey(), ent.getValue(),true);
				}
				for(Map.Entry<BandwidthDemand, SubstrateLink> entry: this.mapping.entrySet()){
					entry.getKey().free(entry.getValue().getBandwidthResource());
				}
				for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
					NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
				}
				return false;
			}
		}
		double tmpProba=1;
		for(SubstrateLink sl:noProtected){
			tmpProba=tmpProba*(1-sl.getProbability());
		}
		this.probability=1-tmpProba;
		return true;
	}
	
	//primary path
	private List<SubstrateLink> computeShortestPath(SubstrateNetwork sn, SubstrateNode substrateNode,
			SubstrateNode substrateNode2, BandwidthDemand bwd ) {
		//block the links without enough available capacities
		EdgePredicateFilter<SubstrateNode,SubstrateLink> filter = new EdgePredicateFilter<SubstrateNode,SubstrateLink>(
				new Predicate<SubstrateLink>() {
					@Override
					public boolean evaluate(SubstrateLink sl) {
						BandwidthResource bdsrc = sl.getBandwidthResource();
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
	
	private List<SubstrateLink> computeLocalBackupPath(SubstrateNetwork sn, SubstrateLink sl, VirtualLink vl){
		SubstrateNode node1 = sn.getEndpoints(sl).getFirst();
		SubstrateNode node2 = sn.getEndpoints(sl).getSecond();
		BandwidthDemand bwd = vl.getBandwidthDemand();
		
		//block the links without enough available capacities
		EdgePredicateFilter<SubstrateNode,SubstrateLink> filter = new EdgePredicateFilter<SubstrateNode,SubstrateLink>(new Predicate<SubstrateLink>(){
			@Override
			public boolean evaluate(SubstrateLink link) {
				if(link.equals(sl)) return false;
				else if(link.getBandwidthResource().getBackupAvailable(sl)>=bwd.getDemandedBandwidth())
					return true;
				return false;
			}
		});
		
		Graph<SubstrateNode, SubstrateLink> tmp = filter.transform(sn);
		Transformer<SubstrateLink, Number> weight= new Transformer<SubstrateLink,Number>(){
			public Double transform(SubstrateLink link){
				BandwidthResource bdsrc = link.getBandwidthResource();
				for(Risk risk:bdsrc.getRisks()){
					if(risk.getNe().equals(sl)){
						return 1/(bdsrc.getBackupCap()-risk.getTotal());
					}
				}
				return 1/bdsrc.getBackupCap();
			}
		};
		DijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode, SubstrateLink>(tmp);	//separation: no link cost
		List<SubstrateLink> result = dijkstra.getPath(node1, node2);
		
		return result;
	}
	
}
