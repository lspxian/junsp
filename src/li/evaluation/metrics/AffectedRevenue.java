package li.evaluation.metrics;

import java.io.IOException;

import li.simulation.AbstractSimulation;

public class AffectedRevenue extends Metric{

	public AffectedRevenue(AbstractSimulation simulation)throws IOException{
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
		return this.simulation.getAffectedRevenue()/this.simulation.getFailures();
	}

}
