package vnreal.algorithms.linkmapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mulavito.algorithms.shortestpath.ksp.Yen;
import vnreal.algorithms.AbstractLinkMapping;
import vnreal.algorithms.utils.LinkWeight;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class KShortestPath extends AbstractLinkMapping{
	int k=5;

	public KShortestPath(SubstrateNetwork sNet) {
		super(sNet);
	}
	
	public KShortestPath(SubstrateNetwork sNet, int k){
		super(sNet);
		this.k=k;
	}
	
	/**
	 * find the k shortest paths without considering the bandwidth constraint, the bandwidth here is just the link weight.
	 * After finding the K shortest paths, adopt the first one that follows the bandwidth constraint.
	 */
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping){
		Map<VirtualLink,List<SubstrateLink>> result = new HashMap<VirtualLink,List<SubstrateLink>>();
		LinkWeight linkWeight = new LinkWeight();
		Yen<SubstrateNode, SubstrateLink> yen = new Yen(sNet,linkWeight);
		
		for(VirtualLink vLink:vNet.getEdges()){
			SubstrateNode srcNode = nodeMapping.get(vNet.getEndpoints(vLink).getFirst());
			SubstrateNode dstNode = nodeMapping.get(vNet.getEndpoints(vLink).getSecond());
			if(!srcNode.equals(dstNode)){
				List<List<SubstrateLink>> ksp = yen.getShortestPaths(srcNode, dstNode, k);
				
				for (List<SubstrateLink> path : ksp) {
					if (NodeLinkAssignation.verifyPathSimple(vLink, path)){
						result.put(vLink, path);
						if (!NodeLinkAssignation.vlmSimple(vLink, path)) //attribute the bw 
							throw new AssertionError("But we checked before!");
						break;
					}
					//if arrive the end , not feasible resource, free all resource and return false
					if(path.equals(ksp.get(ksp.size()-1))){
						for(Map.Entry<VirtualLink, List<SubstrateLink>> entry: result.entrySet()){
							NodeLinkDeletion.linkFree(entry.getKey(), entry.getValue());
						}
						return false;
					}
				}	
				
			}
		}
		
		return true;
	}

}
