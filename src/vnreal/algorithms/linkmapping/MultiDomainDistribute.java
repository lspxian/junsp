package vnreal.algorithms.linkmapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.uci.ics.jung.graph.util.EdgeType;
import li.multiDomain.Domain;
import vnreal.algorithms.AbstractMultiDomainLinkMapping;
import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.AugmentedNetwork;
import vnreal.network.substrate.InterLink;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.resources.BandwidthResource;

public class MultiDomainDistribute extends AbstractMultiDomainLinkMapping {


	Map<Domain, VirtualNetwork> localVNets;
	Map<Domain, AugmentedNetwork> augmentedNets;
	Map<BandwidthDemand, BandwidthResource> mapping;
	List<VirtualLink> linkToMap;
	
	public MultiDomainDistribute(List<Domain> domains) {
		super(domains);
		this.initialize();
	}
	
	public MultiDomainDistribute(List<Domain> domains,String localPath, String remotePath){
		super(domains,localPath, remotePath);
		this.initialize();
	}

	private void initialize(){
		this.localVNets =  new LinkedHashMap<Domain, VirtualNetwork>();
		this.augmentedNets = new LinkedHashMap<Domain, AugmentedNetwork>();
		for(Domain d : domains){
			localVNets.put(d, new VirtualNetwork());
			augmentedNets.put(d, new AugmentedNetwork(d));	//intra substrate links
			for(InterLink tmplink : d.getInterLink()){
				augmentedNets.get(d).addEdge(tmplink, tmplink.getNode1(), tmplink.getNode2(), EdgeType.UNDIRECTED);	//inter substrate links
			}
		}		
		this.mapping = new LinkedHashMap<BandwidthDemand, BandwidthResource>();
		this.linkToMap = new ArrayList<VirtualLink>();
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		
		
		return true;
	}
	
	public void biDomainLinkMapping()
}
