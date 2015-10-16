package li.multiDomain;

import java.util.HashMap;
import java.util.Map;

import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;

public class Solution {
	private SubstrateNetwork domain;
	private Map<SubstrateLink, Double> flow;
	
	public Solution(){
		this.domain = null;
		flow = new HashMap<SubstrateLink, Double>();
	}
	
	public Solution(Domain domain){
		this.domain = domain;
		flow = new HashMap<SubstrateLink, Double>();
	}

	public Map<SubstrateLink, Double> getFlow() {
		return flow;
	}

	public void setFlow(Map<SubstrateLink, Double> flow) {
		this.flow = flow;
	}

	public SubstrateNetwork getDomain() {
		return domain;
	}
	
}
