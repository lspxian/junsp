package protectionProba;

import java.util.List;

import vnreal.network.substrate.SubstrateLink;
import vnreal.network.virtual.VirtualLink;

public class MaxFlowPath implements Comparable<MaxFlowPath>{
	
	SubstrateLink target;
	List<SubstrateLink> path;
	double capacity;
	List<VirtualLink> mapping;
	
	public MaxFlowPath(SubstrateLink sl,List<SubstrateLink> path,double cap){
		this.target=sl;
		this.path=path;
		this.capacity=cap;
	}

	public int length(){
		return path.size();
	}
	
	@Override
	public int compareTo(MaxFlowPath o) {
		//short path first
		return this.length()-o.length();
	}
	
	
}
