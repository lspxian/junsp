package li.evaluation.metrics;

import java.io.IOException;
import java.util.Iterator;

import li.simulation.AbstractSimulation;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.demands.CpuDemand;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class MappedRevenueL extends Metric{
	 //double mappedRevenue = 0;

	public MappedRevenueL(AbstractSimulation simulation) throws IOException {
		super(simulation);
	}
	public MappedRevenueL(AbstractSimulation simulation, String method) throws IOException {
		super(simulation, method);
	}
	public MappedRevenueL(AbstractSimulation simulation, String method, int lambda) throws IOException{
		super(simulation, method, lambda);
	}

	@Override
	public String name() {
		return "Mapped_Revenue";
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
		double total_demCPU = 0;
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
		for (Iterator<VirtualNode> tmpNode = vNet.getVertices().iterator(); tmpNode
				.hasNext();) {
			VirtualNode tmps = tmpNode.next();
			for (AbstractDemand dem : tmps) {
				if (dem instanceof CpuDemand) {
					total_demCPU += ((CpuDemand) dem).getDemandedCycles();
					break; // continue with next node
				}
			}
		}
		return (total_demBW + total_demCPU);
		//return total_demBW ;
	}
}
