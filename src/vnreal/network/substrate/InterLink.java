package vnreal.network.substrate;

import java.util.Random;

import li.multiDomain.Domain;
import vnreal.resources.AbstractResource;

public class InterLink extends SubstrateLink {
	protected SubstrateNode interior;
	protected SubstrateNode exterior;
	protected Domain exterDomain;
	
	public InterLink() {
		super();
	}
	
	public InterLink(SubstrateNode interior, SubstrateNode exterior, Domain exterDomain, boolean randomResource){
		this();
		this.interior = interior;
		this.exterior = exterior;
		this.exterDomain = exterDomain;
		if(randomResource)	this.addResource(new Random().nextDouble());
		else this.addResource(1.0);
	}
	
	public InterLink(SubstrateLink sl,SubstrateNode interior, SubstrateNode exterior, Domain exterDomain){
		super();
		this.setName(sl.getName());
		for (AbstractResource r : sl) {
			this.add(r.getCopy(this));
		}
		this.interior = interior;
		this.exterior = exterior;
		this.exterDomain = exterDomain;
	}
	

	public SubstrateNode getInterior() {
		return interior;
	}

	public SubstrateNode getExterior() {
		return exterior;
	}

	public Domain getExterDomain() {
		return exterDomain;
	}

	@Override
	public String toString() {
		return "Inter Link(" + getId() + ") to domain("+this.exterDomain.getCoordinateX()
			+","+this.exterDomain.getCoordinateY()+") ";
		
	}
}
