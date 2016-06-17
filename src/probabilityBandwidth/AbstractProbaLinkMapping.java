package probabilityBandwidth;

import java.util.Map;

import vnreal.algorithms.AbstractLinkMapping;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public abstract class AbstractProbaLinkMapping extends AbstractLinkMapping {

	protected double probability;
	public AbstractProbaLinkMapping(SubstrateNetwork sNet) {
		super(sNet);
	}
	public double getProbability() {
		return probability;
	}

	@Override
	public abstract boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping); 
}
