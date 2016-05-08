package li.evaluation.metrics;

import java.io.IOException;
import java.util.Iterator;

import li.simulation.AbstractSimulation;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;

public class MappedLinkRevenueL extends Metric {

	public MappedLinkRevenueL(AbstractSimulation simulation, String method, int lambda) throws IOException {
		super(simulation, method, lambda);
	}

	@Override
	public String name() {
		return "Mapped link revenue";
	}

	@Override
	public double calculate() {
		VirtualNetwork tmpN;
		double mappedRevenue = 0;
		for (Iterator<VirtualNetwork> net = simulation.getMappedVNs().iterator(); net.hasNext();) {
			tmpN = net.next();
			mappedRevenue += calculateVnetRevenue(tmpN);
		}
		return mappedRevenue;
	}

	private double calculateVnetRevenue(VirtualNetwork vNet) {
		double total_demBW = 0;
		for (Iterator<VirtualLink> tmpLink = vNet.getEdges().iterator(); tmpLink
				.hasNext();) {
			VirtualLink tmpl = tmpLink.next();
			for (AbstractDemand dem : tmpl) {
				if (dem instanceof BandwidthDemand) {
					total_demBW += ((BandwidthDemand) dem)
							.getDemandedBandwidth();
					break;
				}

			}
		}
		
		return (total_demBW);
	}

}
