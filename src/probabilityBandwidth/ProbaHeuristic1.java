package probabilityBandwidth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.util.Pair;
import vnreal.algorithms.AbstractLinkMapping;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class ProbaHeuristic1 extends AbstractLinkMapping {
	private DijkstraShortestPath<SubstrateNode,SubstrateLink> dijkstra;
	private Map<SubstrateLink, Double> initialProbability;
	private List<VirtualLink> virtualLinks;
	
	protected ProbaHeuristic1(SubstrateNetwork sNet) {
		super(sNet);
		this.initialProbability = new HashMap<SubstrateLink, Double>();
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		this.virtualLinks=new ArrayList<VirtualLink>(vNet.getEdges());
		
		while(!virtualLinks.isEmpty()){
			Pair<SubstrateNode> minPair = null;
			double minCost = 1000;
			VirtualLink minvl=null;
			for(VirtualLink vl : virtualLinks){
				this.dijkstra = new DijkstraShortestPath<SubstrateNode,SubstrateLink> (this.sNet, new ProbaBWCost(vl));
				SubstrateNode snode = nodeMapping.get(vNet.getEndpoints(vl).getFirst());
				SubstrateNode dnode = nodeMapping.get(vNet.getEndpoints(vl).getSecond());
				double tempCost = (double) this.dijkstra.getDistance(snode, dnode);
				if(tempCost<minCost){
					minCost = tempCost;
					minPair = new Pair<SubstrateNode>(snode,dnode);
					minvl = vl;
				}
			}
			
			this.dijkstra = new DijkstraShortestPath<SubstrateNode,SubstrateLink> (this.sNet, new ProbaBWCost(minvl));
			this.virtualLinks.remove(minvl);
			
			for(SubstrateLink sl : this.dijkstra.getPath(minPair.getFirst(),minPair.getSecond())){
				this.initialProbability.put(sl, sl.getProbability());
				sl.setProbability(1.0);
			}

			//update resource
			if(!NodeLinkAssignation.vlmSimple(minvl, dijkstra.getPath(minPair.getFirst(),minPair.getSecond()))){
				throw new AssertionError("But we checked before!");
			}
		}
		
		
		for(Map.Entry<SubstrateLink, Double> entry : this.initialProbability.entrySet()){
			entry.getKey().setProbability(entry.getValue());
		}
		
		return true;
	}

}
