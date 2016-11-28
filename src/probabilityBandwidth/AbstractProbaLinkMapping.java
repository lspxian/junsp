package probabilityBandwidth;

import java.util.HashMap;
import java.util.Map;

import vnreal.algorithms.AbstractLinkMapping;
import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public abstract class AbstractProbaLinkMapping extends AbstractLinkMapping {

	protected double probability;
	//in primary: mapping is used to store result
	protected HashMap<BandwidthDemand, SubstrateLink> mapping;
	public AbstractProbaLinkMapping(SubstrateNetwork sNet) {
		super(sNet);
	}
	public double getProbability() {
		return probability;
	}
	public HashMap<BandwidthDemand, SubstrateLink> getMapping() {
		return mapping;
	}

	
}
