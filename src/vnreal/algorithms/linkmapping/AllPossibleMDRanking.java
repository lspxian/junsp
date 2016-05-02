package vnreal.algorithms.linkmapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import li.multiDomain.Domain;
import li.multiDomain.Solution;
import vnreal.algorithms.AbstractMultiDomainLinkMapping;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.resources.BandwidthResource;

public class AllPossibleMDRanking extends AbstractMultiDomainLinkMapping{
	
	List<Solution> solutions = new ArrayList<Solution>();
	List<List<Domain>> combinations = new ArrayList<List<Domain>>(); 
	
	public AllPossibleMDRanking(List<Domain> domains) {
		super(domains);
	}
	public AllPossibleMDRanking(List<Domain> domains, String localPath, String remotePath) {
		super(domains, localPath, remotePath);
	}
	
	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		this.generate3D();
		boolean flag = false;
		for(List<Domain> combination : this.combinations){
			MultiDomainRanking3 mdr = new MultiDomainRanking3(combination);	//3 without collection.sort
			if(mdr.linkMapping(vNet, nodeMapping)){
				flag = true;
				Solution solution = new Solution(combination,mdr.getMapping());
				this.solutions.add(solution);
				//
				for(Map.Entry<BandwidthDemand, BandwidthResource>e : mdr.getMapping().entrySet()){
					e.getKey().free(e.getValue());
				}
				
			}
				
		}
		
		if(flag==false) return false;
		else{
			this.updateResource();
			return true;
		}
		
	}
	
	private void updateResource(){
		double cost=100000;
		Solution best=null;
		for(Solution s: this.solutions){
			if(s.getCost()<cost){
				cost=s.getCost();
				best=s;
			}
		}
		
		for(Map.Entry<BandwidthDemand, BandwidthResource> e : best.getMapping().entrySet()){
			if(!NodeLinkAssignation.occupy(e.getKey(), e.getValue()))
				throw new AssertionError("But we checked before!");
		}
	}
	
	private void generate3D(){

		this.combinations.add(this.domains);
		
		Collections.swap(domains, 1, 2);
		this.combinations.add(new ArrayList<Domain>(domains));
		
		Collections.swap(domains, 0, 2);
		this.combinations.add(new ArrayList<Domain>(domains));
		
		Collections.swap(domains, 1, 2);
		this.combinations.add(new ArrayList<Domain>(domains));
		
		Collections.swap(domains, 0, 2);
		this.combinations.add(new ArrayList<Domain>(domains));
		
		Collections.swap(domains, 1, 2);
		this.combinations.add(new ArrayList<Domain>(domains));
	}
}