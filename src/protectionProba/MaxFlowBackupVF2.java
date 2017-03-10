package protectionProba;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.filters.EdgePredicateFilter;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
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
import vnreal.resources.CostResource;

public class MaxFlowBackupVF2 extends AbstractLinkMapping {
	
	Map<SubstrateLink,Double> maxflow=new TreeMap<SubstrateLink,Double>();
	public MaxFlowBackupVF2(SubstrateNetwork sNet) {
		super(sNet);
		calculateMaxflow(sNet);
		//System.out.println(maxflow);
		String result="";
		for(SubstrateLink l:sNet.getEdges()){
			Pair<SubstrateNode> pair = sNet.getEndpoints(l);
			result += l + "  (" + pair.getFirst().getId() + "<->"
					+ pair.getSecond().getId() + ") \n";
			for(MaxFlowPath mfp:l.getMaxflow()){
				result+= mfp+"  ";
			}
			result+="\n";
		}
		System.out.println(result);
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		Set<SubstrateLink> noProtected=new TreeSet<SubstrateLink>();
		Map<BandwidthDemand,List<SubstrateLink>> resultB = new HashMap<BandwidthDemand,List<SubstrateLink>>();
		Map<BandwidthDemand,List<SubstrateLink>> tmpMaxflow = new HashMap<BandwidthDemand,List<SubstrateLink>>();
		for(VirtualLink vl: vNet.getEdges()){
//			System.out.println(this.sNet.probaToString());
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
					//backup here
					boolean protectionFlag=false;
					for(MaxFlowPath mfPath:sl.getMaxflow()){
						if(mfPath.fulfil(bwd)){
							List<SubstrateLink> backup=mfPath.getPath();
							System.out.println(sl+"#"+bwd+" "+backup);
							resultB.put(bwd, backup);
							if(!NodeLinkAssignation.backup(vl,sl, backup, true))
								throw new AssertionError("But we checked before!");
							sl.getBandwidthResource().getMapping(bwd).setProtection(true);
							protectionFlag=true;
							break;
						}
					}
					
					if(!protectionFlag){
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
	
	public void freeMaxFlow(VirtualNetwork vn, SubstrateNetwork sn){
		for(VirtualLink vl:vn.getEdges()){
			freeMaxFlow(vl.getBandwidthDemand(),sn.getEdges());
		}
	}
	
	private void freeMaxFlow(BandwidthDemand bwd, Collection<SubstrateLink> link) {
		for(SubstrateLink sl:link){
			for(MaxFlowPath mfPath:sl.getMaxflow()){
				if(mfPath.free(bwd))	break;
			}
		}		
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
				boolean verification=false;
				for(MaxFlowPath mfPath:link.getMaxflow()){
					if(mfPath.residual()>bwd.getDemandedBandwidth()){
						verification=true;
						break;
					}
				}
				if(!verification){
					double logp=-Math.log(1-link.getProbability())*1000000;
					cost=cost+logp;
				}
				return cost;
			}
		};
		
		DijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode, SubstrateLink>(tmp,weight);	//dijkstra
		return dijkstra.getPath(substrateNode, substrateNode2);
	}
	
	public void calculateMaxflow(SubstrateNetwork sNet) {
		// FordFulkerson
		for(SubstrateLink sl:sNet.getEdges()){
			SubstrateNode source=sNet.getEndpoints(sl).getFirst();
			SubstrateNode sink=sNet.getEndpoints(sl).getSecond();
			//create augmented graph
			DirectedSparseGraph<SubstrateNode,SubstrateLink> augment=new DirectedSparseGraph<SubstrateNode,SubstrateLink>();
			for(SubstrateLink slink:sNet.getEdges()){
				if(!slink.equals(sl)){
					BandwidthResource bwr=slink.getBandwidthResource();

					SubstrateLink slink2=new SubstrateLink();
					CostResource cost1=new CostResource(slink2);
					cost1.setCost(bwr.getBackupCap());
					slink2.add(cost1);
					
					SubstrateLink slink3=new SubstrateLink();
					CostResource cost2=new CostResource(slink3);
					cost2.setCost(bwr.getBackupCap());
					slink3.add(cost2);
					
					augment.addEdge(slink2, new Pair<SubstrateNode>(
							sNet.getEndpoints(slink).getFirst(),
							sNet.getEndpoints(slink).getSecond()), EdgeType.DIRECTED);
					augment.addEdge(slink3, new Pair<SubstrateNode>(
							sNet.getEndpoints(slink).getSecond(),
							sNet.getEndpoints(slink).getFirst()), EdgeType.DIRECTED);
					
				}
			}
			//ford fulkerson algo
			double totalFlow=0;
			while(true){
				Predicate<SubstrateLink> pre=new Predicate<SubstrateLink>() {
					@Override
					public boolean evaluate(SubstrateLink arg0) {
						CostResource tmpCost=(CostResource)arg0.get().get(0);
						if(tmpCost.getCost()==0)
							return false;
						return true;
					}
				};
				EdgePredicateFilter<SubstrateNode,SubstrateLink> filter = new EdgePredicateFilter<SubstrateNode,SubstrateLink>(pre);
				Graph<SubstrateNode, SubstrateLink> tmp = filter.transform(augment);
				DijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode, SubstrateLink>(tmp);
				List<SubstrateLink> paths=dijkstra.getPath(source, sink);
				
				if(paths.isEmpty())	break;	//no flow, exit
				double flow=1000;
				for(SubstrateLink tmpsl:paths){
					CostResource tmpCost=(CostResource)tmpsl.get().get(0);
					if(tmpCost.getCost()<flow&&tmpCost.getCost()>0)	flow=tmpCost.getCost();
				}
				totalFlow+=flow;
				for(SubstrateLink tmpsl:paths){
					CostResource tmpCost=(CostResource)tmpsl.get().get(0);
					tmpCost.setCost(tmpCost.getCost()-flow);	//forward
					//backward
					SubstrateNode tmpSource=augment.getSource(tmpsl);
					SubstrateNode tmpSink=augment.getDest(tmpsl);
					SubstrateLink backward=augment.findEdge(tmpSink, tmpSource);
					CostResource backCost=(CostResource)backward.get().get(0);
					backCost.setCost(backCost.getCost()+flow);
				}
			}
			this.maxflow.put(sl, totalFlow);
			//result graph
			SubstrateNetwork resultGraph=new SubstrateNetwork();			
			for(Iterator<SubstrateLink> it=augment.getEdges().iterator();it.hasNext();){
				SubstrateLink forward=it.next();
				CostResource forCost=(CostResource)forward.get().get(0);
				double cost11=forCost.getCost();	//forward
				//backward
				SubstrateNode tmpSource=augment.getSource(forward);
				SubstrateNode tmpSink=augment.getDest(forward);
				SubstrateLink backward=augment.findEdge(tmpSink, tmpSource);
				CostResource backCost=(CostResource)backward.get().get(0);
				double cost22=backCost.getCost();
				double flowT=(cost11+cost22)/2-(cost11>cost22?cost22:cost11);
				//create links on the result graph
				
				if((flowT!=0)&&(resultGraph.findEdge(tmpSource, tmpSink)==null)){
					SubstrateLink link=new SubstrateLink();
					CostResource cost=new CostResource(link);
					cost.setCost(flowT);
					link.add(cost);
					resultGraph.addEdge(link, tmpSource, tmpSink);
				}
			}
			
			while(true){
				Predicate<SubstrateLink> pre=new Predicate<SubstrateLink>() {
					@Override
					public boolean evaluate(SubstrateLink arg0) {
						CostResource tmpCost=(CostResource)arg0.get().get(0);
						if(tmpCost.getCost()==0)
							return false;
						return true;
					}
				};
				EdgePredicateFilter<SubstrateNode,SubstrateLink> filter = new EdgePredicateFilter<SubstrateNode,SubstrateLink>(pre);
				Graph<SubstrateNode, SubstrateLink> tmp = filter.transform(resultGraph);
				DijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode, SubstrateLink>(tmp);
				List<SubstrateLink> paths=dijkstra.getPath(source, sink);
				if(paths.isEmpty())	break;	//no flow, exit
				double flow=1000;
				for(SubstrateLink tmpsl:paths){
					CostResource tmpCost=(CostResource)tmpsl.get().get(0);
					if(tmpCost.getCost()<flow&&tmpCost.getCost()>0)	flow=tmpCost.getCost();
				}
				for(SubstrateLink tmpsl:paths){
					CostResource tmpCost=(CostResource)tmpsl.get().get(0);
					tmpCost.setCost(tmpCost.getCost()-flow);
				}
				
				List<SubstrateLink> graphPath=new ArrayList<SubstrateLink>();
				for(SubstrateLink tmpsl:paths){
					SubstrateNode n1=resultGraph.getEndpoints(tmpsl).getFirst();
					SubstrateNode n2=resultGraph.getEndpoints(tmpsl).getSecond();
					graphPath.add(sNet.findEdge(n1, n2));
				}
				
				MaxFlowPath mfp=new MaxFlowPath(sl,graphPath,flow);
				sl.getMaxflow().add(mfp);
				
			}
			Collections.sort(sl.getMaxflow());
		}
		System.out.println("finished");
	}
}
