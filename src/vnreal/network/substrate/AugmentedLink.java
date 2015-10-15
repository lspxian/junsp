package vnreal.network.substrate;

public class AugmentedLink extends SubstrateLink {
	protected SubstrateNode source;
	protected SubstrateNode destination;
	protected double cost;
	
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

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}
	
	public String toString(){
		return "Augmented Link("+ getId() + ")";
	}

	
}
