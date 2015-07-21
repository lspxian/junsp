package li.evaluation.metrics;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import cherif.Simulation;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.demands.CpuDemand;
import vnreal.evaluations.utils.VnrUtils;
import vnreal.network.Network;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class MappedRevenueL extends Metric{
	private boolean isPathSplitting;

	public MappedRevenueL(Simulation simulation, boolean isPsAlgorithm) throws IOException {
		super(simulation);
		this.isPathSplitting = isPsAlgorithm;
	}

	@Override
	public String name() {
		return "MappedRevenue";
	}

	@Override
	public double calculate() {
		Network<?, ?, ?> tmpN;
		Map<VirtualNetwork, Boolean> isMappedVnr = VnrUtils
				.calculateMappedVnr(simulation.getVirtualNetworks());
		VirtualNetwork tempVnr;
		double mappedRevenue = 0;
		for (Iterator<VirtualNetwork> net = simulation.getVirtualNetworks().iterator(); net.hasNext();) {
			tmpN = net.next();
			if (tmpN.getLayer() != 0) {
				tempVnr = (VirtualNetwork) tmpN;
				if (isMappedVnr.get(tempVnr))
					mappedRevenue += calculateVnetRevenue(tempVnr);
			}
		}
		return mappedRevenue;
	}
	
	private double calculateVnetRevenue(VirtualNetwork vNet) {
		double total_demBW = 0;
		double total_demCPU = 0;
		Iterable<VirtualLink> tmpLinks;
		Iterable<VirtualNode> tmpNodes;
		tmpLinks = vNet.getEdges();
		tmpNodes = vNet.getVertices();
		for (Iterator<VirtualLink> tmpLink = tmpLinks.iterator(); tmpLink
				.hasNext();) {
			VirtualLink tmpl = tmpLink.next();
			for (AbstractDemand dem : tmpl) {
				if (dem instanceof BandwidthDemand) {
					if (!isPathSplitting) {
						total_demBW += ((BandwidthDemand) dem)
								.getDemandedBandwidth();
						break; // continue with next link
					} else {
						if (dem.getMappings().isEmpty()) {
							total_demBW += ((BandwidthDemand) dem)
									.getDemandedBandwidth();
							break;
						}
					}
				}

			}
		}
		for (Iterator<VirtualNode> tmpNode = tmpNodes.iterator(); tmpNode
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
