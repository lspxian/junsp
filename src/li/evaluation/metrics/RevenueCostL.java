package li.evaluation.metrics;

import java.io.IOException;

import cherif.Simulation;

public class RevenueCostL extends Metric{
	private boolean isPathSplitting;

	public RevenueCostL(Simulation simulation, boolean isPsAlgorithm) throws IOException {
		super(simulation);
		this.isPathSplitting = isPsAlgorithm;
	}

	@Override
	public String name() {
		return "RevenueCost";
	}

	@Override
	public double calculate() {
		double RevenueCost=0.0;
		try {
			
			CostL cost = new CostL(simulation);
			MappedRevenueL rev = new MappedRevenueL(simulation,isPathSplitting);
			RevenueCost = rev.calculate() / cost.calculate();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return (RevenueCost);
	}
}
