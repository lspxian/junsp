package li.SteinerTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.util.Pair;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class KMB1981V2 {
	private List<SubstrateNode> participant;
	private SubstrateNetwork sNet;
	private SubstrateNetwork steinerTree;
	private DijkstraShortestPath<SubstrateNode,SubstrateLink> dijkstra;
	private Map<SubstrateLink, Double> initialProbability;
	
	public SubstrateNetwork getSteinerTree() {
		return steinerTree;
		
	}

	public KMB1981V2(Collection<SubstrateNode> participant,SubstrateNetwork sNet){
		this.participant=new ArrayList<SubstrateNode>(participant);
		this.sNet=sNet;
		this.steinerTree=new SubstrateNetwork();
		dijkstra = new DijkstraShortestPath<SubstrateNode,SubstrateLink>(sNet);
		this.initialProbability = new HashMap<SubstrateLink, Double>();
	}
	
	public KMB1981V2(Collection<SubstrateNode> participant,SubstrateNetwork sNet,Transformer<SubstrateLink, Double> weightTrans){
		this.participant=new ArrayList<SubstrateNode>(participant);
		this.sNet=sNet;
		this.steinerTree=new SubstrateNetwork();
		dijkstra = new DijkstraShortestPath<SubstrateNode,SubstrateLink>(sNet,weightTrans);
		this.initialProbability = new HashMap<SubstrateLink, Double>();
	}
	
	public void runSteinerTree(){
		
		List<SubstrateNode> tempoPar = new ArrayList<SubstrateNode>(participant);
		List<SubstrateNode> tempoParIn = new ArrayList<SubstrateNode>();
		Double minCost;
		
		int randomStart = new Random().nextInt(tempoPar.size()-1);
		SubstrateNode startNode = tempoPar.get(randomStart);
		this.steinerTree.addVertex(startNode);
		tempoParIn.add(startNode);
		tempoPar.remove(startNode);
		Pair<SubstrateNode> steinerLink=null;
		
		while(!tempoPar.isEmpty()){
			minCost = 100.0;
			
			for(SubstrateNode par : tempoPar){
				for(SubstrateNode snode : tempoParIn){
					double tempCost = (Double) dijkstra.getDistance(par, snode);
					if(tempCost<minCost){
						minCost = tempCost;
						steinerLink=new Pair<SubstrateNode>(par,snode);
					}
					
				}
			}
			
			tempoParIn.add(steinerLink.getFirst());
			tempoPar.remove(steinerLink.getFirst());
			for(SubstrateLink slink : dijkstra.getPath(steinerLink.getFirst(), steinerLink.getSecond())){
				if(!this.steinerTree.containsEdge(slink)){
					this.steinerTree.addEdge(slink,sNet.getEndpoints(slink));
					this.initialProbability.put(slink, slink.getProbability());
//					slink.setProbability(0.0);
					slink.setProbability(1e-10);
				}
			}
			
		}
		
		for(Map.Entry<SubstrateLink, Double> entry : this.initialProbability.entrySet()){
			entry.getKey().setProbability(entry.getValue());
		}
	}
		
}
