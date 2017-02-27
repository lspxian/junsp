package protectionProba;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.filters.EdgePredicateFilter;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import vnreal.algorithms.AbstractLinkMapping;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.resources.BandwidthResource;
import vnreal.resources.CostResource;

public class MaxFlowBackupVF2 extends AbstractLinkMapping {

	public MaxFlowBackupVF2(SubstrateNetwork sNet) {
		super(sNet);
		calculateMaxflow(sNet);
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void calculateMaxflow(SubstrateNetwork sNet) {
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
					if(tmpCost.getCost()<1000&&tmpCost.getCost()>0)	flow=tmpCost.getCost();
				}
				totalFlow=+flow;
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
			System.out.println(totalFlow);
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
				if(flowT!=0){
					SubstrateLink link=new SubstrateLink();
					CostResource cost=new CostResource(link);
					link.add(cost);
					resultGraph.addEdge(link, tmpSource, tmpSink);
				}
				
				augment.removeEdge(forward);
				augment.removeEdge(backward);
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
					if(tmpCost.getCost()<1000&&tmpCost.getCost()>0)	flow=tmpCost.getCost();
				}
				MaxFlowPath mfp=new MaxFlowPath(sl,paths,flow);
				sl.getMaxflow().add(mfp);
				
			}
			Collections.sort(sl.getMaxflow());
			
			
		}
	}
}
