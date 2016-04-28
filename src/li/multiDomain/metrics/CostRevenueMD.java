package li.multiDomain.metrics;

import java.io.IOException;

import li.multiDomain.AbstractMultiDomain;

public class CostRevenueMD extends MetricMD {

	public CostRevenueMD(AbstractMultiDomain simulation, String method) throws IOException {
		super(simulation, method);
	}
	public CostRevenueMD(AbstractMultiDomain simulation, String method, int lambda) throws IOException{
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
			e.printStackTrace();
		}
		
		return 0;
	}

}
