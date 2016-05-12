package li.evaluation.metrics;

import java.io.IOException;
import java.util.Map;

import li.simulation.SteinerTreeProbabilitySimulation;
import vnreal.network.virtual.VirtualNetwork;

public class AverageProbability extends Metric {

	public AverageProbability(SteinerTreeProbabilitySimulation simulation, String method, int lambda) throws IOException {
		super(simulation, method, lambda);
	}

	@Override
	public String name() {
		return "Average probability";
	}

	@Override
	public double calculate() {
		SteinerTreeProbabilitySimulation sim = (SteinerTreeProbabilitySimulation) this.simulation;
		double average=0.0;
		for(Map.Entry<VirtualNetwork, Double> e : sim.getProbability().entrySet()){
			average = average + e.getValue();
		}
		average = average / sim.getProbability().size();
		return average;
	}

}
