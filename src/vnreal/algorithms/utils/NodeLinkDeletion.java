package vnreal.algorithms.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import li.multiDomain.Domain;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.demands.CpuDemand;
import vnreal.network.substrate.InterLink;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;
import vnreal.resources.CpuResource;

public class NodeLinkDeletion {
		
	public static boolean nodeFree(VirtualNode vnode, SubstrateNode snode){
		for(AbstractDemand dem : vnode){
			if(dem instanceof CpuDemand){
				for(AbstractResource res : snode){
					if(res instanceof CpuResource){
						dem.free(res);	//false may be returned here but it doesn't matter
					}
				}
			}
		}
		
		return true;
	}
	
	public static boolean linkFree(VirtualLink vlink, List<SubstrateLink> slink){
		for(AbstractDemand dem : vlink){
			if(dem instanceof BandwidthDemand){
				for(SubstrateLink singleslink : slink){
					for(AbstractResource res : singleslink){
						if(res instanceof BandwidthResource ){
							dem.free(res);
						}
					}
				}
			}
		}
		return true;
	}
	
	// This function frees the resource allocations of a virtual network in a substrate network
	public static boolean freeResource(VirtualNetwork vn, SubstrateNetwork sn) {
		List<SubstrateLink> list = new ArrayList<SubstrateLink>();
		list.addAll(sn.getEdges());		//substrate links
		
		if(sn instanceof Domain){	//inter links in case of multi domain
			list.addAll(((Domain) sn).getInterLink());
		}
			
		for(VirtualNode vnode : vn.getVertices()){
			for(SubstrateNode snode : sn.getVertices()){
				nodeFree(vnode, snode);
			}
		}
		for(VirtualLink vlink : vn.getEdges()){
			linkFree(vlink, list);
		}
		return true;
		
	}
	
	public static boolean multiDomainFreeResource(VirtualNetwork vn, List<Domain> domains){
		for(Domain d : domains){
			NodeLinkDeletion.freeResource(vn, d);				
		}
		return true;
	}
	
	//free all resource
	public static boolean resetNet(SubstrateNetwork sn){
		for(SubstrateNode snode : sn.getVertices()){
			for(AbstractResource res : snode){
				if(res instanceof CpuResource){
					((CpuResource) res).reset();
				}
			}
		}
		
		for(SubstrateLink slink : sn.getEdges()){
			for(AbstractResource res : slink){
				if(res instanceof BandwidthResource){
					((BandwidthResource) res).reset();
				}
			}
		}
		
		return true;
	}

	public static void linkFreeBackup(BandwidthDemand bwd, Collection<SubstrateLink> slist, boolean share) {
		for(SubstrateLink sl:slist)
			sl.getBandwidthResource().backupFree(bwd, share);
	}
	public static void linkFreeBackup(VirtualLink vl, Collection<SubstrateLink> slist, boolean share) {
		for(SubstrateLink sl:slist)
			sl.getBandwidthResource().backupFree(vl.getBandwidthDemand(), share);
	}
	
	public static void FreeResourceBackup(VirtualNetwork vn, SubstrateNetwork sn, boolean share){
		for(VirtualLink vl : vn.getEdges()){
			linkFreeBackup(vl,sn.getEdges(),share);
		}
	}
}
