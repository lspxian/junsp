package li.evaluation.metrics;

import java.io.IOException;

import li.simulation.AbstractSimulation;


public class CostRevenueL extends Metric{
	
	public CostRevenueL(AbstractSimulation simulation, String method) throws IOException {
		super(simulation, method);
	}
	public CostRevenueL(AbstractSimulation simulation, String method, int lambda) throws IOException{
		super(simulation, method, lambda);
	}
	
	@Override
	public String name() {
		return "CostRevenue";
	}

	@Override
	public double calculate() {
		try {
			CostL cost= new CostL(simulation);
			MappedRevenueL rev = new MappedRevenueL(simulation);
			return cost.calculate() / rev.calculate();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return 0;
	}

}
