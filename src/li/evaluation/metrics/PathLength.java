package li.evaluation.metrics;

import java.io.IOException;

import li.simulation.AbstractSimulation;

public class PathLength extends Metric{
	public PathLength(AbstractSimulation simulation) throws IOException {
		super(simulation);
	}
	public PathLength(AbstractSimulation simulation, String method, int lambda) throws IOException{
		super(simulation, method, lambda);
	}

	@Override
	public String name() {
		return "PathLength";
	}

	@Override
	public double calculate() {
		
		return 0;
	}
}
