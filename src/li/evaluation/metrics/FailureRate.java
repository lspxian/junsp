package li.evaluation.metrics;

import java.io.IOException;

import li.simulation.AbstractSimulation;
import li.simulation.ProbabilitySimulation;
import vnreal.network.virtual.VirtualNetwork;

public class FailureRate extends Metric {

	public FailureRate(ProbabilitySimulation simulation)throws IOException{
		super(simulation);
	}
	
	public FailureRate(AbstractSimulation simulation, String method, int lambda) throws IOException {
		super(simulation, method, lambda);
	}
	
	@Override
	public String name() {
		return "Failure rate ";
	}

	@Override
	public double calculate() {
		double sum=0.0;
		for(VirtualNetwork vn : this.simulation.getMappedVNs()){
			//mtbf of each vn
			sum += vn.getFailureNumber()/vn.getLifetime();
//			sum += vn.getLifetime()/(vn.getFailureNumber()+1);
//			System.out.println(vn.getId( )+" "+vn.getFailureNumber()+" "+vn.getLifetime());
		}
		return sum/this.simulation.getMappedVNs().size();
	}

}
