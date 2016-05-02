package main;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import li.multiDomain.Domain;
import li.multiDomain.MultiDomainUtil;
import li.simulation.Distribute3DVNE;
import vnreal.algorithms.linkmapping.AS_MCF;
import vnreal.algorithms.linkmapping.AllPossibleMDRanking;
import vnreal.algorithms.linkmapping.Shen2014;
import vnreal.algorithms.nodemapping.MultiDomainAvailableResources;
import vnreal.network.substrate.InterLink;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class MultiDomainAlgoTest {

	public static void main(String[] args) throws IOException {
		
		//print to a file instead of console
		//PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
		//System.setOut(out);
		
		List<Domain> multiDomain = new ArrayList<Domain>();
		//int x,int y, file path, resource
		multiDomain.add(new Domain(0,0,"sndlib/india35", true));
		multiDomain.add(new Domain(1,0,"sndlib/pioro40", true));
		multiDomain.add(new Domain(2,0,"sndlib/germany50", true));
		
		//MultiDomainUtil.staticInterLinks(multiDomain.get(0),multiDomain.get(1));
		MultiDomainUtil.random3DInterLinks(multiDomain);
		
		for(int i=0;i<multiDomain.size();i++){
			System.out.println("inter:"+multiDomain.get(i).getInterLinkCount());				
		}
	
		List<VirtualNetwork> vns = new ArrayList<VirtualNetwork>();
		for(int i=0;i<15;i++){
			VirtualNetwork vn = new VirtualNetwork(1,false);
			vn.alt2network("data/vir"+i);
			vn.addAllResource(true);
			vn.scale(3, 1);
			//System.out.println("virtual network\n"+vn);
			vns.add(vn);
		}
		
		/*
		for(int i=0;i<2;i++){
			System.out.println("virtual network "+i+": \n"+vns.get(i));
		}
		List<Domain> tmpDomains = MultiDomainUtil.getCopy(true, multiDomain);
		
		System.out.println(multiDomain.get(0));
		System.out.println(multiDomain.get(1));
		System.out.println(tmpDomains.get(0));
		System.out.println(tmpDomains.get(1));
		
		
		
		System.out.println("********as_mcf****************");
		tmpDomains = new ArrayList<Domain>();
		for(Domain domain : multiDomain){
			tmpDomains.add(domain.deepCopy());
		}*/
		for(int i=3;i<4;i++){
			System.out.println("virtual network "+i+": \n"+vns.get(i));
			MultiDomainAvailableResources mdar = new MultiDomainAvailableResources(multiDomain,50);
			if(mdar.nodeMapping(vns.get(i))){
				System.out.println("node mapping succes, virtual netwotk "+i);
			}else{
				System.out.println("node resource error, virtual network "+i);
				continue;
			}
			Map<VirtualNode, SubstrateNode> nodeMapping = mdar.getNodeMapping();
			System.out.println(nodeMapping);
			
			System.out.println("link mapping, virtual network "+i+"\n");
			AllPossibleMDRanking dis = new AllPossibleMDRanking(multiDomain);
			dis.linkMapping(vns.get(i), nodeMapping);
			System.out.println("virtual network "+i+" finished \n\n");
			
		}
		
		
		System.out.println(multiDomain.get(0));
		System.out.println(multiDomain.get(1));
		System.out.println(multiDomain.get(2));
		

		
		
	}
	
	
	

}


