package li.evaluation.metrics;

import java.io.IOException;

import li.simulation.AbstractSimulation;
import li.simulation.ProbabilitySimulation;

public class AffectedRevenue extends Metric{

	public AffectedRevenue(AbstractSimulation simulation, String method, int lambda) throws IOException {
		super(simulation, method, lambda);
	}

	@Override
	public String name() {
		return "Affected revenue";
	}

	@Override
	public double calculate() {
		ProbabilitySimulation tempSim = (ProbabilitySimulation)this.simulation;
		return tempSim.getAffectedRevenue()/tempSim.getFailures();
	}

}
