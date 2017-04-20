package li.evaluation.metrics;

import java.io.IOException;

import li.simulation.AbstractSimulation;
import vnreal.network.substrate.SubstrateLink;

public class PrimaryUtilisation extends Metric {
	private double capacity = 0.0,sum = 0.0;
	public PrimaryUtilisation(AbstractSimulation simulation) throws IOException {
		super(simulation);
	}

	@Override
	public String name() {
		return "Primary resource utilisation";
	}

	@Override
	public double calculate() {
		for (SubstrateLink sl : this.simulation.getSubstrateNetwork().getEdges()) {
			capacity+=sl.getBandwidthResource().getPrimaryCap();
			sum+=sl.getBandwidthResource().getOccupiedPrimary();
		}
		return sum/capacity;
	}

}
