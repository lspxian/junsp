package li.evaluation.metrics;

import java.io.IOException;

import li.simulation.AbstractSimulation;
import vnreal.demands.BandwidthDemand;
import vnreal.demands.CpuDemand;
import vnreal.mapping.Mapping;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNode;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;
import vnreal.resources.CpuResource;

public class CostL extends Metric{
	double nodeCost = 0.0, linkCost = 0.0;
	public CostL(AbstractSimulation simulation) throws IOException {
		super(simulation);
	}
	public CostL(AbstractSimulation simulation, String method) throws IOException {
		super(simulation, method);
	}
	public CostL(AbstractSimulation simulation, String method, int lambda) throws IOException{
		super(simulation, method, lambda);
	}
	@Override
	public String name() {
		return "Cost";
	}

	@Override
	public double calculate() {
		return simulation.getTotalCost();
	}
}
