package li.SteinerTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class KMB1981 {
	private List<SubstrateNode> participant;
	private SubstrateNetwork sNet;
	private SubstrateNetwork steinerTree;
	private DijkstraShortestPath<SubstrateNode,SubstrateLink> dijkstra;
	
	public SubstrateNetwork getSteinerTree() {
		return steinerTree;
	}

	public KMB1981(Collection<SubstrateNode> participant,SubstrateNetwork sNet){
		this.participant=new ArrayList<SubstrateNode>(participant);
		this.sNet=sNet;
		this.steinerTree=new SubstrateNetwork();
		dijkstra = new DijkstraShortestPath<SubstrateNode,SubstrateLink>(sNet);
	}
	
	public KMB1981(Collection<SubstrateNode> participant,SubstrateNetwork sNet,Transformer<SubstrateLink, Double> weightTrans){
		this.participant=new ArrayList<SubstrateNode>(participant);
		this.sNet=sNet;
		this.steinerTree=new SubstrateNetwork();
		dijkstra = new DijkstraShortestPath<SubstrateNode,SubstrateLink>(sNet,weightTrans);
	}
	
	public void runSteinerTree(){
		SubstrateNetwork complet = new SubstrateNetwork();
		
		for(SubstrateNode sn1: this.participant){
			for(SubstrateNode sn2: this.participant){
				
				Double cost = (Double) this.dijkstra.getDistance(sn1, sn2);
				double proba = 1-Math.exp(-cost);	//re-build probability, it depends on cost
				SubstrateLink sl = new SubstrateLink(proba);
				complet.addEdge(sl, sn1, sn2);
				
			}
		}
		
		PrimMST prim = new PrimMST(complet);
		SubstrateNetwork tempo=prim.getMST();
		
		for(SubstrateNode sn1:tempo.getVertices()){
			for(SubstrateNode sn2: tempo.getVertices()){
				for(SubstrateLink slink : this.dijkstra.getPath(sn1, sn2)){
					this.steinerTree.addEdge(slink, this.sNet.getEndpoints(slink));
				}
			}
		}
		
		Cycle cycle = new Cycle(this.steinerTree);
		if(cycle.isCyclic()){
			PrimMST prim2 = new PrimMST(this.steinerTree);
			this.steinerTree=prim2.getMST();
		}
		
		
	}
}
