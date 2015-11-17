package vnreal.algorithms.linkmapping;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.util.EdgeType;
import li.multiDomain.Domain;
import vnreal.algorithms.AbstractMultiDomainLinkMapping;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.AugmentedLink;
import vnreal.network.substrate.AugmentedNetwork;
import vnreal.network.substrate.InterLink;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.AugmentedVirtualLink;
import vnreal.network.virtual.VirtualInterLink;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;

public class TwoDomainMCF extends AbstractMultiDomainLinkMapping {

	public TwoDomainMCF(List<Domain> domains) {
		super(domains);
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		
		Map<BandwidthDemand, BandwidthResource> mapping = new HashMap<BandwidthDemand, BandwidthResource>();
		
		//link cost, hop
		Transformer<SubstrateLink, Double> weightTrans = new Transformer<SubstrateLink,Double>(){
			public Double transform(SubstrateLink link){
				//return 1/((BandwidthResource)link.get().get(0)).getAvailableBandwidth();
				return 1.;
			}
		};
		
		Domain domain = domains.get(1);
		Domain domain2 = domains.get(0);
//		Random random = new Random();
//		if(random.nextDouble()<0.5){
//			domain = domains.get(1);
//			domain2 = domains.get(0);
//		}
		
		Collection<VirtualLink> virtualLinks = vNet.getEdges();
		//newVnet stores the local mcf
		Map<Domain, VirtualNetwork> newVnet = new HashMap<Domain, VirtualNetwork>();
		for(Domain d : domains){
			newVnet.put(d, new VirtualNetwork());	//initialize the 2nd mcf
		}
		VirtualNetwork tmpvn = newVnet.get(domain);
		//Create virtual network for each domain, transform virtual link to virtual inter link, this means to add source domain and destination domain.
		for(VirtualLink vlink : virtualLinks){
			VirtualNode vSource = vNet.getEndpoints(vlink).getFirst();
			VirtualNode vDest = vNet.getEndpoints(vlink).getSecond();
			SubstrateNode sSource = nodeMapping.get(vSource);
			SubstrateNode sDest = nodeMapping.get(vDest);

			if(domain.containsVertex(sSource)&&domain.containsVertex(sDest)){	//virtual intra link
				tmpvn.addEdge(vlink, vSource, vDest, EdgeType.UNDIRECTED);
			}
			else if(domain.containsVertex(sSource)||domain.containsVertex(sDest))	//virtual inter link
				tmpvn.addEdge(new VirtualInterLink(vlink,vSource,vDest), vSource, vDest, EdgeType.UNDIRECTED);
		}
		//System.out.println(tmpvn);
		
		
		//Create substrate augmented network for each domain, determine intra substrate links, inter substrate link, augmented links
		AugmentedNetwork an = new AugmentedNetwork(domain);	//intra substrate links
		for(InterLink tmplink : domain.getInterLink()){
			an.addEdge(tmplink, tmplink.getNode1(), tmplink.getNode2(), EdgeType.UNDIRECTED);	//inter substrate links
		}
		
		//Each augmented link corresponds to a virtual inter link
		for(VirtualLink vl : tmpvn.getEdges()){
			if(vl instanceof VirtualInterLink){
				VirtualInterLink vil = (VirtualInterLink) vl;
				VirtualNode vnode1 = vil.getNode1();
				VirtualNode vnode2 = vil.getNode2();
				SubstrateNode snode1 = nodeMapping.get(vnode1);
				SubstrateNode snode2 = nodeMapping.get(vnode2);
				SubstrateNode dijkDest = null, dijkSource = null;
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
					if(domain.containsVertex(ilink.getNode1()))
						dijkSource = ilink.getNode2();
					else if(domain.containsVertex(ilink.getNode2()))
						dijkSource = ilink.getNode1();
					
					if(exterDomain.containsVertex(dijkSource)&&
							(!dijkSource.equals(dijkDest))&&			//mapped substrate node is the border node
							(!an.existLink(dijkSource, dijkDest))){		//augmented link does not exist in the augmented network
						
						DijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode, SubstrateLink>(exterDomain,weightTrans);
						AugmentedLink al = new AugmentedLink();
						double cost = (double) dijkstra.getDistance(dijkSource, dijkDest);
						//System.out.println(cost);
						al.setCost(cost);
						al.addResource(100/(cost));	//normally random(0,1), here random = 100 means that it has infinite bw
						an.addEdge(al, dijkSource, dijkDest, EdgeType.UNDIRECTED);	//augmented links
						
					}
					
				}
			}
		}
		//System.out.println(an);
		
