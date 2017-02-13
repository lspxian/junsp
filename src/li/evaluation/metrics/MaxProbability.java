package li.evaluation.metrics;
import java.io.IOException;
import java.util.Map;

import li.simulation.AbstractSimulation;
import vnreal.network.virtual.VirtualNetwork;

public class MaxProbability extends Metric {

	public MaxProbability(AbstractSimulation simulation) throws IOException {
		super(simulation);
	}

	@Override
	public String name() {
		return "Maximum Probability";
	}

	@Override
	public double calculate() {
		double max=0.0;
		for(Map.Entry<VirtualNetwork, Double> e : this.simulation.getProbability().entrySet()){
			if(max<e.getValue())
				max=e.getValue();
		}
		return max;
	}

}
