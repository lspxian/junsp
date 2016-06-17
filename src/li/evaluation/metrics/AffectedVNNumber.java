package li.evaluation.metrics;

import java.io.IOException;
import java.util.ArrayList;

import li.simulation.AbstractSimulation;
import li.simulation.ProbabilitySimulation;

public class AffectedVNNumber extends Metric {

	public AffectedVNNumber(AbstractSimulation simulation, String method, int lambda) throws IOException {
		super(simulation, method, lambda);
	}

	@Override
	public String name() {
		return "Affected VN Number ";
	}

	@Override
	public double calculate() {
		ProbabilitySimulation tempSim = (ProbabilitySimulation)this.simulation;
		return tempSim.getAffected()/tempSim.getFailures();
	}

}
