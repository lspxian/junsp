package vnreal.algorithms.linkmapping;

import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import li.SteinerTree.KMB1981;
import li.SteinerTree.KMB1981V2;
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
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;

public class SteinerTreeHeuristic extends AbstractLinkMapping {
	
	String method;
	SubstrateNetwork steinerTree;
	
	protected SteinerTreeHeuristic(SubstrateNetwork sNet) {
		super(sNet);
		this.method="Takahashi";
	}
	
	public SteinerTreeHeuristic(SubstrateNetwork sNet, String method){
		super(sNet);
		this.method=method;
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		
		if(this.method=="Takahashi"){
			Takahashi ta = new Takahashi(nodeMapping.values(), this.sNet, new ProbaCost());
			ta.runSteinerTree();
			this.steinerTree = ta.getSteinerTree();
		}
		else if(this.method=="KMB1981"){
			KMB1981 kmb = new KMB1981(nodeMapping.values(), this.sNet, new ProbaCost());
			kmb.runSteinerTree();
			this.steinerTree = kmb.getSteinerTree();
		}
		else if(this.method=="KMB1981V2"){
			KMB1981V2 kmb = new KMB1981V2(nodeMapping.values(), this.sNet, new ProbaCost());
			kmb.runSteinerTree();
			this.steinerTree = kmb.getSteinerTree();
		}
		else{
			System.out.println("method not exist!\n");
			return false;
		}
		
		this.probability = computeProbability(this.steinerTree);
		Map<SubstrateLink, BandwidthDemand> pre_allo = new HashMap<SubstrateLink,BandwidthDemand>();
		for(SubstrateLink sl : this.steinerTree.getEdges()){
			pre_allo.put(sl, new BandwidthDemand(sl));
		}
		DijkstraShortestPath<SubstrateNode,SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode,SubstrateLink>(this.steinerTree);
		
		//verify bw
		BandwidthDemand bwDem=null;
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
				BandwidthDemand newbwd = new BandwidthDemand(sl);
				newbwd.setDemandedBandwidth(pre_allo.get(sl).getDemandedBandwidth()+bwDem.getDemandedBandwidth());
				pre_allo.replace(sl, newbwd);
			}
		}
		
		BandwidthResource resource = null;
		for(SubstrateLink sl : this.steinerTree.getEdges()){
			for(AbstractResource res : sl){
				if(res instanceof BandwidthResource){
					resource = (BandwidthResource) res;
					break;
				}
			}
			if(!NodeLinkAssignation.fulfills(pre_allo.get(sl), resource)){
				System.out.println("resource error, substrate link "+sl.getId());
				return false;
			}
		}
		
		//perform link mapping
		for(VirtualLink vl : vNet.getEdges()){
			SubstrateNode sn1 = nodeMapping.get(vNet.getEndpoints(vl).getFirst());
			SubstrateNode sn2 = nodeMapping.get(vNet.getEndpoints(vl).getSecond());
			if(!NodeLinkAssignation.vlmSimple(vl, dijkstra.getPath(sn1, sn2))){
				throw new AssertionError("But we checked before!");
			}
		}
		
		return true;
	}

	public double computeProbability(SubstrateNetwork sn){
		double temproba=1;
		for(SubstrateLink sl: sn.getEdges()){
			temproba = temproba * (1-sl.getProbability());
		}
		return 1-temproba;
	}
}
