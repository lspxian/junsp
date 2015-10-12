package vnreal.network.substrate;

import java.util.Random;

import li.multiDomain.Domain;
import vnreal.resources.AbstractResource;

public class InterLink extends SubstrateLink {
	protected SubstrateNode source;
	protected SubstrateNode destination;
	protected Domain destDomain;
	
	public InterLink() {
		super();
	}
	
	public InterLink(SubstrateNode source, SubstrateNode dest, Domain destDomain, boolean randomResource){
		this();
		this.source = source;
		this.destination = dest;
		this.destDomain = destDomain;
		if(randomResource)	this.addResource(new Random().nextDouble());
		else this.addResource(1.0);
	}
	
	public InterLink(SubstrateLink sl,SubstrateNode source, SubstrateNode dest, Domain destDomain){
		super();
		this.setName(sl.getName());
		for (AbstractResource r : sl) {
			this.add(r.getCopy(this));
		}
		this.source = source;
		this.destination = dest;
		this.destDomain = destDomain;
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
	
	public Domain getDestDomain() {
		return destDomain;
	}
	public void setDestDomain(Domain destDomain) {
		this.destDomain = destDomain;
	}
	@Override
	public String toString() {
		String result = "InterLink with domain ("+this.destDomain.getCoordinateX()
		+","+this.destDomain.getCoordinateY()+"),node@["
				+this.source.getId()+ "<->"+this.destination.getId()+"]\n";
		for (AbstractResource r : this.get()) {
			result += "  " + r.toString() + "\n";
		}
		return result;
		//return "InterLink[" + getId() + ")";
	}
}
