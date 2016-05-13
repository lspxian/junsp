package probabilityBandwidth;

import java.util.Comparator;
import java.util.Map;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class PbbwComparator implements Comparator<VirtualLink> {
	DijkstraShortestPath<SubstrateNode,SubstrateLink>  dijkstra;
	Map<VirtualNode, SubstrateNode> nodeMapping;
	VirtualNetwork vNet;
	
	public PbbwComparator(DijkstraShortestPath<SubstrateNode,SubstrateLink>  dijkstra,
			Map<VirtualNode, SubstrateNode> nodeMapping,
			VirtualNetwork vNet){
		this.dijkstra=dijkstra;
		this.nodeMapping=nodeMapping;
		this.vNet=vNet;
	}
	
	@Override
	public int compare(VirtualLink o1, VirtualLink o2) {
		SubstrateNode snode = nodeMapping.get(vNet.getEndpoints(o1).getFirst());
		SubstrateNode dnode = nodeMapping.get(vNet.getEndpoints(o1).getSecond());
		double cost1 = (double) this.dijkstra.getDistance(snode, dnode);
		
		snode = nodeMapping.get(vNet.getEndpoints(o2).getFirst());
		dnode = nodeMapping.get(vNet.getEndpoints(o2).getSecond());
		double cost2 = (double) this.dijkstra.getDistance(snode, dnode);
		
		if(cost1>cost2)
			return 1;
		else if(cost1<cost2) 
			return -1;
		else return 0;
	}

}
