package li.evaluation.metrics;

import java.io.IOException;

import li.simulation.AbstractSimulation;

public class ProtectedAcceptedRatio extends Metric {

	public ProtectedAcceptedRatio(AbstractSimulation simulation) throws IOException {
		super(simulation);
	}
	@Override
	public String name() {
		return "Protected_Accepted_Ratio";
	}

	@Override
	public double calculate() {
		return (double)this.simulation.getProtectedVNs()/(double)this.simulation.getAccepted()*100;
	}

}
