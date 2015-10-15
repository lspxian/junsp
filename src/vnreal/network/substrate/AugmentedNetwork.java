package vnreal.network.substrate;

import li.multiDomain.Domain;

public class AugmentedNetwork extends SubstrateNetwork {
	protected SubstrateNetwork root;

	public AugmentedNetwork() {
		super();
	}
	
	public AugmentedNetwork(Domain domain){
		super();
		this.copy(domain);
		this.root = domain;
	}


	public SubstrateNetwork getRoot() {
		return root;
	}

	public void setRoot(SubstrateNetwork root) {
		this.root = root;
	}
	

	
}
