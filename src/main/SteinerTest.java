package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import li.SteinerTree.KMB1981;
import li.SteinerTree.ProbaCost;
import li.SteinerTree.SteinerILPExact;
import li.SteinerTree.Takahashi;
import vnreal.algorithms.linkmapping.MultiCommodityFlow;
import vnreal.algorithms.nodemapping.AvailableResourcesNodeMapping;
import vnreal.algorithms.nodemapping.CordinatedNodeLinkMapping;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class SteinerTest {

	public static void main(String[] args) throws IOException {
		SubstrateNetwork sn=new SubstrateNetwork(); //undirected by default 
		sn.alt2network("data/cost239");
		sn.addAllResource(true);
		System.out.println(sn.probaToString());
		
		VirtualNetwork vn = new VirtualNetwork();
		vn.alt2network("data/vir");
		vn.addAllResource(true);

		//node mapping
		AvailableResourcesNodeMapping arnm = new AvailableResourcesNodeMapping(sn,10,true,false);
		
		if(arnm.nodeMapping(vn)){
			System.out.println("node mapping succes, virtual netwotk ");
		}else{
			System.out.println("node resource error, virtual network ");
		}
		Map<VirtualNode, SubstrateNode> nodeMapping = arnm.getNodeMapping();
		System.out.println(nodeMapping);
		
		//link mapping
		
		SteinerILPExact stn = new SteinerILPExact(sn);
		stn.linkMapping(vn, nodeMapping);
		System.out.println("\n");
		
		Takahashi ta = new Takahashi(nodeMapping.values(),sn,new ProbaCost());
		ta.runSteinerTree();
		System.out.println(ta.getSteinerTree().probaToString());
		
		KMB1981 kmb = new KMB1981(nodeMapping.values(),sn,new ProbaCost());
		kmb.runSteinerTree();
		System.out.println(kmb.getSteinerTree().probaToString());
	}

}
