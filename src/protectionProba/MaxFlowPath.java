package protectionProba;

import java.util.ArrayList;
import java.util.List;

import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.SubstrateLink;

public class MaxFlowPath implements Comparable<MaxFlowPath>{
	
	SubstrateLink target;
	List<SubstrateLink> path;
	double capacity;
	List<BandwidthDemand> mapping;
	
	public MaxFlowPath(SubstrateLink sl,List<SubstrateLink> path,double cap){
		this.target=sl;
		this.path=path;
		this.capacity=cap;
		this.mapping=new ArrayList<BandwidthDemand>();
	}

	public int length(){
		return path.size();
	}
	
	public double residual(){
		double residual=capacity;
		for(BandwidthDemand bwd:mapping){
			capacity=capacity-bwd.getDemandedBandwidth();
		}
		return residual;
	}
	
	public boolean fulfil(BandwidthDemand bwd){
		double demand=bwd.getDemandedBandwidth();
		if(residual()>demand){
			mapping.add(bwd);
			return true;
		}
		return false;
	}
	
	public boolean free(BandwidthDemand bwd){
		return mapping.remove(bwd);
	}
	
	public List<SubstrateLink> getPath() {
		return path;
	}

	@Override
	public int compareTo(MaxFlowPath o) {
		//short path first
		return this.length()-o.length();
	}
	
	
}
