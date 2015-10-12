package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import li.multiDomain.Domain;
import vnreal.algorithms.AS_MCF;
import vnreal.algorithms.nodemapping.AvailableResourcesNodeMapping;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class MultiDomainAlgoTest {

	public static void main(String[] args) throws IOException {
		
		List<Domain> multiDomain = new ArrayList<Domain>();
		SubstrateNetwork sn1=new SubstrateNetwork(false,true); //control the directed or undirected
		sn1.alt2network("data/cost239");
		sn1.addAllResource(true);
		SubstrateNetwork sn2 = new SubstrateNetwork(false,true);
		sn2.alt2network("sndlib/abliene");
		sn2.addAllResource(true);
		//System.out.println(sn);
		
		List<VirtualNetwork> vns = new ArrayList<VirtualNetwork>();
		for(int i=0;i<15;i++){
			VirtualNetwork vn = new VirtualNetwork(1,false);
			vn.alt2network("data/vir"+i);
			vn.addAllResource(true);
			//System.out.println("virtual network\n"+vn);
			vns.add(vn);
		}
		
		for(int i=0;i<1;i++){
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
			
			multiDomain = sn.divide4Domain();
			
			System.out.println(multiDomain.get(0));
			System.out.println(multiDomain.get(1));
			System.out.println(multiDomain.get(2));
			System.out.println(multiDomain.get(3));
			
			/*
			AS_MCF as_mcf = new AS_MCF(multiDomain);
			as_mcf.linkMapping(vns.get(i),nodeMapping);
			*/
			
		}
		
		
		
		
		
		
	}
	

}
