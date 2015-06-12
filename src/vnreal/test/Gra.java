package vnreal.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mulavito.algorithms.shortestpath.disjoint.SuurballeTarjan;
import mulavito.algorithms.shortestpath.ksp.LocalBypass;
import mulavito.algorithms.shortestpath.ksp.Yen;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import vnreal.algorithms.linkmapping.KShortestPath;
import vnreal.algorithms.linkmapping.KShortestPathLinkMapping;
import vnreal.algorithms.linkmapping.PathSplittingVirtualLinkMapping;
import vnreal.algorithms.linkmapping.UnsplittingLPCplex;
import vnreal.algorithms.linkmapping.UnsplittingVirtualLinkMapping;
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

public class Gra {

	public static void main(String[] args) throws IOException {
		SubstrateNetwork sn=new SubstrateNetwork(false,true); //control the directed or undirected
		sn.alt2network("newData/Cost239");
		
		System.out.println(sn.getNextHop((SubstrateNode) sn.getVertices().toArray()[8]));
		System.out.println(sn.getLastHop((SubstrateNode) sn.getVertices().toArray()[8]));
		
		sn.addAllResource(false);
		
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
	/*	Yen<SubstrateNode, SubstrateLink> yen = new Yen(sn,basicTrans);
		List<List<SubstrateLink>> ksp = yen.getShortestPaths((SubstrateNode)sn.getVertices().toArray()[2], (SubstrateNode)sn.getVertices().toArray()[1], 3);
		System.out.println("yen k shortest path : "+ksp);
		*/
		
		//SuurballeTarjan 2 disjoint shortest path, minimize total cost of the k paths
		/*SuurballeTarjan<SubstrateNode, SubstrateLink> st = new SuurballeTarjan(sn, weightTrans);
		List<List<SubstrateLink>> sdsp = st.getDisjointPaths((SubstrateNode)sn.getVertices().toArray()[0], (SubstrateNode)sn.getVertices().toArray()[8]);
		System.out.println("Suurballe k disjoint shortest path : "+sdsp);
		*/
		/*
		LocalBypass<SubstrateNode, SubstrateLink> lb = new LocalBypass(sn,basicTrans);
		List<List<SubstrateLink>> bypass = lb.getShortestPaths((SubstrateLink)sn.getEdges().toArray()[8], 3);
		System.out.println("local bypass : "+bypass);
		*/
		
		//virtual network list
		List<VirtualNetwork> vns = new ArrayList<VirtualNetwork>();
		for(int i=0;i<15;i++){
			VirtualNetwork vn = new VirtualNetwork(1,false);
			vn.alt2network("newData/vir"+i);
			vn.addAllResource(true);
			//System.out.println("virtual network\n"+vn);
			vns.add(vn);
			
		}
		
		//Network stack
	
		NetworkStack netst = new NetworkStack(sn,vns);	
		
		for(int i=0;i<10;i++){
			System.out.println("virtual network "+i+": \n"+vns.get(i));
			//node mapping
			AvailableResourcesNodeMapping arnm = new AvailableResourcesNodeMapping(sn,50,true,false);
			
			if(arnm.nodeMapping(vns.get(i))){
				System.out.println("node mapping succes, virtual netwotk "+i);
			}else{
				System.out.println("node resource error, virtual network "+i);
				continue;
			}
			Map<VirtualNode, SubstrateNode> nodeMapping = arnm.getNodeMapping();
			System.out.println(nodeMapping);
			
			//link mapping
			
			UnsplittingLPCplex ulpc = new UnsplittingLPCplex(sn,0.3,0.7);
			ulpc.linkMapping(vns.get(i), nodeMapping);
			//ulpc.generateFile(vns.get(i), nodeMapping);
			
	//		PathSplittingVirtualLinkMapping psvlm = new PathSplittingVirtualLinkMapping(sn,0.3,0.7);
		/*	UnsplittingVirtualLinkMapping psvlm = new UnsplittingVirtualLinkMapping(sn,0.3,0.7);
			if(!psvlm.linkMapping(vns.get(i), nodeMapping)){
				System.out.println("link resource error, virtual network "+i);
				continue;
			}
			
			KShortestPath kspath = new KShortestPath(sn);
			if(kspath.linkMapping(vns.get(i), nodeMapping)){
				System.out.println("link mapping succes, virtual network "+i);
			}
			else{
				System.out.println("link resource error, virtual network "+i);
				continue;
			}
			System.out.println("vitual network "+i+", mapping succes!\n");
			*/
		}
		
		System.out.println(sn);
		
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
