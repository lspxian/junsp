package vnreal.algorithms.linkmapping;

import java.util.ArrayList;
import java.util.HashMap;
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

	public Shen2014(List<Domain> domains) {
		super(domains);
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		
		Map<Domain, VirtualNetwork> newVnet = new HashMap<Domain, VirtualNetwork>();
		for(Domain d : domains){
			newVnet.put(d, new VirtualNetwork());	//initialize the local mcf
		}
		
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
			vl.getSolution().put(newDomain, new TreeMap<SubstrateLink,Double>());	//initialize solution
			//intra virtual link
			if(d1.equals(d2))
				newVnet.get(d1).addEdge(vl, v1, v2, EdgeType.UNDIRECTED);
			else{
				// inter virtual link
				List<SubstrateLink> path = constraintShortestPath(newDomain, nodeMapping.get(v1), nodeMapping.get(v2), vl);
				
				//inter link no resource
				if(path.isEmpty()){
					System.out.println("Inter link no resource");
					for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
						NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
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
			
				for(SubstrateLink sl : path){
					if(sl instanceof InterLink){
						
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
							newVnet.get(d1).addEdge(newVLink, tmp, v1, EdgeType.UNDIRECTED);
						}
						if(!nodeMapping.get(v2).equals(border2)){
							tmp = new VirtualNode();	//augmented node
							nodeMapping.put(tmp, border2);
							//second augmented virtual link
							newVLink = new AugmentedVirtualLink(d1,vl);
							bw=new BandwidthDemand(newVLink);
							bw.setDemandedBandwidth(bwd.getDemandedBandwidth());
							newVLink.add(bw);
							newVnet.get(d2).addEdge(newVLink, tmp, v2, EdgeType.UNDIRECTED);	//augmented virtual link						
						}
						
						vl.getSolution().get(newDomain).put(sl, bwd.getDemandedBandwidth());
						System.out.println("inter link : "+sl);
						
					}
				}
			}
			
		}
		
		//mcf
		for(Map.Entry<Domain, VirtualNetwork> e : newVnet.entrySet()){
			Domain domain = e.getKey();
			VirtualNetwork tmpvn = e.getValue();
			if(tmpvn.getEdgeCount()==0)		continue;	//if there is no virtual links in this domain
			MultiCommodityFlow mcf = new MultiCommodityFlow(domain);
			Map<String, String> solution = mcf.linkMappingWithoutUpdate(tmpvn, nodeMapping);
			if(solution.size()==0){
				System.out.println("link no solution");
				for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
					NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
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
							newDomain).put(tmpsl, bwDem.getDemandedBandwidth()*flow);
				}
				else {
					//for the second time, use domain as key, intra virtual link mapping 
					tmpvl.getSolution().get(newDomain).put(tmpsl, bwDem.getDemandedBandwidth()*flow);
				}
			}
		}
		
		//update resource
		for(VirtualLink vl : vNet.getEdges()){
			BandwidthDemand newBwDem;
			Map<SubstrateLink, Double> flows = vl.getSolution().get(newDomain);
			
			for(Map.Entry<SubstrateLink, Double> e : flows.entrySet()){
			
				newBwDem = new BandwidthDemand(vl);
				newBwDem.setDemandedBandwidth(MiscelFunctions
						.roundThreeDecimals(e.getValue()));
				if(!NodeLinkAssignation.vlmSingleLinkSimple(newBwDem, e.getKey())){
					for(Map.Entry<SubstrateLink, Double> es : flows.entrySet()){
						System.out.println(es.getKey()+" "+es.getValue());
						
					}
					throw new AssertionError("But we checked before!");
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

	private List<SubstrateLink> constraintShortestPath(Domain newDomain, SubstrateNode substrateNode,
			SubstrateNode substrateNode2, VirtualLink vl) {

		Transformer<SubstrateLink, Double> weightTrans = new Transformer<SubstrateLink,Double>(){
			public Double transform(SubstrateLink link){
				if(link instanceof InterLink){
					for(AbstractResource ares : link){
						if(ares instanceof BandwidthResource){
							BandwidthResource bwres = (BandwidthResource)ares;
							return 100/bwres.getAvailableBandwidth();
						}
					}
				}
				return 1.;
			}
		};
		
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
		DijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode, SubstrateLink>(tmp,weightTrans);
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
