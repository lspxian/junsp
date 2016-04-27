package vnreal.algorithms.linkmapping;

import java.util.Map;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import li.SteinerTree.KMB1981;
import li.SteinerTree.ProbaCost;
import li.SteinerTree.Takahashi;
import vnreal.algorithms.AbstractLinkMapping;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class SteinerTreeHeuristic extends AbstractLinkMapping {
	
	String method;
	SubstrateNetwork steinerTree;
	
	protected SteinerTreeHeuristic(SubstrateNetwork sNet) {
		super(sNet);
		this.method="Takahashi";
	}
	
	protected SteinerTreeHeuristic(SubstrateNetwork sNet, String method){
		super(sNet);
		this.method=method;
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		
		if(this.method=="Takahashi"){
			Takahashi ta = new Takahashi(nodeMapping.values(), this.sNet, new ProbaCost());
			this.steinerTree = ta.getSteinerTree();
		}
		else if(this.method=="KMB1981"){
			KMB1981 kmb = new KMB1981(nodeMapping.values(), this.sNet, new ProbaCost());
			this.steinerTree = kmb.getSteinerTree();
		}
		else{
			System.out.println("method not exist!\n");
			return false;
		}
		
		BandwidthDemand bwDem=null;
		DijkstraShortestPath<SubstrateNode,SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode,SubstrateLink>(this.steinerTree);
		for(VirtualLink vl : vNet.getEdges()){
			
			for (AbstractDemand dem : vl) {
				if (dem instanceof BandwidthDemand) {
					bwDem = (BandwidthDemand) dem;
					break;
				}
			}
			SubstrateNode sn1 = nodeMapping.get(vNet.getEndpoints(vl).getFirst());
			SubstrateNode sn2 = nodeMapping.get(vNet.getEndpoints(vl).getSecond());
			for(SubstrateLink sl : dijkstra.getPath(sn1, sn2)){
				if(!NodeLinkAssignation.vlmSingleLinkSimple(bwDem, sl)){
					
				}
			}
		}
		
		return true;
	}

}
