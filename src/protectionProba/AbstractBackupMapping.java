package protectionProba;

import java.util.Map;

import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualNetwork;

public abstract class AbstractBackupMapping {
	protected SubstrateNetwork sNet;
	protected double probability;
	
	protected AbstractBackupMapping(SubstrateNetwork sNet) {
		this.sNet = sNet;
	}
	public double getProbability() {
		return probability;
	}
	
	public abstract boolean linkMapping(VirtualNetwork vNet,Map<BandwidthDemand, SubstrateLink> primary);

}
