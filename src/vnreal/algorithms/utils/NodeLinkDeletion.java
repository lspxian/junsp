package vnreal.algorithms.utils;

import java.util.ArrayList;
import java.util.List;

import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.demands.CpuDemand;
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
						dem.free(res);
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
	@SuppressWarnings("static-access")
	public static boolean freeResource(VirtualNetwork vn, SubstrateNetwork sn) {
		NodeLinkDeletion ndl = new NodeLinkDeletion();
		List<SubstrateLink> list = new ArrayList<SubstrateLink>();
		list.addAll(0, sn.getEdges());
		for(VirtualNode vnode : vn.getVertices()){
			for(SubstrateNode snode : sn.getVertices()){
				ndl.nodeFree(vnode, snode);
			}
		}
		for(VirtualLink vlink : vn.getEdges()){
			ndl.linkFree(vlink, list);
		}
		return true;
		
	}
}
