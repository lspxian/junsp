package li.evaluation.metrics;

import java.io.IOException;
import java.util.ArrayList;

import li.simulation.AbstractSimulation;
import li.simulation.ProbabilitySimulation;

public class AffectedVNRatio extends Metric {

	public AffectedVNRatio(AbstractSimulation simulation, String method, int lambda) throws IOException {
		super(simulation, method, lambda);
	}

	@Override
	public String name() {
		return "Affected VN Ratio ";
	}

	@Override
	public double calculate() {
		ProbabilitySimulation tempSim = (ProbabilitySimulation)this.simulation;
		ArrayList<Double> ratios = tempSim.getAffectedRatio();
		double sum = 0.0;
		for(double ratio : ratios){
			sum=sum + ratio;
		}
		return sum/ratios.size();
	}

}
