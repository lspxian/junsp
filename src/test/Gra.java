package test;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.resources.BandwidthResource;
import vnreal.resources.CpuResource;

public class Gra {

	public static void main(String[] args) throws IOException {
		SubstrateNetwork sn=new SubstrateNetwork(true);
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
		
		//add  source for all
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
		
		//shortest path example
		DijkstraShortestPath<SubstrateNode, SubstrateLink> dsp = new DijkstraShortestPath(sn);
		List<SubstrateLink> l = dsp.getPath((SubstrateNode)sn.getVertices().toArray()[11], (SubstrateNode)sn.getVertices().toArray()[24]);
		System.out.println(l);
		
		
		System.out.println("ok");
		
	}

}
