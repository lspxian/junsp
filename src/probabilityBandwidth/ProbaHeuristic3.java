package probabilityBandwidth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import li.SteinerTree.ProbaCost;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.resources.BandwidthResource;

public class ProbaHeuristic3 extends AbstractProbaLinkMapping {
	private DijkstraShortestPath<SubstrateNode,SubstrateLink> dijkstra;
	private Map<SubstrateLink, Double> initialProbability;
	private List<VirtualLink> virtualLinks;
	private Map<BandwidthDemand, BandwidthResource> mapping;
	
	public ProbaHeuristic3(SubstrateNetwork sNet) {
		super(sNet);
		this.initialProbability = new HashMap<SubstrateLink, Double>();
		this.mapping = new HashMap<BandwidthDemand, BandwidthResource>();
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		
		this.virtualLinks=new ArrayList<VirtualLink>(vNet.getEdges());
		this.dijkstra = new DijkstraShortestPath<SubstrateNode,SubstrateLink> (this.sNet, new ProbaCost());
		
		Collections.sort(this.virtualLinks,new PbbwComparator(dijkstra,nodeMapping,vNet));
			
		for(VirtualLink minvl: this.virtualLinks){
			this.dijkstra = new DijkstraShortestPath<SubstrateNode,SubstrateLink> (this.sNet, new ProbaBWCost(minvl));
			SubstrateNode snode = nodeMapping.get(vNet.getEndpoints(minvl).getFirst());
			SubstrateNode dnode = nodeMapping.get(vNet.getEndpoints(minvl).getSecond());
			double tempCost = (double) this.dijkstra.getDistance(snode, dnode);
			if(tempCost>=1000.0){
				for(Map.Entry<SubstrateLink, Double> entry : this.initialProbability.entrySet()){
					entry.getKey().setProbability(entry.getValue());
				}
				for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
					NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
				}
				//delete resources already distributed !!!
				for(Map.Entry<BandwidthDemand, BandwidthResource> e : mapping.entrySet()){
					e.getKey().free(e.getValue());
				}
				
				System.out.println("link no resource");
				return false;
				
			}else{
				
				for(SubstrateLink sl : this.dijkstra.getPath(snode,dnode)){
					String str = "vs"+vNet.getEndpoints(minvl).getFirst().getId()+
							"vd"+vNet.getEndpoints(minvl).getSecond().getId()+
							"ss"+this.sNet.getEndpoints(sl).getFirst().getId()+"sd"+this.sNet.getEndpoints(sl).getSecond().getId();
					System.out.println(str);
					if(!this.initialProbability.containsKey(sl)){
						this.initialProbability.put(sl, sl.getProbability());
						sl.setProbability(0.0);
					}
					
					BandwidthDemand newBw = new BandwidthDemand(minvl);
					newBw.setDemandedBandwidth(minvl.getBandwidthDemand().getDemandedBandwidth());
					this.mapping.put(newBw, sl.getBandwidthResource());
					//update resource
					if(!NodeLinkAssignation.vlmSingleLinkSimple(minvl.getBandwidthDemand(), sl)){
						throw new AssertionError("But we checked before!");
					}
					
				}
				
			}
		}
		
		double temproba=1;
		for(Map.Entry<SubstrateLink, Double> entry : this.initialProbability.entrySet()){
			temproba = temproba * (1-entry.getValue());
			entry.getKey().setProbability(entry.getValue());
		}
		this.probability = 1 - temproba;
		
		return true;
	}

}
