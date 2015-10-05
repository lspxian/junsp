package vnreal.algorithms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections15.Transformer;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
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
		
		//Create virtual network for each domain, transform virtual link to virtual inter link, this means to add source domain and destination domain.
		Map<Domain, VirtualNetwork> vn4d = new HashMap<Domain, VirtualNetwork>();
		for(Domain domain : domains){
			vn4d.put(domain,  new VirtualNetwork(1)); //TODO lifetime
		}

		for(VirtualLink vlink: vNet.getEdges()){
			VirtualNode vSource = vNet.getSource(vlink);
			VirtualNode vDest = vNet.getDest(vlink);
			SubstrateNode sSource = nodeMapping.get(vSource);
			SubstrateNode sDest = nodeMapping.get(vDest);
			for(Domain sdomain : domains){
				if(sdomain.containsVertex(sSource)){
					for(Domain ddomain : domains){
						if(ddomain.containsVertex(sDest)){
							if(sdomain.equals(ddomain))
								vn4d.get(sdomain).addEdge(vlink, vSource, vDest);	//virtual intra link
							else{
								VirtualInterLink tmp = (VirtualInterLink)vlink;
								tmp.setsDomain(sdomain);
								tmp.setdDomain(ddomain);
								vn4d.get(sdomain).addEdge(tmp, vSource, vDest);	//virtual inter link
							}
							break;
						}
					}
					break;
				}
			}
		}	
		
		
		Transformer<SubstrateLink, Double> weightTrans = new Transformer<SubstrateLink,Double>(){
			public Double transform(SubstrateLink link){
				return 1/((BandwidthResource)link.get().get(0)).getAvailableBandwidth();
			}
		};
		
		//Create substrate augmented network for each domain, determine intra substrate links, inter substrate link, augmented links
		Map<Domain, AugmentedNetwork> an4d = new HashMap<Domain, AugmentedNetwork>();
		for(Domain domain : domains){
			AugmentedNetwork an = (AugmentedNetwork) domain.getCopy(false);	//intra substrate links
			an.setRoot(domain);
			for(InterLink tmplink : domain.getInterLink()){
				an.addEdge(tmplink, tmplink.getSource(), tmplink.getDestination());	//inter substrate links
			}

			for(VirtualLink vl : vn4d.get(domain).getEdges()){
				if(vl instanceof VirtualInterLink){
					VirtualInterLink vil = (VirtualInterLink) vl;
					for(InterLink ilink : domain.getInterLink()){
						if(ilink.getDestDomain().equals(vil.getdDomain())){	//如果横跨多了domain， 这个条件无法满足，找不到结果
							//dijkstra  
							SubstrateNode sSource = ilink.getDestination();
							SubstrateNode sDest = nodeMapping.get(vNet.getDest(vil));
							DijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode, SubstrateLink>(vil.getdDomain(),weightTrans);
							
							AugmentedLink al = new AugmentedLink(); //TODO capacity resource ?
							al.setPrice((double) dijkstra.getDistance(sSource, sDest));
							
							an.addEdge(al, sSource, sDest);	//augmented links
						}
					}
				}
			}
			
			an4d.put(domain, an);
		}
		
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
			
			//update : its own mcf, others' augmented links
			
			//1) its own mcf result
			
			
			
		}
		
		//update resource
		
		
		return true;
	}

}
