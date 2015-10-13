package vnreal.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.util.EdgeType;
import li.multiDomain.Domain;
import vnreal.algorithms.linkmapping.MultiCommodityFlow;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.network.substrate.AugmentedLink;
import vnreal.network.substrate.AugmentedNetwork;
import vnreal.network.substrate.InterLink;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
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
		
		Transformer<SubstrateLink, Double> weightTrans = new Transformer<SubstrateLink,Double>(){
			public Double transform(SubstrateLink link){
				//return 1/((BandwidthResource)link.get().get(0)).getAvailableBandwidth();
				return 1.;
			}
		};
		
		Collection<VirtualLink> virtualLinks = vNet.getEdges();
		for(Domain domain : domains){
			//Create virtual network for each domain, transform virtual link to virtual inter link, this means to add source domain and destination domain.
			VirtualNetwork tmpvn = new VirtualNetwork(1);
			for(VirtualLink vlink : virtualLinks){
				VirtualNode vSource = vNet.getEndpoints(vlink).getFirst();
				VirtualNode vDest = vNet.getEndpoints(vlink).getSecond();
				SubstrateNode sSource = nodeMapping.get(vSource);
				SubstrateNode sDest = nodeMapping.get(vDest);
				if(domain.containsVertex(sSource)&&domain.containsVertex(sDest)){
					tmpvn.addEdge(vlink, vSource, vDest, EdgeType.UNDIRECTED);
					//virtualLinks.remove(vlink);
				}
				else if(domain.containsVertex(sSource)||domain.containsVertex(sDest))
					tmpvn.addEdge(new VirtualInterLink(vlink,vSource,vDest), vSource, vDest, EdgeType.UNDIRECTED);
			}
			//System.out.println(tmpvn);
			
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
						if(exterDomain.containsVertex(ilink.getExterior())){
							SubstrateNode dijkSource = ilink.getExterior();
							DijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode, SubstrateLink>(exterDomain,weightTrans);
							AugmentedLink al = new AugmentedLink();
							al.addResource(100);	//normally random(0,1), here random = 100 means that it has infinite bw
							double cost = (double) dijkstra.getDistance(dijkSource, dijkDest);
							System.out.println(cost);
							al.setCost(cost);	
							an.addEdge(al, dijkSource, dijkDest, EdgeType.UNDIRECTED);	//augmented links
						}
						
					}
				}
			}
		}
		
	/*	
		
		//use mcf with splitting or without splitting
		for(Domain domain : domains){
			MultiCommodityFlow mcf = new MultiCommodityFlow(an4d.get(domain));
			
			//the nodemapping here is original for all the nodes in all domains. 
			Map<String, String> solution = mcf.linkMappingWithoutUpdate(vn4d.get(domain), nodeMapping);
			if(solution.size()==0){
				System.out.println("link no solution");
				for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
					NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
				}
				return false;
			}
			System.out.println(solution);
			
			//Don't update here
			
			
			
		}
		
		*/
		
		return true;
	}

}
