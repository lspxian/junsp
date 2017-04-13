package protectionProba;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LinkedMap;

import edu.uci.ics.jung.algorithms.filters.EdgePredicateFilter;
import edu.uci.ics.jung.algorithms.flows.EdmondsKarpMaxFlow;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import vnreal.algorithms.AbstractLinkMapping;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.demands.BandwidthDemand;
import vnreal.mapping.Mapping;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.resources.BandwidthResource;
/*
 * use jung maxlow api
 */
public class MaxFlowBackupVF extends AbstractLinkMapping {

	Map<SubstrateLink,Integer> maxflow=new TreeMap<SubstrateLink,Integer>();
	public MaxFlowBackupVF(SubstrateNetwork sNet) {
		super(sNet);
		calculateMaxflow(sNet);
		System.out.println(maxflow);
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		Set<SubstrateLink> noProtected=new TreeSet<SubstrateLink>();
		Map<BandwidthDemand,List<SubstrateLink>> resultB = new HashMap<BandwidthDemand,List<SubstrateLink>>();
		Map<BandwidthDemand,List<SubstrateLink>> tmpMaxflow = new HashMap<BandwidthDemand,List<SubstrateLink>>();
		for(VirtualLink vl: vNet.getEdges()){
			BandwidthDemand bwdem=vl.getBandwidthDemand();
			SubstrateNode sn1 = nodeMapping.get(vNet.getEndpoints(vl).getFirst());
			SubstrateNode sn2 = nodeMapping.get(vNet.getEndpoints(vl).getSecond());
			List<SubstrateLink> primary = new ArrayList<SubstrateLink>(
					computeShortestPath(sNet,sn1,sn2,vl));
			if(!primary.isEmpty()){
				System.out.println(vl+" "+primary);
				tmpMaxflow.put(bwdem, primary);
				for(SubstrateLink sl:primary){
					BandwidthDemand bwd=new BandwidthDemand(vl);
					bwd.setDemandedBandwidth(bwdem.getDemandedBandwidth());
					if(!NodeLinkAssignation.vlmSingleLinkSimple(bwd, sl)){
						throw new AssertionError("But we checked before!");
					}
					this.mapping.put(bwd, sl);
					int mfValue=maxflow.get(sl).intValue();
					//backup here
					List<SubstrateLink> backup = this.ComputeLocalBackupPath(sNet, sl, bwd);
					System.out.println(sl+"#"+bwd+" "+backup);
					if(!backup.isEmpty()){	
						maxflow.put(sl, mfValue-bwd.getDemandedBandwidth().intValue());	//update maxflow
						resultB.put(bwd, backup);
						if(!NodeLinkAssignation.backup(vl,sl, backup, true))
							throw new AssertionError("But we checked before!");
						sl.getBandwidthResource().getMapping(bwd).setProtection(true);
					}
					else{
						System.out.println("no backup link "+sl);
						noProtected.add(sl);
						sl.getBandwidthResource().getMapping(bwd).setProtection(false);
						/*
						for(Map.Entry<BandwidthDemand, List<SubstrateLink>> mf: tmpMaxflow.entrySet()){	
							freeMaxflow(mf.getKey(), mf.getValue());
						}
						for(Map.Entry<BandwidthDemand, List<SubstrateLink>> ent: resultB.entrySet()){	//free backup path of other virtual links
							NodeLinkDeletion.linkFreeBackup(ent.getKey(), ent.getValue(),true);
						}
						NodeLinkDeletion.freeResource(vNet, sNet);	//free primary
						return false;*/
					}
					
				}
			}
			else{
				for(Map.Entry<BandwidthDemand, List<SubstrateLink>> mf: tmpMaxflow.entrySet()){	
					freeMaxFlow(mf.getKey(), mf.getValue());
				}
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
	
	private List<SubstrateLink> computeShortestPath(SubstrateNetwork sn, SubstrateNode substrateNode,
			SubstrateNode substrateNode2, VirtualLink vl) {
		BandwidthDemand bwd = vl.getBandwidthDemand();		
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
				double cost=100/(bdsrc.getAvailableBandwidth()+0.0001);
				//maxflow backup verification
				if(maxflow.get(link)<bwd.getDemandedBandwidth()){
					double logp=-Math.log(1-link.getProbability())*1000000;
					cost=cost+logp;
				}
				return cost;
			}
		};
		
		DijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode, SubstrateLink>(tmp,weight);	//dijkstra
		return dijkstra.getPath(substrateNode, substrateNode2);
	}
	
	private List<SubstrateLink> ComputeLocalBackupPath(SubstrateNetwork sn, SubstrateLink sl, BandwidthDemand bwd) {
		SubstrateNode node1 = sn.getEndpoints(sl).getFirst();
		SubstrateNode node2 = sn.getEndpoints(sl).getSecond();
		
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
		DijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode, SubstrateLink>(tmp);	//separation: no link cost
		return dijkstra.getPath(node1, node2);
	}

	public void calculateMaxflow(SubstrateNetwork sNet){
		
		for(SubstrateLink sl:sNet.getEdges()){
			SubstrateNode source=sNet.getEndpoints(sl).getFirst();
			SubstrateNode sink=sNet.getEndpoints(sl).getSecond();
			
			DirectedSparseGraph<SubstrateNode,SubstrateLink> temp=new DirectedSparseGraph<SubstrateNode,SubstrateLink>();
			for(SubstrateLink slink:sNet.getEdges()){
				if(!slink.equals(sl)){
					SubstrateLink slink2=new SubstrateLink();
					BandwidthResource bwr=slink.getBandwidthResource();
					BandwidthResource bwr2=(BandwidthResource) bwr.getCopy(slink2);
					slink2.add(bwr2);
					
					temp.addEdge(slink, new Pair<SubstrateNode>(
							sNet.getEndpoints(slink).getFirst(),
							sNet.getEndpoints(slink).getSecond()), EdgeType.DIRECTED);
					temp.addEdge(slink2, new Pair<SubstrateNode>(
							sNet.getEndpoints(slink).getSecond(),
							sNet.getEndpoints(slink).getFirst()), EdgeType.DIRECTED);
				}
			}
			Map<SubstrateLink,Number> edgeFlowMap=new LinkedMap<SubstrateLink,Number>();
			EdmondsKarpMaxFlow<SubstrateNode,SubstrateLink> mf= new EdmondsKarpMaxFlow<SubstrateNode,SubstrateLink>(temp,source,sink,
					new Transformer<SubstrateLink, Number>(){
						@Override
						public Number transform(SubstrateLink arg0) {
							return arg0.getBandwidthResource().getBackupCap();
						}
				
			},edgeFlowMap,new Factory<SubstrateLink>(){
				@Override
				public SubstrateLink create() {
					return new SubstrateLink();	//??? bandwidth
				}
				
			});
			mf.evaluate();
			this.maxflow.put(sl, mf.getMaxFlow());
		}
	}
	
	public void freeMaxFlow(VirtualNetwork vn, SubstrateNetwork sn){
		for(VirtualLink vl:vn.getEdges()){
			freeMaxFlow(vl.getBandwidthDemand(),sn.getEdges());
		}
	}
	
	public void freeMaxFlow(BandwidthDemand bwd, Collection<SubstrateLink> list){
		for(SubstrateLink sl:list){
			for(Mapping m:sl.getBandwidthResource().getMappings())
				if(m.getDemand().getOwner().equals(bwd.getOwner())&&m.isProtection()){
					int newMaxflow=this.maxflow.get(sl)+bwd.getDemandedBandwidth().intValue();
					this.maxflow.put(sl, newMaxflow);
				}
		}
	}

	public Map<SubstrateLink, Integer> getMaxflow() {
		return maxflow;
	}
	
}
