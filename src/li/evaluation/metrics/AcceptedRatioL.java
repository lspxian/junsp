package li.evaluation.metrics;

import java.io.IOException;

import li.simulation.AbstractSimulation;

public class AcceptedRatioL extends Metric {

	public AcceptedRatioL(AbstractSimulation simulation) throws IOException {
		super(simulation);
	}
	
	public AcceptedRatioL(AbstractSimulation simulation, String method) throws IOException{
		super(simulation, method);
	}
	
	public AcceptedRatioL(AbstractSimulation simulation, String method, int lambda) throws IOException{
		super(simulation, method, lambda);
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
