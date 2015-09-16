package li.evaluation.metrics;

import java.io.IOException;

import li.simulation.Simulation;

public class AcceptedRatioL extends Metric {

	public AcceptedRatioL(Simulation simulation) throws IOException {
		super(simulation);
	}

	@Override
	public double calculate() {
		return ((double)this.simulation.getAccepted()/(double)(this.simulation.getAccepted() + this.simulation.getRejected()))*100.0;
	}

	@Override
	public String name() {
		return "AcceptedRatio";
	}

}
