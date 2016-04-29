package li.evaluation.metrics;

import java.io.IOException;

import li.simulation.AbstractSimulation;
import li.simulation.SteinerTreeProbabilitySimulation;

public class ProbabilityL extends Metric {

	public ProbabilityL(AbstractSimulation simulation, String method) throws IOException {
		super(simulation, method);
	}
	
	public ProbabilityL(AbstractSimulation simulation, String method, int lambda) throws IOException {
		super(simulation, method, lambda);
	}

	@Override
	public String name() {
		return "failure probability";
	}

	@Override
	public double calculate() {
		if(this.simulation instanceof SteinerTreeProbabilitySimulation){
			SteinerTreeProbabilitySimulation tempSim = (SteinerTreeProbabilitySimulation)this.simulation;
			return tempSim.getProbability().get(tempSim.getMappedVNs().get(tempSim.getMappedVNs().size()-1));
			
		}
		return 0;
	}

}
