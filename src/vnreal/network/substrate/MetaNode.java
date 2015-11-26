package vnreal.network.substrate;

import java.util.Random;

import vnreal.network.Node;
import vnreal.resources.AbstractResource;
import vnreal.resources.CpuResource;

public class MetaNode extends Node<AbstractResource> {
	public MetaNode() {
		super();
	}

	public MetaNode getCopy() {
		MetaNode clone = new MetaNode();
		clone.setName(getName());

		for (AbstractResource r : this) {
			clone.add(r.getCopy(clone));
		}

		return clone;
	}

	@Override
	public String toString() {
		return "MetaNode(" + getId() + ")";
	}

	@Override
	public String toStringShort() {
		return "MN(" + getId() + ")";
	}
	
/**
 * 
 * @param random 
 * @return
 */
	public boolean addResource(boolean random){
		CpuResource cpu = new CpuResource(this);
		double quantity = 100;
		if(random)
			quantity = 50+ new Random().nextDouble()*(100-50);
		cpu.setCycles(quantity);
		if(this.preAddCheck(cpu)){
			this.add(cpu);
			return true;
		}
		return false;
	}

}
