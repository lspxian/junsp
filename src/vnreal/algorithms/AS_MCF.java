package vnreal.algorithms;

import java.util.List;
import java.util.Map;

import li.multiDomain.Domain;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class AS_MCF extends AbstractMultiDomainLinkMapping {

	public AS_MCF(List<Domain> domains){
		super(domains);
	}
	
	@Override
	public boolean linkMapping(VirtualNetwork vNet,
			Map<VirtualNode, SubstrateNode> nodeMapping) {

		for(VirtualLink vlink: vNet.getEdges()){
			SubstrateNode sSource = nodeMapping.get(vNet.getSource(vlink));
			SubstrateNode sDest = nodeMapping.get(vNet.getDest(vlink));
			for(Domain sdomain : domains){
				if(sdomain.containsVertex(sSource)){
					for(Domain ddomain : domains){
						if(ddomain.containsVertex(sDest)){
							
							
							break;
						}
					}
					break;
				}
			}
		}
		
		return false;
	}

}
