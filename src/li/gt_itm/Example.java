package li.gt_itm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LinkedMap;

import edu.uci.ics.jung.algorithms.flows.EdmondsKarpMaxFlow;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.resources.BandwidthResource;

public class Example {

	public static void main(String[] args) throws IOException {
/*
		Centralized_MD_VNE_Simulation simulation = new Centralized_MD_VNE_Simulation();
		
		for(int i=0;i<simulation.getMultiDomain().size();i++){
			System.out.println(simulation.getMultiDomain().get(i));
		}
		
		for(int i=0;i<simulation.getMultiDomain().size();i++){
			System.out.println("v:"+simulation.getMultiDomain().get(i).getVertexCount());
			System.out.println("e:"+simulation.getMultiDomain().get(i).getEdgeCount());
		}
		for(int i=0;i<simulation.getMultiDomain().size();i++){
			System.out.println("inter:"+simulation.getMultiDomain().get(i).getInterLinkCount());				
		}*/
		
		//substrat network
//		Generator.createSubNet();
		SubstrateNetwork sn=new SubstrateNetwork();
//		sn.alt2network("./gt-itm/sub");
		sn.alt2network("./data/cost239");
		sn.addAllResource(false);
		sn.configPercentage(0.6);
		System.out.println("substrate network : v "+sn.getVertexCount()+" e "+sn.getEdgeCount());
		DrawGraph dg = new DrawGraph(sn);
		dg.draw();
		System.out.println(calculateMaxflow(sn));
		
		//virtual networks
		/*
		for(int i=0;i<400;i++){
			Generator.createVirNet();
			BufferedReader br = new BufferedReader(new FileReader("gt-itm/sub"));
			String line=null;
			System.out.println("new vn");
			while((line=br.readLine())!=null){
				System.out.println(line);
			}
		br.close();	
		}
		*/
		
		
		
	}
	public static Map<SubstrateLink,Integer> calculateMaxflow(SubstrateNetwork sNet){
		
		Map<SubstrateLink,Integer> maxflow=new HashMap<SubstrateLink,Integer>();
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
				/*	temp.addEdge(slink, 
							sNet.getEndpoints(slink).getFirst(),
							sNet.getEndpoints(slink).getSecond());
					temp.addEdge(slink2, 
							sNet.getEndpoints(slink).getSecond(),
							sNet.getEndpoints(slink).getFirst());*/
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
			maxflow.put(sl, mf.getMaxFlow());
		}
		return maxflow;
	}

}
