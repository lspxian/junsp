package li.evaluation.metrics;

import java.io.IOException;

import cherif.Simulation;


public class CostPerMappedNetworkL extends Metric{
	public CostPerMappedNetworkL(Simulation simulation) throws IOException {
		super(simulation);
	}

	@Override
	public String name() {
		return "CostPerMapped";
	}
	
	@Override
	public double calculate() {
		double res=0.0;
		CostL cost;
		try {
			cost = new CostL(simulation);
			res = cost.calculate() / simulation.getAccepted();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

}
