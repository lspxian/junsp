package li.SteinerTree;

import java.util.ArrayList;
import java.util.Collection;
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
	private DijkstraShortestPath<SubstrateNode,SubstrateLink> dijkstra;
	
	public SubstrateNetwork getSteinerTree() {
		return steinerTree;
	}

	public Takahashi(Collection<SubstrateNode> participant,SubstrateNetwork sNet){
		this.participant=new ArrayList<SubstrateNode>(participant);
		this.sNet=sNet;
		this.steinerTree=new SubstrateNetwork();
		dijkstra = new DijkstraShortestPath<SubstrateNode,SubstrateLink>(sNet);
	}
	
	public Takahashi(Collection<SubstrateNode> participant,SubstrateNetwork sNet,Transformer<SubstrateLink, Double> weightTrans){
		this.participant=new ArrayList<SubstrateNode>(participant);
		this.sNet=sNet;
		this.steinerTree=new SubstrateNetwork();
		dijkstra = new DijkstraShortestPath<SubstrateNode,SubstrateLink>(sNet,weightTrans);
	}
	
	public void runSteinerTree(){
		
		List<SubstrateNode> tempoPar = new ArrayList<SubstrateNode>(participant);
		
		Double minCost;
		
		
		int randomStart = new Random().nextInt(tempoPar.size()-1);
		SubstrateNode startNode = tempoPar.get(randomStart);
		this.steinerTree.addVertex(startNode);
		tempoPar.remove(startNode);
		Pair<SubstrateNode> steinerLink=null;
		
		while(!tempoPar.isEmpty()){
			minCost = 100.0;
			
			for(SubstrateNode par : tempoPar){
				for(SubstrateNode snode : this.steinerTree.getVertices()){
					double tempCost = (Double) dijkstra.getDistance(par, snode);
					if(tempCost<minCost){
						minCost = tempCost;
						steinerLink=new Pair<SubstrateNode>(par,snode);
					}
					
				}
			}
			
			tempoPar.remove(steinerLink.getFirst());
			for(SubstrateLink slink : dijkstra.getPath(steinerLink.getFirst(), steinerLink.getSecond())){
				this.steinerTree.addEdge(slink,sNet.getEndpoints(slink));
			}
			
		}
		
	}
}
