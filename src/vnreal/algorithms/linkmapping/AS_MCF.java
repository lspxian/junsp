package vnreal.algorithms.linkmapping;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.collections15.Transformer;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.util.EdgeType;
import li.multiDomain.Domain;
import vnreal.algorithms.AbstractMultiDomainLinkMapping;
import vnreal.algorithms.utils.MiscelFunctions;
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

// multi-domains, autonomous system multi-commodity flow algorithm using defautl mcf
//问题在于默认的mcf不能区分augmented link属于哪一个virtual link,所以可能乱用
public class AS_MCF extends AbstractMultiDomainLinkMapping {

	public AS_MCF(List<Domain> domains){
		super(domains);
	}
	
	@Override
	public boolean linkMapping(VirtualNetwork vNet,
			Map<VirtualNode, SubstrateNode> nodeMapping) {

		//link cost, hop
		Transformer<SubstrateLink, Double> weightTrans = new Transformer<SubstrateLink,Double>(){
			public Double transform(SubstrateLink link){
				//return 1/((BandwidthResource)link.get().get(0)).getAvailableBandwidth();
				return 1.;
			}
		};
		//newVnet stores the 2nd local mcf
		Map<Domain, VirtualNetwork> newVnet = new HashMap<Domain, VirtualNetwork>();
		for(Domain d : domains){
			newVnet.put(d, new VirtualNetwork());	//initialize the 2nd mcf
		}
		
		Collection<VirtualLink> virtualLinks = vNet.getEdges();
		//for(Domain domain : domains){
		for(int i=0;i<domains.size();i++){
			Domain domain = domains.get(i);
			//in each domain, 
			//Create substrate augmented network for each domain, determine intra substrate links, inter substrate link, augmented links
			AugmentedNetwork an = new AugmentedNetwork(domain);	//intra substrate links
			for(InterLink tmplink : domain.getInterLink()){
				an.addEdge(tmplink, tmplink.getNode1(), tmplink.getNode2(), EdgeType.UNDIRECTED);	//inter substrate links
			}
			
			//Create virtual network for each domain, transform virtual link to virtual inter link, this means to add source domain and destination domain.
			VirtualNetwork tmpvn = new VirtualNetwork();
			for(VirtualLink vlink : virtualLinks){
				VirtualNode vSource = vNet.getEndpoints(vlink).getFirst();
				VirtualNode vDest = vNet.getEndpoints(vlink).getSecond();
				SubstrateNode sSource = nodeMapping.get(vSource);
				SubstrateNode sDest = nodeMapping.get(vDest);

				vlink.getSolution().put(domain, new TreeMap<SubstrateLink, Double>());	//initialize solution
				
				if(domain.containsVertex(sSource)&&domain.containsVertex(sDest)){	//virtual intra link
					vlink.getSolution().put(an, new TreeMap<SubstrateLink, Double>());
					tmpvn.addEdge(vlink, vSource, vDest, EdgeType.UNDIRECTED);
					newVnet.get(domain).addEdge(vlink, vSource, vDest, EdgeType.UNDIRECTED);
					//virtualLinks.remove(vlink);
				}
				else if(domain.containsVertex(sSource)||domain.containsVertex(sDest))	//virtual inter link
					tmpvn.addEdge(new VirtualInterLink(vlink,vSource,vDest), vSource, vDest, EdgeType.UNDIRECTED);
			}
			//System.out.println(tmpvn);

			//Each augmented link corresponds to a virtual inter link
			for(VirtualLink vl : tmpvn.getEdges()){
				if(vl instanceof VirtualInterLink){
					VirtualInterLink vil = (VirtualInterLink) vl;
					VirtualNode vnode1 = vil.getNode1();
					VirtualNode vnode2 = vil.getNode2();
					SubstrateNode dijkDest = null, dijkSource = null;
					if(vnode1.getDomain().equals(domain)){}
					else if(vnode2.getDomain().equals(domain)){
						VirtualNode tmpnode = vnode2;
						vnode2 = vnode1;
						vnode1 = tmpnode;
					}
					else	continue;
					
					Domain exterDomain = vnode2.getDomain();
					dijkDest = nodeMapping.get(vnode2);;
					for(InterLink ilink : domain.getInterLink()){
						if(domain.containsVertex(ilink.getNode1()))
							dijkSource = ilink.getNode2();
						else if(domain.containsVertex(ilink.getNode2()))
							dijkSource = ilink.getNode1();
						
						if(exterDomain.containsVertex(dijkSource)&&
								(!dijkSource.equals(dijkDest))&&			//mapped substrate node is the border node
								(!an.existLink(dijkSource, dijkDest,vnode2))){		//augmented link does not exist in the augmented network
							
							DijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode, SubstrateLink>(exterDomain,weightTrans);
							AugmentedLink al = new AugmentedLink(vnode2);	//TODO
							double cost = (double) dijkstra.getDistance(dijkSource, dijkDest);
							//System.out.println(cost);
							al.addResource(100/(cost));	//normally random(0,1), here random = 100 means that it has infinite bw
							an.addEdge(al, dijkSource, dijkDest, EdgeType.UNDIRECTED);	//augmented links
							
						}
						
					}
				}
			}
			//System.out.println(an);
			
			//first mcf
			MultiCommodityFlow mcf = new MultiCommodityFlow(an);
			//the nodemapping here is original for all the nodes in all domains. 
			if(tmpvn.getEdgeCount()==0)	continue;	//if there is no virtual links in this domain
			Map<String, String> solution = mcf.linkMappingWithoutUpdate(tmpvn, nodeMapping);
			if(solution.size()==0){
				System.out.println("link no solution");
				for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
					NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
				}
				return false;
			}
			//System.out.println(solution);
			
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
				
				if(!(tmpvl instanceof VirtualInterLink)){
					//for the first solution of an virtual intra link, use augmented network as key
					
					tmpvl.getSolution().get(an).put(tmpsl, bwDem.getDemandedBandwidth()*flow);
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
					tmpvil.getOrigLink().getSolution().get(domain).put(tmpsl, bwDem.getDemandedBandwidth()*flow);	//original virtual link
				}
				
			}
		
		}
		
		// 2nd mcf
		
		for(Map.Entry<Domain, VirtualNetwork> e : newVnet.entrySet()){
			Domain domain = e.getKey();
			VirtualNetwork tmpvn = e.getValue();
			if(tmpvn.getEdgeCount()==0)		continue;	//if there is no virtual links in this domain
			MultiCommodityFlow mcf = new MultiCommodityFlow(domain);
			Map<String, String> solution = mcf.linkMappingWithoutUpdate(tmpvn, nodeMapping);
			
			//if 2nd mcf no solution, don't return, but clear original link solution, 
			if(solution.size()==0){
				System.out.println("2nd mcf link no solution");
				for(VirtualLink tmpvl : tmpvn.getEdges()){
					if(tmpvl instanceof AugmentedVirtualLink){
						AugmentedVirtualLink augmentedvl = (AugmentedVirtualLink) tmpvl;
						augmentedvl.getOriginalVL().getSolution().get(
								augmentedvl.getOriginalDomain()).clear();
					}
				}
				
			}
			
			VirtualNode srcVnode = null, dstVnode = null;
			SubstrateNode srcSnode = null,dstSnode = null;
			int srcVnodeId, dstVnodeId, srcSnodeId, dstSnodeId;
			BandwidthDemand bwDem=null;
			
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
				
				for(AbstractDemand dem : tmpvl){
					if (dem instanceof BandwidthDemand) {
						bwDem = (BandwidthDemand) dem;
						break;
					}
				}
				
				if(tmpvl instanceof AugmentedVirtualLink){
					AugmentedVirtualLink augmentedvl = (AugmentedVirtualLink) tmpvl;
					augmentedvl.getOriginalVL().getSolution().get(
							augmentedvl.getOriginalDomain()).put(tmpsl, bwDem.getDemandedBandwidth()*flow);
					
					//merge two flows TODO test
					Map<SubstrateLink, Double> tmpMap = augmentedvl.getOriginalVL().getSolution().get(augmentedvl.getOriginalDomain());
					if(tmpMap.containsKey(tmpsl)){
						tmpMap.put(tmpsl, tmpMap.get(tmpsl)+bwDem.getDemandedBandwidth()*flow);
					}
					else{
						tmpMap.put(tmpsl, bwDem.getDemandedBandwidth()*flow);
					}
					
				}
				else {
					//for the second time, use domain as key, intra virtual link mapping 
					tmpvl.getSolution().get(domain).put(tmpsl, bwDem.getDemandedBandwidth()*flow);
				}
			}
		}
		
		/*
		for(VirtualLink vl : vNet.getEdges()){
			System.out.println(vl);
			Map<SubstrateLink, Double> flows = vl.getMinCost();
			for(Map.Entry<SubstrateLink, Double> e : flows.entrySet()){
				System.out.println(e.getKey());
				System.out.println(e.getValue());
			}
		}*/
		
		
		Map<BandwidthDemand, BandwidthResource> mapping = new HashMap<BandwidthDemand, BandwidthResource>();
		BandwidthResource tmpbd=null;
		//compare 2 mcf results to get a better solution, update resource
		for(VirtualLink vl : vNet.getEdges()){
			BandwidthDemand newBwDem;
			
			if(vl.getMinCostValue()==10000){
				System.out.println("all 2nd mcf no solution, link mapping no solution. \n");
				for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
					NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
				}
				return false;
			}
			
			System.out.println(vl);		//print vl
			
			Map<SubstrateLink, Double> flows = vl.getMinCostMap();
			
			//inter links will not be visited twice, so there is just one occupy
			for(Map.Entry<SubstrateLink, Double> e : flows.entrySet()){
			
				System.out.println(e.getKey().toString()+" "+e.getValue());
				
				for(AbstractResource absRes : e.getKey()){
					if(absRes instanceof BandwidthResource){
						tmpbd = (BandwidthResource) absRes;
					}
				}
				
				newBwDem = new BandwidthDemand(vl);
				newBwDem.setDemandedBandwidth(MiscelFunctions
						.roundThreeDecimals(e.getValue()));
				
				if(!NodeLinkAssignation.vlmSingleLinkSimple(newBwDem, e.getKey())){	//update
//					throw new AssertionError("But we checked before!");
					System.out.println("flow colision, reject !");
					
					for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
						NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
					}
					for(Map.Entry<BandwidthDemand, BandwidthResource> en : mapping.entrySet()){
						en.getKey().free(en.getValue());
					}
					return false;
				}
				else{
					mapping.put(newBwDem, tmpbd);
				}
				
			}
		}
		
		return true;
	}

}
