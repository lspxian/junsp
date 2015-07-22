package li.evaluation.metrics;

import java.io.IOException;

import cherif.Simulation;


public class CostRevenueL extends Metric{
	
	private boolean isPathSplitting;
	private static double CostRevenue=0.0;
	public CostRevenueL(Simulation simulation, boolean isPsAlgorithm) throws IOException {
		super(simulation);
		this.isPathSplitting = isPsAlgorithm;
	}

	@Override
	public String name() {
		return "CostRevenue";
	}

	@Override
	public double calculate() {
	//	double CostRevenue=0.0;
		try {
			
			CostL cost = new CostL(simulation);
			MappedRevenueL rev = new MappedRevenueL(simulation,isPathSplitting);
			CostRevenue = cost.calculate() / rev.calculate();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return (CostRevenue);
	}

}
