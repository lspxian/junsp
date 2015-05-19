package vnreal.test;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import mulavito.graph.generators.WaxmanGraphGenerator;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class LocalGene {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		NetworkStack netst = myGeneTopo(10,6);
		System.out.println(netst);
		
		
	}
	
	
	 public static NetworkStack myGeneTopo(int snNodes,int vnNumber){
		 
		 int[] vnsNodes = new int[vnNumber];
		 double[] vnsAlpha = new double[vnNumber];
		 double[] vnsBeta = new double[vnNumber];
		 for(int i=0;i<vnNumber;i++){
			 vnsNodes[i] = new Random().nextInt(8)+2;
			// vnsNodes[i] =3;
			 vnsAlpha[i] = 0.5;
			 vnsBeta[i] = 1; 
		 }
		 
		 return generateTopology(snNodes,0.5,1,vnNumber,vnsNodes,vnsAlpha,vnsBeta);
	 }

	
	public static NetworkStack generateTopology(int snNodes, double snAlpha,
			double snBeta, int vnNumber, int[] vnsNodes, double[] vnsAlpha,
			double[] vnsBeta) {
		
		NetworkStack result =new NetworkStack(new SubstrateNetwork(false),new LinkedList<VirtualNetwork>());
		SubstrateNetwork substrate = result.getSubstrate();

		// generate substrate network
		while (snNodes-- > 0)
			substrate.addVertex(new SubstrateNode());

		WaxmanGraphGenerator<SubstrateNode, SubstrateLink> sgg = new WaxmanGraphGenerator<SubstrateNode, SubstrateLink>(
				snAlpha, snBeta, true);
		sgg.generate(substrate);

		HashMap<SubstrateNode, Point2D> spos = sgg.getPositions();
		for (SubstrateNode v : substrate.getVertices()) {
			v.setCoordinateX(100.0 * spos.get(v).getX());
			v.setCoordinateY(100.0 * spos.get(v).getY());
		}
		
		//System.out.println(substrate.getEdgeCount());

		// generate virtual networks
		VirtualNetwork vn;
		int virtualNodes;
		double virtualAlpha;
		double virtualBeta;
		for (int layer = 1; layer <= vnNumber; layer++) {
			vn = new VirtualNetwork(layer);
			result.addLayer(vn);
			virtualNodes = vnsNodes[layer - 1];
			virtualAlpha = vnsAlpha[layer - 1];
			virtualBeta = vnsBeta[layer - 1];
			while (virtualNodes-- > 0)
				vn.addVertex(new VirtualNode(layer));

			WaxmanGraphGenerator<VirtualNode, VirtualLink> vgg = new WaxmanGraphGenerator<VirtualNode, VirtualLink>(
					virtualAlpha, virtualBeta, true);
			vgg.generate(vn);

			HashMap<VirtualNode, Point2D> vpos = vgg.getPositions();
			for (VirtualNode v : vn.getVertices()) {
				v.setCoordinateX(100.0 * vpos.get(v).getX());
				v.setCoordinateY(100.0 * vpos.get(v).getY());
			}
		}
		
		
		
		return result;
	}
}
