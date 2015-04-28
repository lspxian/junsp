package vnreal.test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mulavito.algorithms.shortestpath.disjoint.SuurballeTarjan;
import mulavito.algorithms.shortestpath.ksp.Yen;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import vnreal.algorithms.nodemapping.AvailableResourcesNodeMapping;
import vnreal.demands.CpuDemand;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.resources.BandwidthResource;
import vnreal.resources.CpuResource;

public class Gra {

	public static void main(String[] args) throws IOException {
		SubstrateNetwork sn=new SubstrateNetwork(false);
		sn.alt2network("data/sub");
		System.out.println(sn);
		
		//add resource example
		/*
		SubstrateNode sbnd = (SubstrateNode)sn.getVertices().toArray()[0];
		CpuResource cpu = new CpuResource(sbnd);
		if(sbnd.preAddCheck(cpu))
			sbnd.add(cpu);
		
		SubstrateLink sblk = (SubstrateLink)sn.getEdges().toArray()[0];
		BandwidthResource bw = new BandwidthResource(sblk);
		if(sblk.preAddCheck(bw))
			sblk.add(bw);
		*/
		
		//add resource for all
		for(SubstrateNode sbnd:sn.getVertices()){
			double random = new Random().nextDouble();
			CpuResource cpu = new CpuResource(sbnd);
			cpu.setCycles(50+random*(100-50));
			if(sbnd.preAddCheck(cpu))
				sbnd.add(cpu);
		}
		
		for(SubstrateLink sblk : sn.getEdges()){
			double random = new Random().nextDouble();
			BandwidthResource bw = new BandwidthResource(sblk);
			bw.setBandwidth(50+random*(100-50));
			if(sblk.preAddCheck(bw))
				sblk.add(bw);
		}
		
		//jung Dijkstra shortest path unweighted
//		DijkstraShortestPath<SubstrateNode, SubstrateLink> dsp = new DijkstraShortestPath(sn);
//		List<SubstrateLink> l = dsp.getPath((SubstrateNode)sn.getVertices().toArray()[11], (SubstrateNode)sn.getVertices().toArray()[24]);
//		System.out.println(l);
		
		//transformer : weighted Dijkstra shortest path
		Transformer<SubstrateLink, Double> weightTrans = new Transformer<SubstrateLink,Double>(){
			public Double transform(SubstrateLink link){
				return 1/((BandwidthResource)link.get().get(0)).getAvailableBandwidth();
			}
		};
		DijkstraShortestPath<SubstrateNode, SubstrateLink> dspw = new DijkstraShortestPath(sn,weightTrans);
		List<SubstrateLink> l = dspw.getPath((SubstrateNode)sn.getVertices().toArray()[1], (SubstrateNode)sn.getVertices().toArray()[24]);
		System.out.println(l);
		
		//yen k shortest path algo
		Yen<SubstrateNode, SubstrateLink> yen = new Yen(sn,weightTrans);
		List<List<SubstrateLink>> ksp = yen.getShortestPaths((SubstrateNode)sn.getVertices().toArray()[1], (SubstrateNode)sn.getVertices().toArray()[24], 10);
		System.out.println("yen k shortest path : "+ksp);
		
		//SuurballeTarjan 2 disjoint shortest path, minimize total cost of the k paths
		/*SuurballeTarjan<SubstrateNode, SubstrateLink> st = new SuurballeTarjan(sn, weightTrans);
		List<List<SubstrateLink>> sdsp = st.getDisjointPaths((SubstrateNode)sn.getVertices().toArray()[0], (SubstrateNode)sn.getVertices().toArray()[8]);
		System.out.println("Suurballe k disjoint shortest path : "+sdsp);
		*/
		
		//create virtual network
		VirtualNetwork vn1 = new VirtualNetwork(1,false);
		vn1.alt2network("data/vir0");
		System.out.println("virtual network\n"+vn1);
		
		//add resource
		for(VirtualNode vtnd:vn1.getVertices()){
			double random = new Random().nextDouble();
			CpuDemand cpu = new CpuDemand(vtnd);
			cpu.setDemandedCycles(random*(30));
			if(vtnd.preAddCheck(cpu))
				vtnd.add(cpu);
		}
		
		
		AvailableResourcesNodeMapping arnm = new AvailableResourcesNodeMapping(sn,5,false,true);
		arnm.nodeMapping(vn1);
		Map<VirtualNode, SubstrateNode> nodeMapping = arnm.getNodeMapping();
		System.out.println(nodeMapping);
		
		System.out.println("ok");
	}

}
