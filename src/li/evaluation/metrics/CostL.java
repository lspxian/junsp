package li.evaluation.metrics;

import java.io.IOException;

import cherif.Simulation;
import vnreal.demands.BandwidthDemand;
import vnreal.demands.CpuDemand;
import vnreal.mapping.Mapping;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNode;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;
import vnreal.resources.CpuResource;

public class CostL extends Metric{
	private static double nodeCost = 0.0, linkCost = 0.0;
	public CostL(Simulation simulation) throws IOException {
		super(simulation);
	}

	@Override
	public String name() {
		return "Cost";
	}
	
	@Override
	public double calculate() {
		CpuDemand tmpCpuDem;
		BandwidthDemand tmpBwDem;
		for (SubstrateLink sl : simulation.getSubstrateNetwork().getEdges()) {
			for (AbstractResource res : sl.get()) {
				if (res instanceof BandwidthResource) {
					for (Mapping f : res.getMappings()) {
						tmpBwDem = (BandwidthDemand) f.getDemand();
						linkCost += tmpBwDem.getDemandedBandwidth();
					}
				}
			}
		}
		for (SubstrateNode sn : simulation.getSubstrateNetwork().getVertices()) {
			for (AbstractResource res : sn.get()) {
				if (res instanceof CpuResource) {
					for (Mapping f : res.getMappings()) {
						tmpCpuDem = (CpuDemand) f.getDemand();
						nodeCost += tmpCpuDem.getDemandedCycles();
					}
				}
			}
		}
		return (nodeCost + linkCost);
	}

}
