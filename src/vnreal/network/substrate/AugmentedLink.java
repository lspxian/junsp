package vnreal.network.substrate;

import vnreal.network.virtual.VirtualNode;

public class AugmentedLink extends SubstrateLink {
	protected VirtualNode destNode;
	
	public AugmentedLink(VirtualNode node) {
		super();
		this.destNode = node;
	}

	public VirtualNode getDestNode() {
		return destNode;
	}

	public String toString(){
		return "Augmented Link("+ getId() + ")"+" dest:"+destNode.getId();
	}

	
}
