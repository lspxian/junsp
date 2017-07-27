package li.evaluation.metrics;

import java.io.IOException;

import li.simulation.AbstractSimulation;

public class ProtectedAllRatio extends Metric {

	public ProtectedAllRatio(AbstractSimulation simulation) throws IOException {
		super(simulation);
	}

	@Override
	public String name() {
		return "Protected_All_Ratio";
	}

	@Override
	public double calculate() {
		return ((double)this.simulation.getProtectedVNs()/(double)(this.simulation.getAccepted() + this.simulation.getRejected()))*100.0;
	}

}
