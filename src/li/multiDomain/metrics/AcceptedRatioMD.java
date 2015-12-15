package li.multiDomain.metrics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import li.simulation.MultiDomainSimulation;

public class AcceptedRatioMD extends MetricMD {

	public AcceptedRatioMD(MultiDomainSimulation simulation) throws IOException {
		super(simulation);
	}
	
	public AcceptedRatioMD(MultiDomainSimulation simulation, String method) throws IOException {
		super(simulation, method);
	}
	public AcceptedRatioMD(MultiDomainSimulation simulation, String method, int lambda) throws IOException{
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
