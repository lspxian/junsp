package protectionProba;

import java.util.List;

import vnreal.network.substrate.SubstrateLink;
import vnreal.network.virtual.VirtualLink;

public class MaxFlowPath implements Comparable<MaxFlowPath>{
	
	List<SubstrateLink> path;
	double capacity;
	List<VirtualLink> mapping;
	
	public MaxFlowPath(SubstrateLink sl){
		
	}

	@Override
	public int compareTo(MaxFlowPath o) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
}
