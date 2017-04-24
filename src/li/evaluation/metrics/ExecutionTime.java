package li.evaluation.metrics;

import java.io.IOException;

import li.simulation.AbstractSimulation;

public class ExecutionTime extends Metric {

	public ExecutionTime(AbstractSimulation simulation) throws IOException {
		super(simulation);
	}

	@Override
	public String name() {
		return "Execution time";
	}

	@Override
	public double calculate() {
		return this.simulation.getTimeDifference();
	}

}
