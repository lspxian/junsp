package vnreal.network.virtual;

import li.multiDomain.Domain;

public class VirtualInterLink extends VirtualLink {
	protected Domain sDomain;
	protected Domain dDomain;

	public VirtualInterLink(int layer) {
		super(layer);
	}

	public Domain getsDomain() {
		return sDomain;
	}

	public void setsDomain(Domain sDomain) {
		this.sDomain = sDomain;
	}

	public Domain getdDomain() {
		return dDomain;
	}

	public void setdDomain(Domain dDomain) {
		this.dDomain = dDomain;
	}

	@Override
	public String toString(){
		return super.toString()+"@("+this.sDomain.getId()+"->"+this.dDomain.getId()+") ";
	}
}
