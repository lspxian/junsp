package vnreal.algorithms.linkmapping;

import java.util.List;
import java.util.Map;

import edu.uci.ics.jung.graph.util.EdgeType;
import li.multiDomain.Domain;
import vnreal.algorithms.AbstractMultiDomainLinkMapping;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.InterLink;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class MultiDomainAsOneDomain extends AbstractMultiDomainLinkMapping {

	public MultiDomainAsOneDomain(List<Domain> domains) {
		super(domains);
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet,
			Map<VirtualNode, SubstrateNode> nodeMapping) {
		
		Domain newDomain = merge(domains);
		
		MultiCommodityFlow mcf = new MultiCommodityFlow(newDomain);
		
		Map<String, String> solution = mcf.linkMappingWithoutUpdate(vNet, nodeMapping);
		
		if(solution.size()==0){
			System.out.println("link no solution");
			for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
				NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
			}
			for(Domain d : domains){
				//restore node coordinate to [0,100]
				for(SubstrateNode snode : d.getVertices()){
					double x = snode.getCoordinateX()-d.getCoordinateX()*100;
					double y = snode.getCoordinateY()-d.getCoordinateY()*100;
					snode.setCoordinateX(x);
					snode.setCoordinateY(y);
				}
			}
			return false;
		}
		
		//update
		BandwidthDemand bwDem = null,newBwDem;
		VirtualNode srcVnode = null, dstVnode = null;
		SubstrateNode srcSnode = null, dstSnode = null;
		int srcVnodeId, dstVnodeId, srcSnodeId, dstSnodeId;
		
		for(Map.Entry<String, String> entry : solution.entrySet()){
			String linklink = entry.getKey();
			double flow = Double.parseDouble(entry.getValue());
			srcVnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("vs")+2, linklink.indexOf("vd")));
			dstVnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("vd")+2, linklink.indexOf("ss")));
			srcSnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("ss")+2, linklink.indexOf("sd")));
			dstSnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("sd")+2));
			
			//for undirected network, flow 0->1 and 1->0 are added to 0<->1, so if we have a flow 1->0, 
			//we have to change the s and d to meet the original link 0->1
			
			if(srcSnodeId>dstSnodeId){
				int tmp = srcSnodeId;
				srcSnodeId = dstSnodeId;
				dstSnodeId = tmp;
			}
			
			srcVnode = vNet.getNodeFromID(srcVnodeId);
			dstVnode = vNet.getNodeFromID(dstVnodeId);
			VirtualLink tmpvl = vNet.findEdge(srcVnode, dstVnode);
			
			for (AbstractDemand dem : tmpvl) {
				if (dem instanceof BandwidthDemand) {
					bwDem = (BandwidthDemand) dem;
					break;
				}
			}
			
			srcSnode = newDomain.getNodeFromID(srcSnodeId);
			dstSnode = newDomain.getNodeFromID(dstSnodeId);
			SubstrateLink tmpsl = newDomain.findEdge(srcSnode, dstSnode);
			
			newBwDem = new BandwidthDemand(tmpvl);
			newBwDem.setDemandedBandwidth(bwDem.getDemandedBandwidth()*flow);
			
			if(!NodeLinkAssignation.vlmSingleLinkSimple(newBwDem, tmpsl)){
				throw new AssertionError("But we checked before!");
			}
			
		}
		
		for(Domain domain : domains){
			for(SubstrateNode snode : domain.getVertices()){
				double x = snode.getCoordinateX()-domain.getCoordinateX()*100;
				double y = snode.getCoordinateY()-domain.getCoordinateY()*100;
				snode.setCoordinateX(x);
				snode.setCoordinateY(y);
			}
		}
		
		return true;
	}

	//merge multi-domain
	private Domain merge(List<Domain> domains) {
		
		Domain newDomain = new Domain();
		for(Domain domain : domains){
			//add substrate node
			for(SubstrateNode snode : domain.getVertices()){
				double x = snode.getCoordinateX()+domain.getCoordinateX()*100;
				double y = snode.getCoordinateY()+domain.getCoordinateY()*100;
				snode.setCoordinateX(x);
				snode.setCoordinateY(y);
			}
			//add substrate link
			for(SubstrateLink sl : domain.getEdges()){
				newDomain.addEdge(sl, domain.getEndpoints(sl).getFirst(), domain.getEndpoints(sl).getSecond(), EdgeType.UNDIRECTED);
			}
			//add inter link
			for(InterLink il : domain.getInterLink()){
				if(!newDomain.getInterLink().contains(il)){
					newDomain.getInterLink().add(il);
					newDomain.addEdge(il, il.getNode1(), il.getNode2(), EdgeType.UNDIRECTED);
				}
			}
		}
		
		return newDomain;
	}
	
}
