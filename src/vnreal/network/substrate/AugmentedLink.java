package vnreal.network.substrate;

public class AugmentedLink extends SubstrateLink {
	protected SubstrateNode source;
	protected SubstrateNode destination;
	protected double price;
	
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

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
	
	
}
