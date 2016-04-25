package li.SteinerTree;

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
	private DijkstraShortestPath<SubstrateNode,SubstrateLink> Dijkstra;
	
	public SubstrateNetwork getSteinerTree() {
		return steinerTree;
	}

	public KMB1981(List<SubstrateNode> participant,SubstrateNetwork sNet){
		this.participant=participant;
		this.sNet=sNet;
		this.steinerTree=new SubstrateNetwork();
		Dijkstra = new DijkstraShortestPath<SubstrateNode,SubstrateLink>(sNet);
	}
	
	public KMB1981(List<SubstrateNode> participant,SubstrateNetwork sNet,Transformer<SubstrateLink, Double> weightTrans){
		this.participant=participant;
		this.sNet=sNet;
		this.steinerTree=new SubstrateNetwork();
		Dijkstra = new DijkstraShortestPath<SubstrateNode,SubstrateLink>(sNet,weightTrans);
	}
	
	public void runSteinerTree(){
		
		
		
	}
}
