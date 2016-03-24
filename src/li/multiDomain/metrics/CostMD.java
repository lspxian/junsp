package li.multiDomain.metrics;

import java.io.IOException;
import li.multiDomain.AbstractMultiDomain;


public class CostMD extends MetricMD {
	double nodeCost = 0.0, linkCost = 0.0;
	public CostMD(AbstractMultiDomain simulation) throws IOException {
		super(simulation);
	}
	public CostMD(AbstractMultiDomain simulation, String method) throws IOException {
		super(simulation, method);
	}
	public CostMD(AbstractMultiDomain simulation, String method, int lambda) throws IOException{
		super(simulation, method, lambda);
	}
	@Override
	public String name() {
		return "Cost";
	}

	@Override
	public double calculate() {
		return simulation.getTotalCost();
	}

}
