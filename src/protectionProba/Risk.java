package protectionProba;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.demands.BandwidthDemand;
import vnreal.network.NetworkEntity;
import vnreal.resources.AbstractResource;
/*
 * This is a substrate network element failure risk class 
 * It implement the shared protection principle
 * ne : network element, a node or a link. Don't include SRLG
 * demands : virtual demands (links) that are supported by the risk element
 */
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
	
	public List<BandwidthDemand> getDemands() {
		return demands;
	}

	public void addDemand(BandwidthDemand bwd){
		demands.add(bwd);
	}
	
	public void removeDemand(BandwidthDemand bwd){
		demands.remove(bwd);
	}
	
	public void findAndRemove(BandwidthDemand bwd){
		for(Iterator<BandwidthDemand> it=demands.iterator();it.hasNext();){
			if(it.next().getOwner().equals(bwd.getOwner()))
				it.remove();
		}
	}
	
	public double getTotal(){
		double total=0.0;
		for(BandwidthDemand bwd:demands){
			total +=  bwd.getDemandedBandwidth();			
		}
		return MiscelFunctions.roundThreeDecimals(total);
	}
	
	public String toString(){
		String str = "Risk: "+ne.toString();
		for(BandwidthDemand de:demands){
			str+=de.toString();
		}
		return str+"\n";
	}
}
