package li.evaluation.metrics;

import java.io.IOException;

import li.simulation.AbstractSimulation;
import li.simulation.SteinerTreeProbabilitySimulation;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;

public class RevenueProba extends Metric {

	public RevenueProba(AbstractSimulation simulation) throws IOException {
		super(simulation);
	}
	
	public RevenueProba(AbstractSimulation simulation, String method) throws IOException {
		super(simulation, method);
	}
	
	public RevenueProba(AbstractSimulation simulation, String method, int lambda) throws IOException {
		super(simulation, method, lambda);
	}

	@Override
	public String name() {
		return "Revenue with probability ";
	}

	@Override
	public double calculate() {
		double revenueProba = 0;
		if(simulation instanceof SteinerTreeProbabilitySimulation){
			SteinerTreeProbabilitySimulation sim = (SteinerTreeProbabilitySimulation)simulation; 
			for (VirtualNetwork tmpvn : sim.getMappedVNs()) {
				revenueProba += calculateVnetRevenue(tmpvn)*sim.getProbability().get(tmpvn);
			}
		}
		return revenueProba;
	}

	private double calculateVnetRevenue(VirtualNetwork vNet) {
		double total_demBW = 0;
		for (VirtualLink tmpl: vNet.getEdges()) {
			for (AbstractDemand dem : tmpl) {
				if (dem instanceof BandwidthDemand) {
					total_demBW += ((BandwidthDemand) dem)
							.getDemandedBandwidth();
					break;
				}

			}
		}
		return total_demBW;
	}

}
