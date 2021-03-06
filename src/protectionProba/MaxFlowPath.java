package protectionProba;

import java.util.ArrayList;
import java.util.List;

import vnreal.algorithms.utils.MiscelFunctions;
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
			residual=residual-bwd.getDemandedBandwidth();
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
		for(BandwidthDemand bwd2:mapping){
			if(bwd2.getOwner().equals(bwd.getOwner()))
				return mapping.remove(bwd2);
		}
		return false;
	}
	
	public List<SubstrateLink> getPath() {
		return path;
	}
	
	public int mappingNB(){
		return this.mapping.size();
	}

	@Override
	public int compareTo(MaxFlowPath o) {
		//short path first
		return this.length()-o.length();
	}
	
	public String toString(){
		String res="cap:"+MiscelFunctions.roundThreeDecimals(capacity)+":";
		for(SubstrateLink sl:path){
			res+=sl.getId()+"->";
		}
		res+="#";
		for(BandwidthDemand bd:mapping){
			res+=bd.getDemandedBandwidth()+"@vl"+bd.getOwner().getId()+",";
		}
		return res+"\n";
	}

	public double getCapacity() {
		return capacity;
	}
	
	
}
