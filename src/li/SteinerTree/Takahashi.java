package li.SteinerTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.util.Pair;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class Takahashi {
	private List<SubstrateNode> participant;
	private SubstrateNetwork sNet;
	private SubstrateNetwork steinerTree;
	DijkstraShortestPath<SubstrateNode,SubstrateLink> Dijkstra;
	
	public SubstrateNetwork getSteinerTree() {
		return steinerTree;
	}

	public Takahashi(List<SubstrateNode> participant,SubstrateNetwork sNet){
		this.participant=participant;
		this.sNet=sNet;
		this.steinerTree=new SubstrateNetwork();
		Dijkstra = new DijkstraShortestPath<SubstrateNode,SubstrateLink>(sNet);
	}
	
	public Takahashi(List<SubstrateNode> participant,SubstrateNetwork sNet,Transformer<SubstrateLink, Double> weightTrans){
		this.participant=participant;
		this.sNet=sNet;
		this.steinerTree=new SubstrateNetwork();
		Dijkstra = new DijkstraShortestPath<SubstrateNode,SubstrateLink>(sNet,weightTrans);
	}
	
	public void runSteinerTree(){
		
		List<SubstrateNode> tempoPar = new ArrayList<SubstrateNode>(participant);
		
		Double minCost;
		
		
		int randomStart = new Random().nextInt(sNet.getVertexCount());
		SubstrateNode startNode = participant.get(randomStart);
		this.steinerTree.addVertex(startNode);
		Pair<SubstrateNode> steinerLink=null;
		
		while(!tempoPar.isEmpty()){
			minCost = 100.0;
			
			for(SubstrateNode par : tempoPar){
				for(SubstrateNode snode : this.steinerTree.getVertices()){
					double tempCost = (Double) Dijkstra.getDistance(par, snode);
					if(tempCost<minCost){
						minCost = tempCost;
						steinerLink=new Pair<SubstrateNode>(par,snode);
					}
					
				}
			}
			
			tempoPar.remove(steinerLink.getFirst());
			for(SubstrateLink slink : Dijkstra.getPath(steinerLink.getFirst(), steinerLink.getSecond())){
				this.steinerTree.addEdge(slink,sNet.getEndpoints(slink));
			}
			
		}
		
	}
}
