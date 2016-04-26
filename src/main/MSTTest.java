package main;

import java.io.IOException;

import li.SteinerTree.Cycle;
import li.SteinerTree.KruskalMST;
import li.SteinerTree.PrimMST;
import vnreal.network.substrate.SubstrateNetwork;

public class MSTTest {

	public static void main(String[] args) throws IOException {
		SubstrateNetwork sn=new SubstrateNetwork(); //undirected by default 
		sn.alt2network("data/cost239");
		sn.addAllResource(true);
		System.out.println(sn.probaToString());
//		Cycle c = new Cycle(sn);
//		System.out.println(c.isCyclic());
		
		KruskalMST kruskal = new KruskalMST(sn);
		System.out.println(kruskal.getMST().probaToString());

		PrimMST prim = new PrimMST(sn);
		System.out.println(prim.getMST().probaToString());

	}

}
