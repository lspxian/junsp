package projetTR2;

import java.io.IOException;

import li.gt_itm.DrawGraph;
import li.gt_itm.Generator;
import vnreal.algorithms.nodemapping.CordinatedNodeLinkMapping;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualNetwork;

public class Test2 {

	public static void main(String[] args) throws IOException {
		SubstrateNetwork sn=new SubstrateNetwork(); //undirected by default 
		sn.alt2network("sndlib/germany50");
		DrawGraph dg = new DrawGraph(sn);
		dg.draw();
		sn.addAllResource(true);
		for(int i=0;i<1;i++){
			VirtualNetwork vn = new VirtualNetwork();
			Generator.createVirNet();
			vn.alt2network("./gt-itm/vir");
			vn.addAllResource(true);
			System.out.println(vn);
			CordinatedNodeLinkMapping cnlm = new CordinatedNodeLinkMapping(sn);
			cnlm.nodeMapping(vn);
			
		}

		//System.out.println(sn);
		
	}

}
