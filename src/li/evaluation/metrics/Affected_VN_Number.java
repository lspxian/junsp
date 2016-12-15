package li.evaluation.metrics;

import java.io.IOException;
import li.simulation.AbstractSimulation;

public class Affected_VN_Number extends Metric {

	public Affected_VN_Number(AbstractSimulation simulation)throws IOException{
		super(simulation);
	}
	
	public Affected_VN_Number(AbstractSimulation simulation, String method, int lambda) throws IOException {
		super(simulation, method, lambda);
	}

	@Override
	public String name() {
		return "Affected_VN_Number ";
	}

	@Override
	public double calculate() {
		return (double)this.simulation.getAffected()/this.simulation.getFailures();
	}

}
