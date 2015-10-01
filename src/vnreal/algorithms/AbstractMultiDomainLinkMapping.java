package vnreal.algorithms;

import java.util.List;
import java.util.Map;

import li.multiDomain.Domain;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public abstract class AbstractMultiDomainLinkMapping {
	protected List<Domain> domains;
	AbstractMultiDomainLinkMapping(List<Domain> domains){
		this.domains = domains;
	}
	public abstract boolean linkMapping(VirtualNetwork vNet,
			Map<VirtualNode, SubstrateNode> nodeMapping);
}
