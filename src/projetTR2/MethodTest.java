package projetTR2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import vnreal.algorithms.linkmapping.MultiCommodityFlow;
import vnreal.algorithms.nodemapping.AvailableResourcesNodeMapping;
import vnreal.network.substrate.AugmentedNetwork;
import vnreal.network.substrate.MetaLink;
import vnreal.network.substrate.MetaNode;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class MethodTest {

	public static void main(String[] args) throws IOException {
		SubstrateNetwork sn=new SubstrateNetwork(); //undirected by default 
		sn.alt2network("data/cost239");
		sn.addAllResource(true);
		
		List<VirtualNetwork> vns = new ArrayList<VirtualNetwork>();
		for(int i=0;i<5;i++){
			VirtualNetwork vn = new VirtualNetwork();
			vn.alt2network("data/vir"+i);
			vn.addAllResource(true);
			//System.out.println("virtual network\n"+vn);
			vns.add(vn);
		}
	   AugmentedNetwork an = new AugmentedNetwork(sn);
	   List<SubstrateNode> candidates = new LinkedList<SubstrateNode>();
		
		for(int i=0;i<1;i++){
			System.out.println("virtual network "+i+": \n"+vns.get(i));
			//node mapping
			AvailableResourcesNodeMapping arnm = new AvailableResourcesNodeMapping(sn,80,true,false);
			/*
			if(arnm.nodeMapping(vns.get(i))){
				System.out.println("node mapping succes, virtual netwotk "+i);
			}else{
				System.out.println("node resource error, virtual network "+i);
				continue;
			}
			Map<VirtualNode, SubstrateNode> nodeMapping = arnm.getNodeMapping();
			System.out.println(nodeMapping);*/
			
			for(Iterator<VirtualNode> itt = vns.get(i).getVertices().iterator(); itt
				.hasNext();)
			{
				VirtualNode currNode = itt.next();
				MetaNode mnode = new MetaNode();
				mnode.setCoordinateX(currNode.getCoordinateX());
				mnode.setCoordinateY(currNode.getCoordinateY());
				//mnode.addResource(currNode.);
				an.addVertex(mnode);
				candidates = arnm.SearchCandidates(currNode);
				System.out.println(candidates);
				for (SubstrateNode node : candidates ){
					MetaLink mlink = new MetaLink();
					mlink.addResource(1000);
					an.addEdge(mlink, mnode, node);
				}
				
				
			}
			System.out.println(an);
			
			//link mapping
			// vous utilisez les fichiers tr2mcf. Vous n'aurez pas de collision avec moi.
//			MultiCommodityFlow mcf = new MultiCommodityFlow(sn, "ILP-LP-Models/tr2mcf.lp", "pytest/tr2mcf.lp");
//			mcf.linkMapping(vns.get(i), nodeMapping);
			
		}
		
		System.out.println(sn);
		
	}

}
