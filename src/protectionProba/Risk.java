package protectionProba;

import java.util.ArrayList;
import java.util.List;

import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.demands.BandwidthDemand;
import vnreal.network.NetworkEntity;
import vnreal.resources.AbstractResource;

public class Risk {
	protected NetworkEntity<AbstractResource> ne;
	protected List<BandwidthDemand> demands;
	
	public Risk(NetworkEntity<AbstractResource> ne,BandwidthDemand demand){
		this.ne=ne;
		demands = new ArrayList<BandwidthDemand>();
		demands.add(demand);
	}
	
	public NetworkEntity<AbstractResource> getNe() {
		return ne;
	}
	
	public void addDemand(BandwidthDemand bwd){
		demands.add(bwd);
	}
	
	public boolean removeDemand(BandwidthDemand bwd){
		return demands.remove(bwd);
	}
	
	public double getTotal(){
		double total=0.0;
		for(BandwidthDemand bwd:demands){
			total +=  bwd.getDemandedBandwidth();			
		}
		return MiscelFunctions.roundThreeDecimals(total);
	}
	
}
