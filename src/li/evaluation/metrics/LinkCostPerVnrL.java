package li.evaluation.metrics;

import java.io.IOException;
import java.util.Iterator;

import cherif.Simulation;
import vnreal.demands.BandwidthDemand;
import vnreal.mapping.Mapping;
import vnreal.network.substrate.SubstrateLink;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;

public class LinkCostPerVnrL extends Metric{
	private static double linkCost = 0.0;
	public LinkCostPerVnrL(Simulation simulation) throws IOException {
		super(simulation);
	}

	@Override
	public String name() {
		return "linkCost";
	}

	@Override
	public double calculate() {
			BandwidthDemand tmpBwDem;
			for (Iterator<SubstrateLink> tmpSLink = simulation.getSubstrateNetwork().getEdges()
					.iterator(); tmpSLink.hasNext();) {
				SubstrateLink currSLink = tmpSLink.next();
				for (AbstractResource res : currSLink) {
					if (res instanceof BandwidthResource) {
						for (Mapping f : res.getMappings()) {
							tmpBwDem = (BandwidthDemand) f.getDemand();
							linkCost += tmpBwDem.getDemandedBandwidth();
						}
					}
				}
			}
			

			return linkCost / (simulation.getAccepted());
	}
}
