package main;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import vnreal.algorithms.AbstractMultiDomainLinkMapping;
import vnreal.algorithms.linkmapping.AS_MCF;
import vnreal.algorithms.linkmapping.MDasOD2;
import vnreal.algorithms.linkmapping.MultiDomainAsOneDomain;
import vnreal.algorithms.linkmapping.MultiDomainRanking;
import vnreal.algorithms.linkmapping.Shen2014;
import vnreal.algorithms.linkmapping.TwoDomainMCF;
import vnreal.algorithms.nodemapping.MultiDomainAvailableResources;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import li.gt_itm.Generator;
import li.multiDomain.Domain;
import li.multiDomain.MultiDomainUtil;

public class SimpleMultiDomainTest {

	public static void main(String[] args) throws IOException {
		
		//print to a file instead of console
		PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
		System.setOut(out);
		
		List<Domain> multiDomain = new ArrayList<Domain>();
		//int x,int y, file path, resource
		multiDomain.add(new Domain(0,0,"data/cost239", true));
		multiDomain.add(new Domain(1,0,"sndlib/abilene", true));
		
		//use gt-itm to create random net
//		multiDomain.add(new Domain(0,0, true));
//		multiDomain.add(new Domain(1,0, true));

		MultiDomainUtil.staticInterLinks(multiDomain.get(0),multiDomain.get(1));
//		MultiDomainUtil.randomInterLinks(multiDomain);
		
		
		List<VirtualNetwork> vns = new ArrayList<VirtualNetwork>();
		for(int i=0;i<100;i++){
			VirtualNetwork vn = new VirtualNetwork(1,false);
			vn.alt2network("data/vir"+i);
			vn.addAllResource(true);
			vn.scale(2, 1);
			vns.add(vn);
		}

			
		for(int i=0;i<19;i++){
			VirtualNetwork vn = vns.get(i);
			
			/*
			VirtualNetwork vn = new VirtualNetwork(1,false);
			Generator.createVirNet();
			vn.alt2network("./gt-itm/sub");
			vn.addAllResource(true);
			vn.scale(2, 1);*/
			
			System.out.println("virtual network "+i+": \n"+vn);
			
			MultiDomainAvailableResources mdar = new MultiDomainAvailableResources(multiDomain,80);
			if(mdar.nodeMapping(vn)){
				System.out.println("node mapping succes, virtual netwotk "+i);
			}else{
				System.out.println("node resource error, virtual network "+i);
				continue;
			}
			Map<VirtualNode, SubstrateNode> nodeMapping = mdar.getNodeMapping();
			System.out.println(nodeMapping);
		
			System.out.println("link mapping, virtual network "+i+"\n");
			
//			AbstractMultiDomainLinkMapping method = new MultiDomainRanking(multiDomain);
			
//			TwoDomainMCF method = new TwoDomainMCF(multiDomain);
			
//			AS_MCF method = new AS_MCF(multiDomain);
		
			Shen2014 method = new Shen2014(multiDomain);
			
//			MultiDomainAsOneDomain method = new MultiDomainAsOneDomain(multiDomain);
			
//			MDasOD2 method = new MDasOD2(multiDomain);
			
			method.linkMapping(vn, nodeMapping);
			
			System.out.println("virtual network "+i+" finished \n\n");
			
//			System.out.println(multiDomain.get(0));
//			System.out.println(multiDomain.get(1));
			
			
			//Multi domain free resource
//			NodeLinkDeletion.multiDomainFreeResource(vn, multiDomain);
			
			System.out.println(multiDomain.get(0));
			System.out.println(multiDomain.get(1));

		
		}
		
//		System.out.println(multiDomain.get(0));
//		System.out.println(multiDomain.get(1));
		
//		MultiDomainUtil.reset(multiDomain);
//		
//		System.out.println(multiDomain.get(0));
//		System.out.println(multiDomain.get(1));
//		
		
	}

}
