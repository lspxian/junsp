package li.evaluation.metrics;

import java.io.IOException;

import li.simulation.AbstractSimulation;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.mapping.Mapping;
import vnreal.network.substrate.SubstrateLink;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;

public class CurrentLinkUtilisationL extends Metric {
	private double capacity,sum;
	
	public CurrentLinkUtilisationL(AbstractSimulation simulation) throws IOException {
		super(simulation);
	}
	
	public CurrentLinkUtilisationL(AbstractSimulation simulation, String method) throws IOException{
		super(simulation, method);
	}
	
	public CurrentLinkUtilisationL(AbstractSimulation simulation, String method, int lambda) throws IOException{
		super(simulation, method, lambda);
	}
	
	@Override
	public String name() {
		return "Current_Link_Utilisation";
	}

	@Override
	public double calculate() {
		capacity = 0.0;
		sum = 0.0;
	/*	for (SubstrateLink sl : this.simulation.getSubstrateNetwork().getEdges()) {
			for (AbstractResource res : sl.get()) {
				if(res instanceof BandwidthResource){
					capacity += ((BandwidthResource) res).getBandwidth();					
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
		return sum/capacity;*/
		
		for (SubstrateLink sl : this.simulation.getSubstrateNetwork().getEdges()) {
			capacity+=sl.getBandwidthResource().getBandwidth();
			sum+=sl.getBandwidthResource().getOccupiedBandwidth();
		}
		return sum/capacity;
	}

}
