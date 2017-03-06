package main;

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections15.Transformer;

import li.gt_itm.DrawGraph;
import li.gt_itm.Generator;
import mulavito.algorithms.shortestpath.ksp.Yen;
import protectionProba.MaxFlowBackupVF;
import protectionProba.MaxFlowBackupVF2;
import protectionProba.MaxFlowPath;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class Test {

	public static void main(String[] args) throws IOException{
		SubstrateNetwork sn=new SubstrateNetwork();
//		sn.alt2network("sndlib/germany50");
		sn.alt2network("data/cost239");
		sn.addAllResource(true);
		DrawGraph dg = new DrawGraph(sn);
		dg.draw();
		sn.configPercentage(0.65);
		System.out.println(sn);
//		MaxFlowBackupVF mfb=new MaxFlowBackupVF(sn);
		MaxFlowBackupVF2 mfb2=new MaxFlowBackupVF2(sn);
		for(SubstrateLink sl:sn.getEdges()){
			System.out.println(sl.toString());
			for(MaxFlowPath mfp:sl.getMaxflow()){
				System.out.println(mfp);
			}
		}
		System.out.println("");
		
	}

}
