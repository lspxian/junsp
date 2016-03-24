package li.multiDomain.metrics;

import java.io.IOException;
import li.multiDomain.AbstractMultiDomain;

public class AcceptedRatioMD extends MetricMD {

	public AcceptedRatioMD(AbstractMultiDomain simulation) throws IOException {
		super(simulation);
	}
	
	public AcceptedRatioMD(AbstractMultiDomain simulation, String method) throws IOException {
		super(simulation, method);
	}
	public AcceptedRatioMD(AbstractMultiDomain simulation, String method, int lambda) throws IOException{
		super(simulation, method, lambda);
	}

	@Override
	public String name() {
		return "AcceptedRatio";
	}

	@Override
	public double calculate() {
		return ((double)this.simulation.getAccepted()/(double)(this.simulation.getAccepted() + this.simulation.getRejected()))*100.0;
	}

}
