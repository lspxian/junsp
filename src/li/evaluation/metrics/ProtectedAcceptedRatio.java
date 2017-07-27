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
		double res=(double)this.simulation.getProtectedVNs()/(double)this.simulation.getAccepted()*100;
		if(res>100)
			System.out.println(" ");
		return res;
	}

}
