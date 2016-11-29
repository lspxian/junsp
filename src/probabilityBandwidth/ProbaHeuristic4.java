package probabilityBandwidth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Predicate;

import edu.uci.ics.jung.algorithms.filters.EdgePredicateFilter;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import li.SteinerTree.ProbaCost;
import vnreal.algorithms.AbstractLinkMapping;
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

public class ProbaHeuristic4 extends AbstractLinkMapping {
	private DijkstraShortestPath<SubstrateNode,SubstrateLink> dijkstra;
	private Map<SubstrateLink, Double> initialProbability;
	private List<VirtualLink> virtualLinks;
	private Map<BandwidthDemand, BandwidthResource> mapping;
	
	public ProbaHeuristic4(SubstrateNetwork sNet) {
		super(sNet);
		this.initialProbability = new HashMap<SubstrateLink, Double>();
		this.mapping = new HashMap<BandwidthDemand, BandwidthResource>();
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		
		this.virtualLinks=new ArrayList<VirtualLink>(vNet.getEdges());
		double averageDem = 0;
		for(VirtualLink vl : vNet.getEdges()){
			averageDem += vl.getBandwidthDemand().getDemandedBandwidth();
		}
		averageDem = averageDem/vNet.getEdgeCount();
		while(!virtualLinks.isEmpty()){
			double minCost = 1000;
			VirtualLink minvl=null;
			List<SubstrateLink> minPath=null;
			for(VirtualLink vl : virtualLinks){
				EdgePredicateFilter<SubstrateNode,SubstrateLink> filter = new EdgePredicateFilter<SubstrateNode,SubstrateLink>(
						new Predicate<SubstrateLink>() {
							@Override
							public boolean evaluate(SubstrateLink sl) {
						
								BandwidthResource bdsrc = sl.getBandwidthResource();
								BandwidthDemand bwd = vl.getBandwidthDemand();
								if(bdsrc.getAvailableBandwidth()< bwd.getDemandedBandwidth())
									return false;
								return true;
							}
						});
				Graph<SubstrateNode, SubstrateLink> tmpG = filter.transform(this.sNet);
				this.dijkstra = new DijkstraShortestPath<SubstrateNode,SubstrateLink> (tmpG, new ProbaCost());
				SubstrateNode snode = nodeMapping.get(vNet.getEndpoints(vl).getFirst());
				SubstrateNode dnode = nodeMapping.get(vNet.getEndpoints(vl).getSecond());

				List<SubstrateLink> tempPath = this.dijkstra.getPath(snode,dnode);
				
				
				if(tempPath.isEmpty()){
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
					
//					System.out.println("link no resource");
					return false;
				}
				else{
					double tempCost = (double) this.dijkstra.getDistance(snode, dnode);
					double demandBW = vl.getBandwidthDemand().getDemandedBandwidth();
					tempCost = tempCost/1e-5 - (demandBW-averageDem)/averageDem*5;
					
					if(tempCost<minCost){
						minCost = tempCost;
						minPath = new ArrayList<SubstrateLink>(tempPath);
						minvl = vl;
					}
				}
			}
			
			this.virtualLinks.remove(minvl);
			for(SubstrateLink sl : minPath){
				String str = "vs"+vNet.getEndpoints(minvl).getFirst().getId()+
						"vd"+vNet.getEndpoints(minvl).getSecond().getId()+
						"ss"+this.sNet.getEndpoints(sl).getFirst().getId()+"sd"+this.sNet.getEndpoints(sl).getSecond().getId();
				System.out.println(str+" "+minCost);
				if(!this.initialProbability.containsKey(sl)){
					this.initialProbability.put(sl, sl.getProbability());
					sl.setProbability(1e-10);
//					sl.setProbability(0.0);
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
		
		double temproba=1;
		for(Map.Entry<SubstrateLink, Double> entry : this.initialProbability.entrySet()){
			temproba = temproba * (1-entry.getValue());
			entry.getKey().setProbability(entry.getValue());
		}
		this.probability = 1 - temproba;
		
		return true;
	}

}

