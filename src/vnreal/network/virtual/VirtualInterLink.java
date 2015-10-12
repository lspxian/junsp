package vnreal.network.virtual;

import vnreal.demands.AbstractDemand;
import li.multiDomain.Domain;

public class VirtualInterLink extends VirtualLink {
	protected Domain sDomain;
	protected Domain dDomain;

	public VirtualInterLink(int layer) {
		super(layer);
	}
	
	public VirtualInterLink(VirtualLink vl, Domain sDomain, Domain dDomain){
		super(1);
		for (AbstractDemand d : vl) {
			this.add(d.getCopy(this));
		}
		this.sDomain = sDomain;
		this.dDomain = dDomain;
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
		return super.toString()+"@[("+this.sDomain.getCoordinateX()+","+this.sDomain.getCoordinateY()
				+")->("+this.dDomain.getCoordinateX()+","+this.dDomain.getCoordinateY()+")] ";
	}
}
