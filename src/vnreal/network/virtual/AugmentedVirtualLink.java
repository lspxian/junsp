package vnreal.network.virtual;

import li.multiDomain.Domain;

public class AugmentedVirtualLink extends VirtualLink {
	private VirtualLink originalVL;
	private Domain originalDomain;
	
	public AugmentedVirtualLink(Domain d, VirtualLink ovl){
		this.originalDomain = d;
		this.originalVL = ovl;
	}

	public VirtualLink getOriginalVL() {
		return originalVL;
	}

	public Domain getOriginalDomain() {
		return originalDomain;
	}
	
	public String toString(){
		return "Augmented Virtual Link(" + getId() + ")";
	}
}
