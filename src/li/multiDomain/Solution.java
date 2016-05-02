package li.multiDomain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.resources.BandwidthResource;

public class Solution {
	private List<Domain> domainsOrder;
	private Map<BandwidthDemand, BandwidthResource> mapping;
	private double cost;
	
	public Solution(List<Domain> domainsOrder,Map<BandwidthDemand, BandwidthResource> mapping){
		this.domainsOrder=domainsOrder;
		this.mapping=mapping;
		this.cost = 0.0;
		computeCost();
	}
	
	public List<Domain> getDomainsOrder() {
		return domainsOrder;
	}
	public Map<BandwidthDemand, BandwidthResource> getMapping() {
		return mapping;
	}
	public double getCost() {
		return cost;
	}
	public void computeCost(){
		for(Map.Entry<BandwidthDemand, BandwidthResource> en : mapping.entrySet()){
			this.cost = this.cost + en.getKey().getDemandedBandwidth()/(en.getValue().getAvailableBandwidth()+0.001);
		}
	}
	
	
	
}
