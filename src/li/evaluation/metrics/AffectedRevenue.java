package li.evaluation.metrics;

import java.io.IOException;

import li.simulation.AbstractSimulation;
import li.simulation.ProbabilitySimulation;

public class AffectedRevenue extends Metric{

	public AffectedRevenue(ProbabilitySimulation simulation)throws IOException{
		super(simulation);
	}
	
	public AffectedRevenue(AbstractSimulation simulation, String method, int lambda) throws IOException {
		super(simulation, method, lambda);
	}

	@Override
	public String name() {
		return "Affected_Revenue";
	}

	@Override
	public double calculate() {
		ProbabilitySimulation tempSim = (ProbabilitySimulation)this.simulation;
		return tempSim.getAffectedRevenue()/tempSim.getFailures();
	}

}
