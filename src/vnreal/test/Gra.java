package vnreal.test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mulavito.algorithms.shortestpath.disjoint.SuurballeTarjan;
import mulavito.algorithms.shortestpath.ksp.Yen;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import vnreal.algorithms.linkmapping.KShortestPath;
import vnreal.algorithms.linkmapping.KShortestPathLinkMapping;
import vnreal.algorithms.linkmapping.PathSplittingVirtualLinkMapping;
import vnreal.algorithms.nodemapping.AvailableResourcesNodeMapping;
import vnreal.algorithms.utils.Consts;
import vnreal.algorithms.utils.dataSolverFile;
import vnreal.demands.BandwidthDemand;
import vnreal.demands.CpuDemand;
import vnreal.evaluations.metrics.AcceptedVnrRatio;
import vnreal.evaluations.metrics.MappedRevenue;
import vnreal.evaluations.metrics.TotalRevenue;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.resources.BandwidthResource;
import vnreal.resources.CpuResource;

public class Gra {

	public static void main(String[] args) throws IOException {
		SubstrateNetwork sn=new SubstrateNetwork(false,true); //control the directed or undirected
		sn.alt2network("data/sub");	
		
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
		
		Iterator<SubstrateLink> it=sn.getEdges().iterator();
		while(it.hasNext()){
			double random = new Random().nextDouble();
			SubstrateLink sblk1 = (SubstrateLink)it.next();
			SubstrateLink sblk2 = (SubstrateLink)it.next();
			BandwidthResource bw1=new BandwidthResource(sblk1);
			BandwidthResource bw2=new BandwidthResource(sblk2);
			bw1.setBandwidth(50+random*(100-50));
			bw2.setBandwidth(50+random*(100-50));
			sblk1.add(bw1);
			sblk2.add(bw2);
			
		}
		/*
		for(SubstrateLink sblk : sn.getEdges()){
			double random = new Random().nextDouble();
			BandwidthResource bw = new BandwidthResource(sblk);
			bw.setBandwidth(50+random*(100-50));
			if(sblk.preAddCheck(bw))
				sblk.add(bw);
		}*/
		//System.out.println(sn);
		
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
		
		Transformer<SubstrateLink, Double> basicTrans = new Transformer<SubstrateLink,Double>(){
			public Double transform(SubstrateLink link){
				return 1.0;
			}
		};
		/*
		DijkstraShortestPath<SubstrateNode, SubstrateLink> dspw = new DijkstraShortestPath(sn,weightTrans);
		List<SubstrateLink> l = dspw.getPath((SubstrateNode)sn.getVertices().toArray()[1], (SubstrateNode)sn.getVertices().toArray()[8]);
		System.out.println("dijkstra : "+l);*/
		
		//yen k shortest path algo
		/*
		Yen<SubstrateNode, SubstrateLink> yen = new Yen(sn,basicTrans);
		List<List<SubstrateLink>> ksp = yen.getShortestPaths((SubstrateNode)sn.getVertices().toArray()[1], (SubstrateNode)sn.getVertices().toArray()[8], 5);
		System.out.println("yen k shortest path : "+ksp);
		*/
		
		//SuurballeTarjan 2 disjoint shortest path, minimize total cost of the k paths
		/*SuurballeTarjan<SubstrateNode, SubstrateLink> st = new SuurballeTarjan(sn, weightTrans);
		List<List<SubstrateLink>> sdsp = st.getDisjointPaths((SubstrateNode)sn.getVertices().toArray()[0], (SubstrateNode)sn.getVertices().toArray()[8]);
		System.out.println("Suurballe k disjoint shortest path : "+sdsp);
		*/
		
		//virtual network list
		List<VirtualNetwork> vns = new ArrayList<VirtualNetwork>();		
		
		for(int i=0;i<15;i++){
			//create virtual network		
			VirtualNetwork vn = new VirtualNetwork(1,false);
			vn.alt2network("data/vir"+i);
			//System.out.println("virtual network\n"+vn);
			
			//add resource cpu and bw
			for(VirtualNode vtnd:vn.getVertices()){
				double random = new Random().nextDouble();
				CpuDemand cpu = new CpuDemand(vtnd);
				cpu.setDemandedCycles(random*(50));
				if(vtnd.preAddCheck(cpu))
					vtnd.add(cpu);
			}
			
			Iterator<VirtualLink> itv=vn.getEdges().iterator();
			while(itv.hasNext()){
				double random = new Random().nextDouble();
				VirtualLink vtlk1 = (VirtualLink)itv.next();
				VirtualLink vtlk2 = (VirtualLink)itv.next();
				BandwidthDemand bw1=new BandwidthDemand(vtlk1);
				BandwidthDemand bw2=new BandwidthDemand(vtlk2);
				bw1.setDemandedBandwidth(random*50);
				bw2.setDemandedBandwidth(random*50);
				vtlk1.add(bw1);
				vtlk2.add(bw2);
				
			}
			/*
			for(VirtualLink vtlk : vn.getEdges()){
				double random = new Random().nextDouble();
				BandwidthDemand bw = new BandwidthDemand(vtlk);
				bw.setDemandedBandwidth(+random*(50));
				if(vtlk.preAddCheck(bw))
					vtlk.add(bw);
			}*/
			
			vns.add(vn);
		}
		
		//Network stack
		NetworkStack netst = new NetworkStack(sn,vns);	
		
		for(int i=0;i<15;i++){
			System.out.println("virtual network "+i+": \n"+vns.get(i));
			//node mapping
			AvailableResourcesNodeMapping arnm = new AvailableResourcesNodeMapping(sn,8,true,true);
			
			if(arnm.nodeMapping(vns.get(i))){
				System.out.println("node mapping succes, virtual netwotk "+i);
			}else{
				System.out.println("node resource error, virtual network "+i);
				continue;
			}
			Map<VirtualNode, SubstrateNode> nodeMapping = arnm.getNodeMapping();
			System.out.println(nodeMapping);
			
			//link mapping
			/*
			PathSplittingVirtualLinkMapping psvlm = new PathSplittingVirtualLinkMapping(sn,0.3,0.7);
			if(!psvlm.linkMapping(vns.get(i), nodeMapping)){
				System.out.println("link resource error, virtual network "+i);
				continue;
			}*/
			
			KShortestPath kspath = new KShortestPath(sn);
			if(kspath.linkMapping(vns.get(i), nodeMapping)){
				System.out.println("link mapping succes, virtual network "+i);
			}
			else{
				System.out.println("link resource error, virtual network "+i);
				continue;
			}
			System.out.println("vitual network "+i+", mapping succes!\n");
			
		}
		
		//System.out.println(sn);
		
		//total revenue
		TotalRevenue totalRevenue = new TotalRevenue(true);
		totalRevenue.setStack(netst);
		System.out.println("total revenue : "+totalRevenue.calculate());
		
		//mapped revenue
		MappedRevenue mappedRevenue = new MappedRevenue(true);
		mappedRevenue.setStack(netst);
		System.out.println("mapped revenue : "+mappedRevenue.calculate());
		
		//accepted ratio
		AcceptedVnrRatio acceptedRatio = new AcceptedVnrRatio();
		acceptedRatio.setStack(netst);
		System.out.println("accepted ratio : "+acceptedRatio.calculate()+"%");
		
		System.out.println("ok");
	}

}
