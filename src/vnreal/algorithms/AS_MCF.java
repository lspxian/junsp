package vnreal.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LinkedMap;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.util.EdgeType;
import li.multiDomain.Domain;
import vnreal.algorithms.linkmapping.MultiCommodityFlow;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.AugmentedLink;
import vnreal.network.substrate.AugmentedNetwork;
import vnreal.network.substrate.InterLink;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.AugmentedVirtualLink;
import vnreal.network.virtual.VirtualInterLink;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.resources.BandwidthResource;

// multi-domains, autonomous system multi-commodity flow algorithm 
public class AS_MCF extends AbstractMultiDomainLinkMapping {

	public AS_MCF(List<Domain> domains){
		super(domains);
	}
	
	@Override
	public boolean linkMapping(VirtualNetwork vNet,
			Map<VirtualNode, SubstrateNode> nodeMapping) {
		/*
		Map<SubstrateNode, VirtualNode> inverseNodeMapping = new LinkedMap<SubstrateNode, VirtualNode>();
		for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
			inverseNodeMapping.put(entry.getValue(), entry.getKey());
		}*/
		
		Transformer<SubstrateLink, Double> weightTrans = new Transformer<SubstrateLink,Double>(){
			public Double transform(SubstrateLink link){
				//return 1/((BandwidthResource)link.get().get(0)).getAvailableBandwidth();
				return 1.;
			}
		};
		
		Map<Domain, VirtualNetwork> newVnet = new HashMap<Domain, VirtualNetwork>();
		
		Collection<VirtualLink> virtualLinks = vNet.getEdges();
		//for(Domain domain : domains){
		for(int i=0;i<domains.size();i++){
			//System.out.println(domains.get(1));
			Domain domain = domains.get(i);
			newVnet.put(domain, new VirtualNetwork());	//initialize the 2nd mcf
			//Create virtual network for each domain, transform virtual link to virtual inter link, this means to add source domain and destination domain.
			VirtualNetwork tmpvn = new VirtualNetwork();
			for(VirtualLink vlink : virtualLinks){
				VirtualNode vSource = vNet.getEndpoints(vlink).getFirst();
				VirtualNode vDest = vNet.getEndpoints(vlink).getSecond();
				SubstrateNode sSource = nodeMapping.get(vSource);
				SubstrateNode sDest = nodeMapping.get(vDest);
				if(domain.containsVertex(sSource)&&domain.containsVertex(sDest)){
					tmpvn.addEdge(vlink, vSource, vDest, EdgeType.UNDIRECTED);
					newVnet.get(domain).addEdge(vlink, vSource, vDest, EdgeType.UNDIRECTED);
					//virtualLinks.remove(vlink);
				}
				else if(domain.containsVertex(sSource)||domain.containsVertex(sDest))
					tmpvn.addEdge(new VirtualInterLink(vlink,vSource,vDest), vSource, vDest, EdgeType.UNDIRECTED);
			}
			System.out.println(tmpvn);
			
			//Create substrate augmented network for each domain, determine intra substrate links, inter substrate link, augmented links
			AugmentedNetwork an = new AugmentedNetwork(domain);	//intra substrate links
			for(InterLink tmplink : domain.getInterLink()){
				an.addEdge(tmplink, tmplink.getInterior(), tmplink.getExterior(), EdgeType.UNDIRECTED);	//inter substrate links
			}
			
			for(VirtualLink vl : tmpvn.getEdges()){
				if(vl instanceof VirtualInterLink){
					VirtualInterLink vil = (VirtualInterLink) vl;
					VirtualNode vnode1 = vil.getNode1();
					VirtualNode vnode2 = vil.getNode2();
					SubstrateNode snode1 = nodeMapping.get(vnode1);
					SubstrateNode snode2 = nodeMapping.get(vnode2);
					SubstrateNode dijkDest=null;
					Domain exterDomain = null;
					if(vnode1.getDomain().equals(domain)){
						exterDomain = vnode2.getDomain();
						dijkDest = snode2;
					}
					else if(vnode2.getDomain().equals(domain)){
						exterDomain = vnode1.getDomain();
						dijkDest = snode1;
					}
					else	continue;
					for(InterLink ilink : domain.getInterLink()){
						SubstrateNode dijkSource = ilink.getExterior();
						if(exterDomain.containsVertex(dijkSource)&&
								(!dijkSource.equals(dijkDest))&&
								(!an.existLink(dijkSource, dijkDest))){		//augmented link does not exist in the augmented network
							
							DijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode, SubstrateLink>(exterDomain,weightTrans);
							AugmentedLink al = new AugmentedLink();
							double cost = (double) dijkstra.getDistance(dijkSource, dijkDest);
							System.out.println(cost);
							al.setCost(cost);
							al.addResource(100/(cost));	//normally random(0,1), here random = 100 means that it has infinite bw
							an.addEdge(al, dijkSource, dijkDest, EdgeType.UNDIRECTED);	//augmented links
							
						}
						
					}
				}
			}
			System.out.println(an);
			
			//first mcf
			MultiCommodityFlow mcf = new MultiCommodityFlow(an);
			//the nodemapping here is original for all the nodes in all domains. 
			Map<String, String> solution = mcf.linkMappingWithoutUpdate(tmpvn, nodeMapping);
			if(solution.size()==0){
				System.out.println("link no solution");
				for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
					NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
				}
				return false;
			}
			System.out.println(solution);
			
			//Don't update here, 	
			VirtualNode srcVnode = null, dstVnode = null;
			SubstrateNode srcSnode = null,dstSnode = null;
			int srcVnodeId, dstVnodeId, srcSnodeId, dstSnodeId;
			for(Map.Entry<String, String> entry : solution.entrySet()){
				String linklink = entry.getKey();
				double flow = Double.parseDouble(entry.getValue());
				srcVnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("vs")+2, linklink.indexOf("vd")));
				dstVnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("vd")+2, linklink.indexOf("ss")));
				srcSnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("ss")+2, linklink.indexOf("sd")));
				dstSnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("sd")+2));
				
				srcVnode = tmpvn.getNodeFromID(srcVnodeId);
				dstVnode = tmpvn.getNodeFromID(dstVnodeId);
				srcSnode = an.getNodeFromID(srcSnodeId);
				dstSnode = an.getNodeFromID(dstSnodeId);
				
				VirtualLink tmpvl = tmpvn.findEdge(srcVnode, dstVnode);
				SubstrateLink tmpsl = an.findEdge(srcSnode, dstSnode);
				BandwidthDemand bwDem=null;
				Domain exterDomain = null;
				for(AbstractDemand dem : tmpvl){
					if (dem instanceof BandwidthDemand) {
						bwDem = (BandwidthDemand) dem;
						break;
					}
				}
				
				tmpvl.getSolution().put(domain, new HashMap<SubstrateLink,Double>());
				if(!(tmpvl instanceof VirtualInterLink)){
					tmpvl.getSolution().get(domain).put(tmpsl, flow);
				}
				else if(tmpsl instanceof AugmentedLink){
					VirtualInterLink tmpvil = (VirtualInterLink) tmpvl;
					if(domain.containsVertex(nodeMapping.get(dstVnode))){
						VirtualNode tmp = dstVnode;
						dstVnode = srcVnode;
						srcVnode = tmp;
					}
					exterDomain = dstVnode.getDomain();
					
					if(nodeMapping.get(dstVnode).equals(srcSnode)){
						SubstrateNode tmp = dstSnode;
						dstSnode = srcSnode;
						srcSnode = tmp;
					}
					
					VirtualNode newVNode = new VirtualNode();
					nodeMapping.put(newVNode, srcSnode);
					AugmentedVirtualLink newVLink = new AugmentedVirtualLink(domain, tmpvil.getOrigLink());	//original virtual link
					BandwidthDemand bw=new BandwidthDemand(newVLink);
					bw.setDemandedBandwidth(bwDem.getDemandedBandwidth()*flow);
					newVLink.add(bw);
					newVnet.get(exterDomain).addEdge(newVLink, newVNode, dstVnode, EdgeType.UNDIRECTED);	
				}
				else {
					VirtualInterLink tmpvil = (VirtualInterLink) tmpvl;	
					tmpvil.getOrigLink().getSolution().get(domain).put(tmpsl, flow);	////original virtual link
				}
				
			}
		
		}
		
		// 2nd mcf
		for(Map.Entry<Domain, VirtualNetwork> e : newVnet.entrySet()){
			Domain domain = e.getKey();
			VirtualNetwork tmpvn = e.getValue();
			MultiCommodityFlow mcf = new MultiCommodityFlow(domain);
			Map<String, String> solution = mcf.linkMappingWithoutUpdate(tmpvn, nodeMapping);
			if(solution.size()==0){
				System.out.println("link no solution");
				for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
					NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
				}
				return false;
			}
			
			VirtualNode srcVnode = null, dstVnode = null;
			SubstrateNode srcSnode = null,dstSnode = null;
			int srcVnodeId, dstVnodeId, srcSnodeId, dstSnodeId;
			for(Map.Entry<String, String> entry : solution.entrySet()){
				String linklink = entry.getKey();
				double flow = Double.parseDouble(entry.getValue());
				srcVnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("vs")+2, linklink.indexOf("vd")));
				dstVnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("vd")+2, linklink.indexOf("ss")));
				srcSnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("ss")+2, linklink.indexOf("sd")));
				dstSnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("sd")+2));
				
				srcVnode = tmpvn.getNodeFromID(srcVnodeId);
				dstVnode = tmpvn.getNodeFromID(dstVnodeId);
				srcSnode = domain.getNodeFromID(srcSnodeId);
				dstSnode = domain.getNodeFromID(dstSnodeId);
				
				VirtualLink tmpvl = tmpvn.findEdge(srcVnode, dstVnode);
				SubstrateLink tmpsl = domain.findEdge(srcSnode, dstSnode);
				
				if(tmpvl instanceof AugmentedVirtualLink){
					AugmentedVirtualLink augmentedvl = (AugmentedVirtualLink) tmpvl;
					augmentedvl.getOriginalVL().getSolution().get(
							augmentedvl.getOriginalDomain()).put(tmpsl, flow);
				}
				else {
					//TODO intra virtual link mapping self-compare
					
				}
			}
		}
		
		//compare 2 mcf results to get a better solution
		for(VirtualLink vl : vNet.getEdges()){
			
		}
		
		return true;
	}

}
