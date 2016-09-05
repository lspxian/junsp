package probabilityBandwidth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Predicate;

import edu.uci.ics.jung.algorithms.filters.EdgePredicateFilter;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import li.SteinerTree.KMB1981;
import li.SteinerTree.KMB1981V2;
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

public class ProbaHeuristic1 extends AbstractProbaLinkMapping {
	private DijkstraShortestPath<SubstrateNode,SubstrateLink> dijkstra;
	private List<SubstrateLink> mappingLinks;
	private List<VirtualLink> virtualLinks;
	private Map<BandwidthDemand, BandwidthResource> mapping;
	
	public ProbaHeuristic1(SubstrateNetwork sNet) {
		super(sNet);
		this.mappingLinks = new ArrayList<SubstrateLink>();
		this.mapping = new HashMap<BandwidthDemand, BandwidthResource>();
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		
		KMB1981 kmb = new KMB1981(nodeMapping.values(),this.sNet,new ProbaCost());
//		KMB1981V2 kmb = new KMB1981V2(nodeMapping.values(),this.sNet,new ProbaCost());
		kmb.runSteinerTree();
		SubstrateNetwork steiner = kmb.getSteinerTree();
		this.dijkstra = new DijkstraShortestPath<SubstrateNode,SubstrateLink> (steiner, new ProbaCost());
		
		this.virtualLinks=new ArrayList<VirtualLink>(vNet.getEdges());
		Collections.sort(this.virtualLinks,new PbbwComparator(dijkstra,nodeMapping,vNet));
		for(VirtualLink vl: this.virtualLinks){
			SubstrateNode snode = nodeMapping.get(vNet.getEndpoints(vl).getFirst());
			SubstrateNode dnode = nodeMapping.get(vNet.getEndpoints(vl).getSecond());
			List<SubstrateLink> path = this.dijkstra.getPath(snode,dnode);
			double tempCost = (double) this.dijkstra.getDistance(snode, dnode);
			
			for(SubstrateLink sl : path){
				String str = "vs"+vNet.getEndpoints(vl).getFirst().getId()+
						"vd"+vNet.getEndpoints(vl).getSecond().getId()+
						"ss"+this.sNet.getEndpoints(sl).getFirst().getId()+"sd"+this.sNet.getEndpoints(sl).getSecond().getId();
				System.out.println(str+" "+tempCost);
				if(!this.mappingLinks.contains(sl)){
					this.mappingLinks.add(sl);
				}
				BandwidthDemand newBw = new BandwidthDemand(vl);
				newBw.setDemandedBandwidth(vl.getBandwidthDemand().getDemandedBandwidth());
				this.mapping.put(newBw, sl.getBandwidthResource());
				//update resource
				if(!NodeLinkAssignation.vlmSingleLinkSimple(vl.getBandwidthDemand(), sl)){
					throw new AssertionError("But we checked before!");
				}
				
			}
		}
		
		
		double temproba=1;
		for(SubstrateLink sl:this.mappingLinks){
			temproba = temproba * (1-sl.getProbability());
		}
		this.probability = 1 - temproba;
		
		return true;
	}

}
