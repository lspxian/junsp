package vnreal.network.substrate;

public class AugmentedNetwork extends SubstrateNetwork {
	protected SubstrateNetwork root;

	public AugmentedNetwork() {
		super();
		// TODO Auto-generated constructor stub
	}

	public AugmentedNetwork(boolean autoUnregisterConstraints, boolean directed) {
		super(autoUnregisterConstraints, directed);
		// TODO Auto-generated constructor stub
	}

	public AugmentedNetwork(boolean autoUnregisterConstraints) {
		super(autoUnregisterConstraints);
		// TODO Auto-generated constructor stub
	}

	public SubstrateNetwork getRoot() {
		return root;
	}

	public void setRoot(SubstrateNetwork root) {
		this.root = root;
	}
	
	
}
