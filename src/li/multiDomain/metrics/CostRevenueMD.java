package li.multiDomain.metrics;

import java.io.IOException;

import li.simulation.MultiDomainSimulation;

public class CostRevenueMD extends MetricMD {

	public CostRevenueMD(MultiDomainSimulation simulation, String method) throws IOException {
		super(simulation, method);
	}
	public CostRevenueMD(MultiDomainSimulation simulation, String method, int lambda) throws IOException{
		super(simulation, method, lambda);
	}
	
	@Override
	public String name() {
		return "CostRevenue";
	}

	@Override
	public double calculate() {
		try {
			CostMD cost= new CostMD(simulation);
			MappedRevenueMD rev = new MappedRevenueMD(simulation);
			return cost.calculate() / rev.calculate();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}

}
