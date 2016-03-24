package li.multiDomain.metrics;

import java.io.IOException;
import java.util.ArrayList;

import li.multiDomain.AbstractMultiDomain;
import li.multiDomain.Domain;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.mapping.Mapping;
import vnreal.network.substrate.SubstrateLink;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;

public class CurrentLinkUtilisationMD extends MetricMD {

	private double capacity,sum;
	public CurrentLinkUtilisationMD(AbstractMultiDomain simulation) throws IOException {
		super(simulation);
	}
	
	public CurrentLinkUtilisationMD(AbstractMultiDomain simulation, String method) throws IOException {
		super(simulation, method);
		capacity = 0.0;
		sum = 0.0;
	}
	public CurrentLinkUtilisationMD(AbstractMultiDomain simulation, String method, int lambda) throws IOException{
		super(simulation, method, lambda);
	}

	@Override
	public String name() {
		return "CurrentlinkUtilisation";
	}

	@Override
	public double calculate() {
		capacity = 0.0;
		sum = 0.0;
		ArrayList<SubstrateLink> allLinks = new ArrayList<SubstrateLink>();
		for(Domain d : this.simulation.getMultiDomain()){
			for(SubstrateLink link: d.getAllLinks()){
				if(!allLinks.contains(link))
					allLinks.add(link);
			}
		}
		for (SubstrateLink sl : allLinks) {
			for (AbstractResource res : sl.get()) {
				if(res instanceof BandwidthResource){
					capacity += ((BandwidthResource) res).getBandwidth();					
					//System.out.println(capacity);
				}
			}
		}
		for (SubstrateLink sl : allLinks) {
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
