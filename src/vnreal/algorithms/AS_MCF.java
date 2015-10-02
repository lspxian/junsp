package vnreal.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import li.multiDomain.Domain;
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
				return 1/((BandwidthResource)link.get().get(0)).getAvailableBandwidth();
			}
		};
		
		//augmented network for each domain, each request
		List<AugmentedNetwork> augmentedNets = new ArrayList<AugmentedNetwork>();
		for(Domain domain : domains){
			AugmentedNetwork an = (AugmentedNetwork) domain.getCopy(false);	//intra substrate links
			an.setRoot(domain);
			for(InterLink tmplink : domain.getInterLink()){
				an.addEdge(tmplink, tmplink.getSource(), tmplink.getDestination());	//inter substrate links
			}
			augmentedNets.add(an);
		}
		
		//transform virtual link to virtual inter link, this means to add source domain and destination domain.
		ArrayList<VirtualInterLink> vils = new ArrayList<VirtualInterLink>();
		for(VirtualLink vlink: vNet.getEdges()){
			SubstrateNode sSource = nodeMapping.get(vNet.getSource(vlink));
			SubstrateNode sDest = nodeMapping.get(vNet.getDest(vlink));
			for(Domain sdomain : domains){
				if(sdomain.containsVertex(sSource)){
					for(Domain ddomain : domains){
						if(ddomain.containsVertex(sDest)){
							VirtualInterLink tmp = (VirtualInterLink)vlink;
							tmp.setsDomain(sdomain);
							tmp.setdDomain(ddomain);
							vils.add(tmp);
							break;
						}
					}
					break;
				}
			}
		}
		
		//intra links and partial inter links to map after determination of interface
		Map<Domain, List<VirtualLink>> domainLinks = new HashMap<Domain, List<VirtualLink>>();
		for(Domain domain : domains){
			domainLinks.put(domain, new ArrayList<VirtualLink>());
		}
		
		for(VirtualInterLink vil : vils){
			for(InterLink ilink : vil.getdDomain().getInterLink()){
				if(ilink.getDestDomain().equals(vil.getsDomain())){
					//dijkstra  
					SubstrateNode sSource = ilink.getSource();
					SubstrateNode sDest = nodeMapping.get(vNet.getDest(vil));
					DijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode, SubstrateLink>(vil.getsDomain(),weightTrans);
					
					AugmentedLink al = new AugmentedLink();
					al.setPrice((double) dijkstra.getDistance(sSource, sDest));
					
					for(AugmentedNetwork an : augmentedNets){
						if(an.getRoot().equals(vil.getdDomain())){
							an.addEdge(al, sSource, sDest);	//augmented links
						}
					}
					
				}
			}
			
		}
		
		//MCF mapping 输入参数是网络还是链接
		
		
		
		
		return false;
	}

}
