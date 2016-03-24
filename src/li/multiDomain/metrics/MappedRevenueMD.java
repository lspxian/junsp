package li.multiDomain.metrics;

import java.io.IOException;
import java.util.Iterator;

import li.multiDomain.AbstractMultiDomain;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.demands.CpuDemand;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class MappedRevenueMD extends MetricMD {

	public MappedRevenueMD(AbstractMultiDomain simulation) throws IOException {
		super(simulation);
	}
	public MappedRevenueMD(AbstractMultiDomain simulation, String method) throws IOException {
		super(simulation, method);
	}
	public MappedRevenueMD(AbstractMultiDomain simulation, String method, int lambda) throws IOException{
		super(simulation, method, lambda);
	}

	@Override
	public String name() {
		return "MappedRevenue";
	}

	@Override
	public double calculate() {
		VirtualNetwork tempVnr;
		double mappedRevenue = 0;
		for (Iterator<VirtualNetwork> net = simulation.getMappedVNs().iterator(); net.hasNext();) {
			tempVnr = net.next();
			mappedRevenue += calculateVnetRevenue(tempVnr);
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
	}
}
