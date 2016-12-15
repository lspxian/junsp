package li.evaluation.metrics;

import java.io.IOException;
import java.util.ArrayList;

import li.simulation.AbstractSimulation;

public class AverageAffectedVNRatio extends Metric {

	public AverageAffectedVNRatio(AbstractSimulation simulation)throws IOException{
		super(simulation);
	}
	
	public AverageAffectedVNRatio(AbstractSimulation simulation, String method, int lambda) throws IOException {
		super(simulation, method, lambda);
	}

	@Override
	public String name() {
		return "Average_Affected_VN_Ratio ";
	}

	@Override
	public double calculate() {
		ArrayList<Double> ratios = this.simulation.getAffectedRatio();
		double sum = 0.0;
		for(double ratio : ratios){
			sum=sum + ratio;
		}
		return sum/ratios.size();
	}

}
