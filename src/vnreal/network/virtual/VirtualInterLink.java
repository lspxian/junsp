package vnreal.network.virtual;

import vnreal.demands.AbstractDemand;
import li.multiDomain.Domain;

public class VirtualInterLink extends VirtualLink {
	protected VirtualNode node1;
	protected VirtualNode node2;

	public VirtualInterLink(int layer) {
		super(layer);
	}
	
	public VirtualInterLink(VirtualLink vl,VirtualNode n1, VirtualNode n2){
		super(1);
		for (AbstractDemand d : vl) {
			this.add(d.getCopy(this));
		}
		this.node1 = n1;
		this.node2 = n2;
	}

	public VirtualNode getNode1() {
		return node1;
	}

	public VirtualNode getNode2() {
		return node2;
	}

	@Override
	public String toString(){
		return "Virtual Inter Link(" + getId() + ")"+"@[("+this.node1.getDomain().getCoordinateX()+","+this.node1.getDomain().getCoordinateY()
				+")<->("+this.node2.getDomain().getCoordinateX()+","+this.node2.getDomain().getCoordinateY()+")] ";
	}
}
