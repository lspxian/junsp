package li.generator;

import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class Waxman {
	int nodeNomber;
	int xMax;
	int yMax;
	double alpha;
	double beta;
	
	
	public Waxman(int nodeNomber, int xMax,int yMax, double alpha, double beta){
		this.nodeNomber = nodeNomber;
		this.xMax = xMax;
		this.yMax = yMax;
		this.alpha = alpha;
		this.beta = beta;
	}
	
	//option : 1 SubstrateNetwork 2 VirtualNetwork
	public VirtualNetwork generateVirtualNetwork(){
		VirtualNetwork network = new VirtualNetwork(1);
		
		//generate nodes
		VirtualNode vn;
		for(int i=0;i<nodeNomber;i++){
			vn=new VirtualNode();
			vn.setCoordinateX(Math.random()*xMax);
			vn.setCoordinateY(Math.random()*yMax);
			network.addVertex(vn);
		}
		
		//generate links
		double maxDistance=0;
		double distance = 0;
		for(VirtualNode start : network.getVertices()){
			for(VirtualNode end : network.getVertices()){
				distance = Math.sqrt(Math.pow(start.getCoordinateX()-end.getCoordinateX(), 2) 
						+ Math.pow(start.getCoordinateY()-end.getCoordinateY(), 2));
				if(distance>maxDistance)	maxDistance = distance;
			}
		}
		
		for(VirtualNode start : network.getVertices()){
			for(VirtualNode end : network.getVertices()){
				distance = Math.sqrt(Math.pow(start.getCoordinateX()-end.getCoordinateX(), 2) 
						+ Math.pow(start.getCoordinateY()-end.getCoordinateY(), 2));
				double proba = alpha * Math.exp(-distance/beta/maxDistance);
				if(proba>Math.random()){
					VirtualLink vl = new VirtualLink();
					network.addEdge(vl, start, end);
				}
				
			}
		}
		
		return network;
		
	}
	
	
	public void generateFile(){
		
	}
	
}
