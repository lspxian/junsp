package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import li.multiDomain.Domain;
import vnreal.algorithms.linkmapping.AS_MCF;
import vnreal.algorithms.linkmapping.Shen2014;
import vnreal.algorithms.nodemapping.MultiDomainAvailableResources;
import vnreal.network.substrate.InterLink;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class MultiDomainAlgoTest {

	public static void main(String[] args) throws IOException {
		
		List<Domain> multiDomain = new ArrayList<Domain>();
		//int x,int y, file path, resource
		multiDomain.add(new Domain(0,0,"data/cost239", false));
		multiDomain.add(new Domain(1,0,"sndlib/abilene", false));
		//multiDomain.add(new Domain(1,1,"data/cost239", false));
		//multiDomain.add(new Domain(0,1,"sndlib/abilene", false));
				
		//max distance for all the domains
		double maxDistance=0, distance=0, ax,ay,bx,by;
		for(int i=0;i<multiDomain.size();i++){
			Domain startDomain = multiDomain.get(i);
			for(int j=i;j<multiDomain.size();j++){
				Domain endDomain = multiDomain.get(j);
				for(SubstrateNode start : startDomain.getVertices()){
					for(SubstrateNode end : endDomain.getVertices()){
						ax = start.getCoordinateX()+startDomain.getCoordinateX()*100;
						ay = start.getCoordinateY()+startDomain.getCoordinateY()*100;
						bx = end.getCoordinateX()+endDomain.getCoordinateX()*100;
						by = end.getCoordinateY()+endDomain.getCoordinateY()*100;
						distance = Math.sqrt(Math.pow(ax-ay, 2) + Math.pow(bx-by, 2));
						if(distance>maxDistance)	maxDistance = distance;
						
					}
				}
				
			}
		}
		
		//generate inter links
		double alpha = 0.6;	//alpha increases the probability of edges between any nodes in the graph
		double beta = 0.2;	//beta yields a larger ratio of long edges to short edges.
		for(int i=0;i<multiDomain.size();i++){
			Domain startDomain = multiDomain.get(i);
			for(int j=i+1;j<multiDomain.size();j++){
				Domain endDomain = multiDomain.get(j);
				//for each pair of domain, generate inter links by Waxman
				for(SubstrateNode start : startDomain.getVertices()){
					for(SubstrateNode end : endDomain.getVertices()){
						ax = start.getCoordinateX()+startDomain.getCoordinateX()*100;
						ay = start.getCoordinateY()+startDomain.getCoordinateY()*100;
						bx = end.getCoordinateX()+endDomain.getCoordinateX()*100;
						by = end.getCoordinateY()+endDomain.getCoordinateY()*100;
						distance = Math.sqrt(Math.pow(ax-ay, 2) + Math.pow(bx-by, 2));
						double proba = alpha * Math.exp(-distance/beta/maxDistance);
						if(proba>new Random().nextDouble()){
							//source, destination, destination domain, random resource 
							InterLink il = new InterLink(start, end, false);
							startDomain.addInterLink(il);
							endDomain.addInterLink(il);
							//startDomain.addInterLink(start, end, endDomain, false);
							//endDomain.addInterLink(end, start, startDomain, false);
						}
					}
				}
				
				
			}
		}
		/*
		System.out.println(multiDomain.get(0));
		System.out.println(multiDomain.get(1));
		System.out.println(multiDomain.get(2));
		System.out.println(multiDomain.get(3));
	*/
		

		List<VirtualNetwork> vns = new ArrayList<VirtualNetwork>();
		for(int i=0;i<15;i++){
			VirtualNetwork vn = new VirtualNetwork(1,false);
			vn.alt2network("data/vir"+i);
			vn.addAllResource(true);
			vn.scale(2, 1);
			
			//System.out.println("virtual network\n"+vn);
			vns.add(vn);
		}
		
		for(int i=0;i<1;i++){
			System.out.println("virtual network "+0+": \n"+vns.get(i));
			MultiDomainAvailableResources mdar = new MultiDomainAvailableResources(multiDomain,50);
			if(mdar.nodeMapping(vns.get(i))){
				System.out.println("node mapping succes, virtual netwotk "+i);
			}else{
				System.out.println("node resource error, virtual network "+i);
				continue;
			}
			Map<VirtualNode, SubstrateNode> nodeMapping = mdar.getNodeMapping();
			System.out.println(nodeMapping);
			
			
			AS_MCF as_mcf = new AS_MCF(multiDomain);
			as_mcf.linkMapping(vns.get(i),nodeMapping);
			
			/*
			Shen2014 shen = new Shen2014(multiDomain);
			shen.linkMapping(vns.get(12), nodeMapping);
			*/
			System.out.println(multiDomain.get(0));
			System.out.println(multiDomain.get(1));
		}
		
		
		
		
		
		
	}
	

}
