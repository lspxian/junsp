package vnreal.network.substrate;

import java.util.Random;

import li.multiDomain.Domain;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;

public class InterLink extends SubstrateLink{
	protected SubstrateNode node1;
	protected SubstrateNode node2;
	
	public InterLink(){
		super();
	}
	
	public InterLink(SubstrateNode n1, SubstrateNode n2, boolean randomResource){
		this();
		this.node1 = n1;
		this.node2 = n2;
		if(randomResource) this.addResource(new Random().nextDouble());
		else this.addResource(1.0);
	}
	
	public InterLink(SubstrateLink sl, SubstrateNode n1, SubstrateNode n2){
		super();
		this.setName(sl.getName());
		for (AbstractResource r : sl) {
			this.add(r.getCopy(this));
		}
		this.node1 = n1;
		this.node2 = n2;
	}
	
	//for deep copy in Domain
	public InterLink(InterLink sl, SubstrateNode n1, SubstrateNode n2){
		super();
		this.setName(sl.getName());
		for (AbstractResource r : sl) {
			this.add(r.getCopy(this));
		}
		this.node1 = n1;
		this.node2 = n2;
	}

	public SubstrateNode getNode1() {
		return node1;
	}

	public void setNode1(SubstrateNode node1) {
		this.node1 = node1;
	}

	public SubstrateNode getNode2() {
		return node2;
	}

	public void setNode2(SubstrateNode node2) {
		this.node2 = node2;
	}
	
	@Override
	public String toString() {
		return "Inter Link(" + getId() +") "+"("+node1.getId()+"<->"+node2.getId()+")";
		
	}
	
	public InterLink getCopy(SubstrateNode snode1, SubstrateNode snode2) {
		InterLink clone = new InterLink();
		clone.setNode1(snode1);
		clone.setNode2(snode2);
		clone.setName(getName());

		for (AbstractResource r : this) {
			clone.add(r.getCopy(clone));
		}

		return clone;
	}
	
	@Override
	public boolean addResource(double random){
		BandwidthResource bw=new BandwidthResource(this);
		bw.setBandwidth(MiscelFunctions.roundThreeDecimals(200+random*100));
		this.add(bw);
		return true;
	}
}

