package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import vnreal.algorithms.linkmapping.UnsplittingLPCplex;
import vnreal.algorithms.nodemapping.AvailableResourcesNodeMapping;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class FreeResourceTest {

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException {
		
		SubstrateNetwork sn=new SubstrateNetwork(false,true); //control the directed or undirected
		sn.alt2network("data/cost239");
		
		sn.addAllResource(true);
		//System.out.println(sn);
		//virtual network list
		List<VirtualNetwork> vns = new ArrayList<VirtualNetwork>();
		for(int i=0;i<15;i++){
			VirtualNetwork vn = new VirtualNetwork(1,false);
			vn.alt2network("data/vir"+i);
			vn.addAllResource(true);
			//System.out.println("virtual network\n"+vn);
			vns.add(vn);
			
		}
		
		//Network stack
	
		@SuppressWarnings("unused")
		NetworkStack netst = new NetworkStack(sn,vns);	
		
		//Mapping
		
		for(int i=0;i<5;i++){
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
		}
		
		System.out.println(sn);
		
		//Free resource
	
		NodeLinkDeletion ndl = new NodeLinkDeletion();
		for(int i=0;i<5;i++){
			ndl.freeResource(vns.get(i), sn);
		}
		System.out.println(sn);
		
		
		
	}

}
