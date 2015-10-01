package vnreal.network.substrate;

import li.multiDomain.Domain;
import vnreal.resources.AbstractResource;

public class InterLink extends SubstrateLink {
	protected SubstrateNode source;
	protected SubstrateNode destination;
	protected Domain destDomain;
	
	public InterLink() {
		super();
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
		return "InterLink(" + getId() + ")";
	}
}
