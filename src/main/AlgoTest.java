package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import li.gt_itm.Generator;
import probabilityBandwidth.AbstractProbaLinkMapping;
import probabilityBandwidth.PBBWExactILP;
import probabilityBandwidth.ProbaHeuristic1;
import vnreal.algorithms.linkmapping.MultiCommodityFlow;
import vnreal.algorithms.linkmapping.SOD_BK;
import vnreal.algorithms.linkmapping.SteinerTreeHeuristic;
import vnreal.algorithms.linkmapping.UnsplittingLPCplex;
import vnreal.algorithms.nodemapping.AvailableResourcesNodeMapping;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class AlgoTest {

	public static void main(String[] args) throws IOException {

		SubstrateNetwork sn=new SubstrateNetwork(); //control the directed or undirected
		Generator.createSubNet();
		sn.alt2network("./gt-itm/sub");
//		sn.alt2network("data/cost239");
		sn.addAllResource(true);
		
		List<VirtualNetwork> vns = new ArrayList<VirtualNetwork>();
		for(int i=0;i<15;i++){
			VirtualNetwork vn = new VirtualNetwork();
			Generator.createVirNet();
			vn.alt2network("./gt-itm/sub");
		//	vn.alt2network("data/vir"+i);
			vn.addAllResource(true);
			//System.out.println("virtual network\n"+vn);
			vns.add(vn);
		}
		
		for(int i=2;i<8;i++){
			System.out.println("virtual network "+i+": \n"+vns.get(i));
			//node mapping
			AvailableResourcesNodeMapping arnm = new AvailableResourcesNodeMapping(sn,50,true,false);
			
			if(arnm.nodeMapping(vns.get(i))){
				System.out.println("node mapping succes, virtual netwotk "+i);
			
			Map<VirtualNode, SubstrateNode> nodeMapping = arnm.getNodeMapping();
			System.out.println(nodeMapping);
			
				//link mapping
				
	//			MultiCommodityFlow mcf = new MultiCommodityFlow(sn);
	//			mcf.linkMapping(vns.get(i), nodeMapping);
				
	//			UnsplittingLPCplex ulpc = new UnsplittingLPCplex(sn,0.3,0.7);
	//			ulpc.linkMapping(vns.get(i), nodeMapping);
	//			SOD_BK sod_bk = new SOD_BK(sn);
	//			sod_bk.linkMapping(vns.get(i), nodeMapping);
				
//				SteinerTreeHeuristic st = new SteinerTreeHeuristic(sn,"Takahashi");
//				System.out.println(st.linkMapping(vns.get(i), nodeMapping));
			
				AbstractProbaLinkMapping method; 
				method= new PBBWExactILP(sn);
//				method = new ProbaHeuristic1(sn);
				System.out.println(method.linkMapping(vns.get(i), nodeMapping));
				
				
				
			
			}else{
				System.out.println("node resource error, virtual network "+i);
				continue;
			}
		}
		
		for(int i=0;i<10;i++){
			NodeLinkDeletion.freeResource(vns.get(i), sn);
		}
		
		System.out.println(sn.probaToString());
		
		
		
	}

}
