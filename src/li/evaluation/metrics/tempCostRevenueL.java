package li.evaluation.metrics;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import li.simulation.Simulation;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.demands.CpuDemand;
import vnreal.evaluations.utils.VnrUtils;
import vnreal.mapping.Mapping;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class tempCostRevenueL extends Metric{
	
private boolean isPathSplitting;
double costMapped = 0;
private static double  mappedRevenue = 0;

	public tempCostRevenueL(Simulation simulation, boolean isPsAlgorithm) throws IOException {
		super(simulation);
		this.isPathSplitting = isPsAlgorithm;
	}

	@Override
	public String name() {
		return "tempCostRevenue";
	}

	@Override
	public double calculate() {
		Map<VirtualNetwork, Boolean> isMappedVnr = VnrUtils
				.calculateMappedVnr(simulation.getVirtualNetworks());
		VirtualNetwork tempVnr;
		//double mappedRevenue = 0;
		int mapped = 0;
		double costRev = 0;
		for (Iterator<VirtualNetwork> net = simulation.getVirtualNetworks().iterator(); net.hasNext();) {
			tempVnr = net.next();
				if (isMappedVnr.get(tempVnr)){
					mapped++;
					costMapped = 0;
					mappedRevenue += calculateVnetRevenue(tempVnr);
					costRev += (costMapped/mappedRevenue);
					
				}
					
			}
		return (costRev/mapped);
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
				for (AbstractDemand dem : tmpl) {
					if (dem instanceof BandwidthDemand) {
						for (Mapping map : dem.getMappings()){
							costMapped += ((BandwidthDemand) map.getDemand()).getDemandedBandwidth();
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
				for (AbstractDemand dem : tmps) {
					if (dem instanceof CpuDemand) {
						for (Mapping map : dem.getMappings()){
							costMapped += ((CpuDemand) map.getDemand()).getDemandedCycles();
						}
					}
				}
		}
		return (total_demBW + total_demCPU);
	}
}

