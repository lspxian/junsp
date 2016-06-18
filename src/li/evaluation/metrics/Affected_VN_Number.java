package li.evaluation.metrics;

import java.io.IOException;
import li.simulation.AbstractSimulation;
import li.simulation.ProbabilitySimulation;

public class Affected_VN_Number extends Metric {

	public Affected_VN_Number(AbstractSimulation simulation, String method, int lambda) throws IOException {
		super(simulation, method, lambda);
	}

	@Override
	public String name() {
		return "Affected_VN_Number ";
	}

	@Override
	public double calculate() {
		ProbabilitySimulation tempSim = (ProbabilitySimulation)this.simulation;
		return (double)tempSim.getAffected()/tempSim.getFailures();
	}

}
