package vnreal.network.substrate;

public class AugmentedLink extends SubstrateLink {
	protected SubstrateNode source;
	protected SubstrateNode destination;
	
	public AugmentedLink() {
		super();
	}

	public SubstrateNode getSource() {
		return source;
	}

	public void setSource(SubstrateNode source) {
		this.source = source;
	}

	public SubstrateNode getDestination() {
		return destination;
	}

	public void setDestination(SubstrateNode destination) {
		this.destination = destination;
	}

	public String toString(){
		return "Augmented Link("+ getId() + ")";
	}

	
}
