package li.multiDomain;

import vnreal.network.substrate.SubstrateNetwork;

/*
 * 
 * 
 */
public class Domain {

	private SubstrateNetwork substrateNetwork;

	Domain(SubstrateNetwork sn){
		this.substrateNetwork = sn;
	}

	public SubstrateNetwork getSubstrateNetwork() {
		return substrateNetwork;
	}

	public void setSubstrateNetwork(SubstrateNetwork substrateNetwork) {
		this.substrateNetwork = substrateNetwork;
	}
	
	
}

