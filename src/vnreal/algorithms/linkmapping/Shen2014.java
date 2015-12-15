package vnreal.algorithms.linkmapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.filters.EdgePredicateFilter;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import li.multiDomain.Domain;
import vnreal.algorithms.AbstractMultiDomainLinkMapping;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.AugmentedNetwork;
import vnreal.network.substrate.InterLink;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.AugmentedVirtualLink;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;

public class Shen2014 extends AbstractMultiDomainLinkMapping {
	Map<Domain, VirtualNetwork> localVNets;
	Map<BandwidthDemand, BandwidthResource> mapping;

	public Shen2014(List<Domain> domains) {
		super(domains);
		initialize();
	}
	
	private void initialize(){
		this.localVNets = new HashMap<Domain, VirtualNetwork>();
		for(Domain d : domains){
			this.localVNets.put(d, new VirtualNetwork());	//initialize the local mcf
		}
		this.mapping = new LinkedHashMap<BandwidthDemand, BandwidthResource>();
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		
		if(!createLocalVNet(vNet,nodeMapping)){
			return false;
		}
		
		//mcf
		for(Map.Entry<Domain, VirtualNetwork> e : this.localVNets.entrySet()){
			Domain domain = e.getKey();
			VirtualNetwork tmpvn = e.getValue();
			if(tmpvn.getEdgeCount()==0)		continue;	//if there is no virtual links in this domain
			//System.out.println(tmpvn);
			this.localPath="tmp/Shen2014-"+vNet.getId()+"-"+domain.getId()+".lp";	//TODO
			MultiCommodityFlow mcf = new MultiCommodityFlow(domain,this.localPath,this.remotePath);
			Map<String, String> solution = mcf.linkMappingWithoutUpdate(tmpvn, nodeMapping);
			if(solution.size()==0){
				System.out.println("link no solution");
				for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
					NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
				}
				
				//free inter link resource that are attributed by dijkstra 
				//and intra link resources already distributed !!!
				for(Map.Entry<BandwidthDemand, BandwidthResource> entry : mapping.entrySet()){
					entry.getKey().free(entry.getValue());
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
			
			BandwidthResource tmpbd = null;
			BandwidthDemand bwDem=null, newDem=null;
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
				
				for(AbstractDemand dem : tmpvl){
					if (dem instanceof BandwidthDemand) {
						bwDem = (BandwidthDemand) dem;
						break;
					}
				}
				if(tmpvl instanceof AugmentedVirtualLink){
					newDem = new BandwidthDemand(((AugmentedVirtualLink) tmpvl).getOriginalVL());
				}else{
					newDem = new BandwidthDemand(tmpvl);
				}
				newDem.setDemandedBandwidth(bwDem.getDemandedBandwidth()*flow);
				
				if(!NodeLinkAssignation.vlmSingleLinkSimple(newDem, tmpsl)){
					throw new AssertionError("But we checked before!");
				}
				else{
					for(AbstractResource absRes : tmpsl){
						if(absRes instanceof BandwidthResource){
							tmpbd = (BandwidthResource) absRes;
							break;
						}
					}
					mapping.put(newDem, tmpbd);
				}
			}
		}
		
		//restore node coordinate to [0,100]
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
	
	private boolean createLocalVNet(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping){
		Domain newDomain = merge(domains);
		
		for(VirtualLink vl : vNet.getEdges()){
			VirtualNode v1 = vNet.getEndpoints(vl).getFirst();
			VirtualNode v2 = vNet.getEndpoints(vl).getSecond();
			Domain d1 = v1.getDomain();
			Domain d2 = v2.getDomain();
			SubstrateNode border1=null, border2=null;

			BandwidthDemand bwd = null;
			for(AbstractDemand abd : vl){
				if(abd instanceof BandwidthDemand){
					bwd = (BandwidthDemand)abd;
					break;
				}
			}
			//intra virtual link
			if(d1.equals(d2))
				this.localVNets.get(d1).addEdge(vl, v1, v2, EdgeType.UNDIRECTED);
			else{
				// inter virtual link
				List<SubstrateLink> path = constraintShortestPath(newDomain, nodeMapping.get(v1), nodeMapping.get(v2), vl);
				
				//inter link no resource
				if(path.isEmpty()){
					System.out.println("Inter link no resource");
					for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
						NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
					}
					for(Map.Entry<BandwidthDemand, BandwidthResource> entry : mapping.entrySet()){
						entry.getKey().free(entry.getValue());
					}
					//restore node coordinate to [0,100]
					for(Domain d : domains){
						for(SubstrateNode snode : d.getVertices()){
							double x = snode.getCoordinateX()-d.getCoordinateX()*100;
							double y = snode.getCoordinateY()-d.getCoordinateY()*100;
							snode.setCoordinateX(x);
							snode.setCoordinateY(y);
						}
					}
					return false;
				}
				
				//create augmented link for the substrate links on the path
				for(SubstrateLink sl : path){
					if(sl instanceof InterLink){
						//get border nodes
						if(d1.containsVertex(((InterLink) sl).getNode1())){ 
							border1 = ((InterLink) sl).getNode1();
							border2 = ((InterLink) sl).getNode2();
						}
						else{
							border2 = ((InterLink) sl).getNode1();
							border1 = ((InterLink) sl).getNode2();
						}
						VirtualNode tmp = null;
						AugmentedVirtualLink newVLink=null;
						BandwidthDemand bw = null;
						//substrate node that virtual node maps to may be the border node(augmented node).
						//in this case, don't create virtual node and augmented virtual link!!!
						//in as mcf this is done by augmented link, before the creation of augmented node
						if(!nodeMapping.get(v1).equals(border1)){	
							tmp = new VirtualNode();	//augmented node
							nodeMapping.put(tmp, border1);
							//first augmented virtual link
							newVLink = new AugmentedVirtualLink(d1,vl);
							bw=new BandwidthDemand(newVLink);
							bw.setDemandedBandwidth(bwd.getDemandedBandwidth());
							newVLink.add(bw);
							this.localVNets.get(d1).addEdge(newVLink, tmp, v1, EdgeType.UNDIRECTED);
						}
						if(!nodeMapping.get(v2).equals(border2)){
							tmp = new VirtualNode();	//augmented node
							nodeMapping.put(tmp, border2);
							//second augmented virtual link
							newVLink = new AugmentedVirtualLink(d2,vl);
							bw=new BandwidthDemand(newVLink);
							bw.setDemandedBandwidth(bwd.getDemandedBandwidth());
							newVLink.add(bw);
							this.localVNets.get(d2).addEdge(newVLink, tmp, v2, EdgeType.UNDIRECTED);	//augmented virtual link						
						}
						System.out.println("Virtuallink("+vl.getId()+")"+" use "+sl);
						
						//update resource on the inter link
						if(!NodeLinkAssignation.vlmSingleLinkSimple(bwd, sl)){
							throw new AssertionError("But we checked before!");
						}
						else{
							BandwidthResource tmpbd=null;
							for(AbstractResource absRes : sl){
								if(absRes instanceof BandwidthResource){
									tmpbd = (BandwidthResource) absRes;
									break;
								}
							}
							mapping.put(bwd, tmpbd);
						}
					}
				}
			}
			
		}
		return true;
	}

