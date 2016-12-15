package li.evaluation.metrics;

import java.io.IOException;
import java.util.Map;

import li.simulation.AbstractSimulation;
import vnreal.network.virtual.VirtualNetwork;

public class AverageProbability extends Metric {

	public AverageProbability(AbstractSimulation simulation)throws IOException{
		super(simulation);
	}
	
	public AverageProbability(AbstractSimulation simulation, String method, int lambda) throws IOException {
		super(simulation, method, lambda);
	}

	@Override
	public String name() {
		return "Average_Probability";
	}

	@Override
	public double calculate() {
		double average=0.0;
		for(Map.Entry<VirtualNetwork, Double> e : this.simulation.getProbability().entrySet()){
			average = average + e.getValue();
		}
		average = average / this.simulation.getProbability().size();
		return average;
	}

}
