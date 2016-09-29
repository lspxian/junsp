package li.evaluation.metrics;

import java.io.IOException;
import java.util.ArrayList;

import li.simulation.AbstractSimulation;
import vnreal.network.substrate.SubstrateLink;

public class PrimaryPercentage extends Metric {
	private ArrayList<Double> percentage=new ArrayList<Double>();
	public PrimaryPercentage(AbstractSimulation simulation) throws IOException {
		super(simulation);
	}

	@Override
	public String name() {
		return "Link primary resource percentage";
	}

	@Override
	public double calculate() {
		double sum=0.0, primary=0.0;
		for (SubstrateLink sl : this.simulation.getSubstrateNetwork().getEdges()) {
			sum+=sl.getBandwidthResource().getOccupiedBandwidth();
			primary+=sl.getBandwidthResource().getPrimaryBw();
		}
		percentage.add(primary/sum);
		
		double tmp=0.0;
		for(Double per:percentage)
			tmp+=per;
		return tmp/percentage.size();
	}

}