	private List<SubstrateLink> constraintShortestPath(Domain newDomain, SubstrateNode substrateNode,
			SubstrateNode substrateNode2, VirtualLink vl) {
		//block the links without enough available capacities
		//TODO!!!!
		EdgePredicateFilter<SubstrateNode,SubstrateLink> filter = new EdgePredicateFilter<SubstrateNode,SubstrateLink>(
				new Predicate<SubstrateLink>() {
					@Override
					public boolean evaluate(SubstrateLink sl) {
						if(sl instanceof InterLink){
							InterLink il = (InterLink) sl;
							BandwidthResource bdsrc = null;
							for(AbstractResource asrc : il)
								if(asrc instanceof BandwidthResource){
									bdsrc = (BandwidthResource) asrc;
									break;
								}
							BandwidthDemand bwd = null;
							for(AbstractDemand abd : vl){
								if(abd instanceof BandwidthDemand){
									bwd = (BandwidthDemand)abd;
									break;
								}
							}
							if(bdsrc.getAvailableBandwidth()< bwd.getDemandedBandwidth())
								return false;
						}
						return true;
					}
				});
		Graph<SubstrateNode, SubstrateLink> tmp = filter.transform(newDomain);
		DijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode, SubstrateLink>(tmp);	//dijkstra weight=1
		return dijkstra.getPath(substrateNode, substrateNode2);
		
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
