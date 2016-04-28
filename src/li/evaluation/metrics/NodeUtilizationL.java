package li.evaluation.metrics;

import java.io.IOException;

import li.simulation.AbstractSimulation;
import vnreal.demands.AbstractDemand;
import vnreal.demands.CpuDemand;
import vnreal.mapping.Mapping;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.resources.AbstractResource;
import vnreal.resources.CpuResource;

public class NodeUtilizationL extends Metric{
	public static double capacity = 0.0,sum=0.0;
	public NodeUtilizationL(AbstractSimulation simulation) throws IOException {
		super(simulation);
	}
	
	@Override
	public double calculate() {
		SubstrateNetwork sNetwork = this.simulation.getSubstrateNetwork();
		for (SubstrateNode sn : sNetwork.getVertices()) {
			for (AbstractResource res : sn.get()) {
				capacity += ((CpuResource) res).getCycles();
			}
		}
		for (SubstrateNode sn : sNetwork.getVertices()) {
			for (AbstractResource res : sn.get()) {
				for (Mapping m : res.getMappings()) {
					AbstractDemand dem = m.getDemand();

					if (dem instanceof CpuDemand) {
						sum += ((CpuDemand) dem).getDemandedCycles();
						
					}
				}
			}
		}
		return sum/capacity;
	}
		
	@Override
	public String name() {
		return "NodeUtilization";
	}
}
