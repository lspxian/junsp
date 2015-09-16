package li.evaluation.metrics;

import java.io.IOException;

import li.simulation.Simulation;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.mapping.Mapping;
import vnreal.network.substrate.SubstrateLink;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;

public class LinkUtilizationL extends Metric {

	private static double capacity = 0.0,sum = 0.0;
	public LinkUtilizationL(Simulation simulation) throws IOException {
		super(simulation);
	}

	@Override
	public String name() {
		return "linkUtilization";
	}

	@Override
	public double calculate() {
		for (SubstrateLink sl : this.simulation.getSubstrateNetwork().getEdges()) {
			for (AbstractResource res : sl.get()) {
				if(res instanceof BandwidthResource){
					capacity += ((BandwidthResource) res).getBandwidth();					
					//System.out.println(capacity);
				}
			}
		}
		for (SubstrateLink sl : this.simulation.getSubstrateNetwork().getEdges()) {
			for (AbstractResource res : sl.get()) {
				for (Mapping m : res.getMappings()) {
					AbstractDemand dem = m.getDemand();
					if (dem instanceof BandwidthDemand) {
						sum += ((BandwidthDemand) dem).getDemandedBandwidth();
					}
				}
			
			}
		}
		return sum/capacity;
	}
		

}
