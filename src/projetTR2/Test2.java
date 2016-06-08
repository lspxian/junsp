package projetTR2;

import java.io.IOException;

import vnreal.algorithms.nodemapping.CordinatedNodeLinkMapping;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualNetwork;

public class Test2 {

	public static void main(String[] args) throws IOException {
		SubstrateNetwork sn=new SubstrateNetwork(); //undirected by default 
		sn.alt2network("data/cost239");
		sn.addAllResource(true);
		for(int i=0;i<10;i++){
			VirtualNetwork vn = new VirtualNetwork();
			vn.alt2network("data/vir"+i);
			vn.addAllResource(true);
			CordinatedNodeLinkMapping cnlm = new CordinatedNodeLinkMapping(sn);
			cnlm.nodeMapping(vn);
			
		}

		//System.out.println(sn);
		
	}

}
