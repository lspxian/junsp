package vnreal.algorithms.linkmapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mulavito.algorithms.shortestpath.ksp.Yen;
import vnreal.algorithms.AbstractLinkMapping;
import vnreal.algorithms.utils.LinkWeight;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class KShortestPath extends AbstractLinkMapping{

	public KShortestPath(SubstrateNetwork sNet) {
		super(sNet);
		
	}
	
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping){
		Map<VirtualLink,List<SubstrateLink>> result = new HashMap<VirtualLink,List<SubstrateLink>>();
		LinkWeight linkWeight = new LinkWeight();
		Yen<SubstrateNode, SubstrateLink> yen = new Yen(sNet,linkWeight);
		
		for(VirtualLink vLink:vNet.getEdges()){
			SubstrateNode srcNode = nodeMapping.get(vNet.getSource(vLink));
			SubstrateNode dstNode = nodeMapping.get(vNet.getDest(vLink));
			if(!srcNode.equals(dstNode)){
				List<List<SubstrateLink>> ksp = yen.getShortestPaths(srcNode, dstNode, 5);
				
				for (List<SubstrateLink> path : ksp) {
					if (NodeLinkAssignation.verifyPathSimple(vLink, path)){
						result.put(vLink, path);
						break;
					}
					//if arrive the end , not feasible resource, return false
					if(path.equals(ksp.get(ksp.size()-1)))
						return false;
				}	
				
			}
		}
		
		//attribute the bw after all the demands are verified
		for(Map.Entry<VirtualLink, List<SubstrateLink>> entry: result.entrySet()){
			if (!NodeLinkAssignation.vlmSimple(entry.getKey(), entry.getValue()))
				throw new AssertionError("But we checked before!");
		}
		
		return true;
	}

}
