package vnreal.algorithms;

import java.util.List;
import java.util.Map;

import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public abstract class AbstractMultiDomainLinkMapping {
	protected List<SubstrateNetwork> multiSNet;
	public abstract boolean linkMapping(VirtualNetwork vNet,
			Map<VirtualNode, SubstrateNode> nodeMapping);
}
