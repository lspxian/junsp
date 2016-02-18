package li.multiDomain.metrics;

import java.io.IOException;
import java.util.ArrayList;

import li.multiDomain.Domain;
import li.simulation.MultiDomainSimulation;
import vnreal.demands.BandwidthDemand;
import vnreal.demands.CpuDemand;
import vnreal.mapping.Mapping;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;
import vnreal.resources.CpuResource;

public class CostMD extends MetricMD {
	double nodeCost = 0.0, linkCost = 0.0;
	public CostMD(MultiDomainSimulation simulation) throws IOException {
		super(simulation);
	}
	public CostMD(MultiDomainSimulation simulation, String method) throws IOException {
		super(simulation, method);
	}
	public CostMD(MultiDomainSimulation simulation, String method, int lambda) throws IOException{
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
