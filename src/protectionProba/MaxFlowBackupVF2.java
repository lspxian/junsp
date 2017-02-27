package protectionProba;

import java.util.List;
import java.util.Map;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
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
			
			DirectedSparseGraph<SubstrateNode,SubstrateLink> temp=new DirectedSparseGraph<SubstrateNode,SubstrateLink>();
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
					
					temp.addEdge(slink2, new Pair<SubstrateNode>(
							sNet.getEndpoints(slink).getFirst(),
							sNet.getEndpoints(slink).getSecond()), EdgeType.DIRECTED);
					temp.addEdge(slink3, new Pair<SubstrateNode>(
							sNet.getEndpoints(slink).getSecond(),
							sNet.getEndpoints(slink).getFirst()), EdgeType.DIRECTED);
					
					DijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode, SubstrateLink>(temp);
					List<SubstrateLink> paths=dijkstra.getPath(source, sink);
					
					double flow=1000;
					for(SubstrateLink tmpsl:paths){
						CostResource tmpCost=(CostResource)tmpsl.get().get(0);
						if(tmpCost.getCost()<1000)	flow=tmpCost.getCost();
					}
					for(SubstrateLink tmpsl:paths){
						CostResource tmpCost=(CostResource)tmpsl.get().get(0);
						tmpCost.setCost(tmpCost.getCost()-flow);
						
					}
					
					
					
				}
			}
			
			
			
		}
	}
}
