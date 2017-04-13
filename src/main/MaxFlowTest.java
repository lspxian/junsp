package main;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LinkedMap;

import edu.uci.ics.jung.algorithms.flows.EdmondsKarpMaxFlow;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import li.gt_itm.DrawGraph;
import protectionProba.MaxFlowPath;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.resources.BandwidthResource;

public class MaxFlowTest {

	public static void main(String[] args) throws IOException {
		SubstrateNetwork sNet=new SubstrateNetwork();
		sNet.alt2network("data/cost239");
		sNet.addAllResource(true);
		DrawGraph dg = new DrawGraph(sNet);
		dg.draw();
		sNet.configPercentage(0.65);
		
		SubstrateLink sl=sNet.findEdge(sNet.getNodeFromID(0), sNet.getNodeFromID(1));
		System.out.println(sNet);
		
		
		int maxflow=0;
		//call maxflow api
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
		maxflow=mf.getMaxFlow();
		System.out.println("The maxflow value is "+maxflow);
		
//		System.out.println("Edge flows: "+edgeFlowMap);
		String result="";
		for(SubstrateLink l:temp.getEdges()){
			Pair<SubstrateNode> pair = temp.getEndpoints(l);
			result += l + " (" + pair.getFirst().getId() + "<->"
					+ pair.getSecond().getId() + ") "+edgeFlowMap.get(l)+"\n";
		}
		System.out.println(result);
		
		//min cut
		System.out.println("min cut:");
		result="";
		for(SubstrateLink l:mf.getMinCutEdges()){
			Pair<SubstrateNode> pair = temp.getEndpoints(l);
			result += l + " (" + pair.getFirst().getId() + "<->"
					+ pair.getSecond().getId() + ") "+edgeFlowMap.get(l)+"\n";
		}
		System.out.println(result);
		
		//maxflow graph
	/*	System.out.println("graph:");
		result="";
		for(SubstrateLink l:mf.getFlowGraph().getEdges()){
			Pair<SubstrateNode> pair = mf.getFlowGraph().getEndpoints(l);
			result += l + " (" + pair.getFirst().getId() + "<->"
					+ pair.getSecond().getId() + ") "+"\n";
		}
		System.out.println(result);
		*/
		
	}
	
	
}