		//first mcf
		//if there is no virtual links in this domain
		if(tmpvn.getEdgeCount()!=0){
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
			
			//update in the first domain and create virtual links for second domain
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
				BandwidthDemand bwDem=null,newBwDem;
				BandwidthResource tmpbd=null;
				Domain exterDomain = null;
				for(AbstractDemand dem : tmpvl){
					if (dem instanceof BandwidthDemand) {
						bwDem = (BandwidthDemand) dem;
						break;
					}
				}
				
				
				if(tmpsl instanceof AugmentedLink){
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
					//add augmented virtual link to second domain
					newVnet.get(exterDomain).addEdge(newVLink, newVNode, dstVnode, EdgeType.UNDIRECTED);
				}
				else {
					//update
					if(tmpvl instanceof VirtualInterLink){
						VirtualInterLink tmpvil = (VirtualInterLink) tmpvl;
						newBwDem = new BandwidthDemand(tmpvil.getOrigLink());
					}
					else{
						newBwDem = new BandwidthDemand(tmpvl);						
					}
					newBwDem.setDemandedBandwidth(bwDem.getDemandedBandwidth()*flow);
					for(AbstractResource absRes : tmpsl){
						if(absRes instanceof BandwidthResource){
							tmpbd = (BandwidthResource) absRes;
						}
					}
					if(!NodeLinkAssignation.vlmSingleLinkSimple(newBwDem, tmpsl)){
						throw new AssertionError("But we checked before!");
					}
					else{
						mapping.put(newBwDem, tmpbd);
					}
				}
			}
		}
		
		
		//second domain
		VirtualNetwork tmpvn2 = newVnet.get(domain2);
		if(tmpvn2.getEdgeCount()!=0){
			MultiCommodityFlow mcf2 = new MultiCommodityFlow(domain2);
			Map<String, String> solution = mcf2.linkMappingWithoutUpdate(tmpvn2, nodeMapping);
			
			if(solution.size()==0){
				System.out.println("2nd mcf link no solution");
				for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
					NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
				}
				for(Map.Entry<BandwidthDemand, BandwidthResource> en : mapping.entrySet()){
					en.getKey().free(en.getValue());
				}
				return false;
			}
			//update
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
				
				srcVnode = tmpvn2.getNodeFromID(srcVnodeId);
				dstVnode = tmpvn2.getNodeFromID(dstVnodeId);
				srcSnode = domain2.getNodeFromID(srcSnodeId);
				dstSnode = domain2.getNodeFromID(dstSnodeId);
				
				VirtualLink tmpvl = tmpvn2.findEdge(srcVnode, dstVnode);
				SubstrateLink tmpsl = domain2.findEdge(srcSnode, dstSnode);
				BandwidthDemand bwDem=null,newBwDem;
				for(AbstractDemand dem : tmpvl){
					if (dem instanceof BandwidthDemand) {
						bwDem = (BandwidthDemand) dem;
						break;
					}
				}
				
				if(tmpvl instanceof AugmentedVirtualLink)
					newBwDem = new BandwidthDemand(((AugmentedVirtualLink) tmpvl).getOriginalVL());
				else
					newBwDem = new BandwidthDemand(tmpvl);
				newBwDem.setDemandedBandwidth(bwDem.getDemandedBandwidth()*flow);
				
				if(!NodeLinkAssignation.vlmSingleLinkSimple(newBwDem, tmpsl)){
					throw new AssertionError("But we checked before!");
				}
			}
		}
		
		return true;
	}

}
