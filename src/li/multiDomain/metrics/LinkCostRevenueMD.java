package li.multiDomain.metrics;

import java.io.IOException;

import li.multiDomain.AbstractMultiDomain;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;

public class LinkCostRevenueMD extends MetricMD {

	public LinkCostRevenueMD(AbstractMultiDomain simulation) throws IOException {
		super(simulation);
	}

	public LinkCostRevenueMD(AbstractMultiDomain simulation, String method, int lambda) throws IOException{
		super(simulation, method, lambda);
	}

	@Override
	public String name() {
		return "LinkCost_Revenue";
	}

	@Override
	public double calculate() {
		double linkRevenue=0.0;
		for(VirtualNetwork vn:simulation.getMappedVNs()){
			for(VirtualLink vl:vn.getEdges()){
				linkRevenue=linkRevenue+vl.getBandwidthDemand().getDemandedBandwidth();
			}
		}
		return simulation.getLinkCost()/linkRevenue;
	}

}
