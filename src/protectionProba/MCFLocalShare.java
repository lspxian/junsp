package protectionProba;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.filters.EdgePredicateFilter;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import vnreal.algorithms.AbstractLinkMapping;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.algorithms.utils.NodeLinkDeletion;
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

public class MCFLocalShare extends AbstractLinkMapping {
	
	private String localPath ;
	protected MCFLocalShare(SubstrateNetwork sNet) {
		super(sNet);
		this.localPath = "cplex/vne-mcf.lp";
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		Map<String, String> solution = linkMappingWithoutUpdateLocal(vNet, nodeMapping);		
		if(solution.size()==0){
			System.out.println("link no solution");
			for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
				NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
			}
			return false;
		}
		//update
		updateResource(vNet, nodeMapping, solution);
		
		return false;
	}

	public Map<String,String> linkMappingWithoutUpdateLocal(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		Map<String, String> solution = new HashMap<String, String>();
		//generate .lp file
		try {
			this.generateFile(vNet, nodeMapping);
			Process p = Runtime.getRuntime().exec("python cplex/mysolver.py "+localPath+" o");
			InputStream in = p.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String readLine;
			boolean solBegin=false;
			while (((readLine = br.readLine()) != null)) {
				if(solBegin==true){
					System.out.println(readLine);
					StringTokenizer st = new StringTokenizer(readLine, " ");
					solution.put(st.nextToken(), st.nextToken());
				}
				if(solBegin==false&&readLine.equals("The solutions begin here : "))
					solBegin=true;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return solution;
	}
	
	public void updateResource(VirtualNetwork vNet,  Map<VirtualNode, SubstrateNode> nodeMapping, Map<String,String> solution){
		BandwidthDemand bwDem = null,newBwDem;
		VirtualNode srcVnode = null, dstVnode = null;
		SubstrateNode srcSnode = null, dstSnode = null;
		int srcVnodeId, dstVnodeId, srcSnodeId, dstSnodeId;
		
		for(Map.Entry<String, String> entry : solution.entrySet()){
			String linklink = entry.getKey();
			double flow = Double.parseDouble(entry.getValue());
			srcVnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("vs")+2, linklink.indexOf("vd")));
			dstVnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("vd")+2, linklink.indexOf("ss")));
			srcSnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("ss")+2, linklink.indexOf("sd")));
			dstSnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("sd")+2));
			//for undirected network, flow 0->1 and 1->0 are added to 0<->1, so if we have a flow 1->0, 
			//we have to change the s and d to meet the original link 0->1
			if(srcSnodeId>dstSnodeId){
				int tmp = srcSnodeId;
				srcSnodeId = dstSnodeId;
				dstSnodeId = tmp;
			}		
			srcVnode = vNet.getNodeFromID(srcVnodeId);
			dstVnode = vNet.getNodeFromID(dstVnodeId);
			VirtualLink tmpvl = vNet.findEdge(srcVnode, dstVnode);
			bwDem = tmpvl.getBandwidthDemand();
			
			srcSnode = sNet.getNodeFromID(srcSnodeId);
			dstSnode = sNet.getNodeFromID(dstSnodeId);
			SubstrateLink tmpsl = sNet.findEdge(srcSnode, dstSnode);		
			newBwDem = new BandwidthDemand(tmpvl);
			newBwDem.setDemandedBandwidth(bwDem.getDemandedBandwidth()*flow);
			
			if(!NodeLinkAssignation.vlmSingleLinkSimple(newBwDem, tmpsl)){
				throw new AssertionError("But we checked before!");
			}
			
			//backup
			Map<SubstrateLink,Set<SubstrateLink>> backupLinks=new HashMap<SubstrateLink,Set<SubstrateLink>>();
			Map<VirtualLink,List<SubstrateLink>> resultP = new HashMap<VirtualLink,List<SubstrateLink>>();
			Map<VirtualLink,List<SubstrateLink>> resultB = new HashMap<VirtualLink,List<SubstrateLink>>();
			List<SubstrateLink> backup = this.ComputeLocalBackupPath(sNet, tmpsl, newBwDem, true);
			if(!backup.isEmpty()){
				tmpBackup.addAll(backup);
				if(!NodeLinkAssignation.backup(tmpvl,tmpsl, backup, true))
					throw new AssertionError("But we checked before!");
			}
			else{
				System.out.println("no backup link");
				NodeLinkDeletion.linkFreeBackup(vl, tmpBackup, share);	//free temporary backup
				for(Map.Entry<VirtualNode, SubstrateNode> ent : nodeMapping.entrySet()){	//free node mapping
					NodeLinkDeletion.nodeFree(ent.getKey(), ent.getValue());
				}
				for(Map.Entry<VirtualLink, List<SubstrateLink>> ent: resultP.entrySet()){	// free primary path
					NodeLinkDeletion.linkFree(ent.getKey(), ent.getValue());
				}
				for(Map.Entry<VirtualLink, List<SubstrateLink>> ent: resultB.entrySet()){	//free backup path of other virtual links
					NodeLinkDeletion.linkFreeBackup(ent.getKey(), ent.getValue(),true);
				}
				return false;
			}
			
			if(backupLinks.containsKey(tmpsl)){	//if the primary link is in the list, add just the the protection link
				Set<SubstrateLink> tmpSet = backupLinks.get(tmpsl);
				for(SubstrateLink slink : backup)
					tmpSet.add(slink);
			}
			else{	//if not, add a new map element for the primary link
				Set<SubstrateLink> tmpSet=new HashSet<SubstrateLink>(backup);
				backupLinks.put(tmpsl, tmpSet);
			}
		}
	}
	
	private List<SubstrateLink> ComputeLocalBackupPath(SubstrateNetwork sn, SubstrateLink sl, BandwidthDemand bwd, boolean share){
		SubstrateNode node1 = sn.getEndpoints(sl).getFirst();
		SubstrateNode node2 = sn.getEndpoints(sl).getSecond();
		
		//block the links without enough available capacities
		//calculate additional bandwidth
		Predicate<SubstrateLink> pre=null;
		if(share){
			pre=new Predicate<SubstrateLink>(){
				@Override
				public boolean evaluate(SubstrateLink link) {
					if(link.equals(sl)) return false;
					BandwidthResource bdsrc = link.getBandwidthResource();
					for(Risk risk:bdsrc.getRisks()){
						if(risk.getNe().equals(sl)){
							double origTotal = bdsrc.maxRiskTotal();
							risk.addDemand(bwd);
							double newTotal = bdsrc.maxRiskTotal();
							risk.removeDemand(bwd);
							if((newTotal-origTotal)>bdsrc.getAvailableBandwidth())
								return false;
							else return true;
						}
					}
					double additional = bdsrc.getAvailableBandwidth()+bdsrc.getReservedBackupBw()-bwd.getDemandedBandwidth();
					if(additional<0) return false;
					else return true;
				}
			};
		}
		else{
			pre=new Predicate<SubstrateLink>() {
				@Override
				public boolean evaluate(SubstrateLink slink) {
					BandwidthResource bdsrc = slink.getBandwidthResource();
					if((bdsrc.getAvailableBandwidth()< bwd.getDemandedBandwidth())||slink.equals(sl))
						return false;
					return true;
				}
			};
		}
		
		EdgePredicateFilter<SubstrateNode,SubstrateLink> filter = new EdgePredicateFilter<SubstrateNode,SubstrateLink>(pre);
		Graph<SubstrateNode, SubstrateLink> tmp = filter.transform(sn);
		
		Transformer<SubstrateLink, Number> weight=null;;
		if(share){	//optimize additional bw
			weight = new Transformer<SubstrateLink,Number>(){
				public Double transform(SubstrateLink link){
					BandwidthResource bdsrc = link.getBandwidthResource();
					for(Risk risk:bdsrc.getRisks()){
						if(risk.getNe().equals(sl)){
							double origTotal = bdsrc.maxRiskTotal();
							risk.addDemand(bwd);
							double newTotal = bdsrc.maxRiskTotal();
							risk.removeDemand(bwd);
							return newTotal-origTotal;
						}
					}
					//new risk
					double additional = bwd.getDemandedBandwidth()-bdsrc.getReservedBackupBw();
					if(additional<=0) return 0.0;
					else return additional;
				}
			};
		}
		else{	//optimize residual bandwidth
			weight = new Transformer<SubstrateLink,Number>(){
				public Number transform(SubstrateLink link){
					BandwidthResource bdsrc = link.getBandwidthResource();
					return 1/(bdsrc.getAvailableBandwidth()+0.0001);
				}
			};
		}
		
		DijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode, SubstrateLink>(tmp,weight);	//dijkstra
		
		List<SubstrateLink> result = dijkstra.getPath(node1, node2);
		//TODO
	/*	if(result.isEmpty()){
			DrawGraph dg = new DrawGraph(tmp);
			dg.draw();			
		}*/
		return result;
	}
	
	public void generateFile(VirtualNetwork vNet,Map<VirtualNode, SubstrateNode> nodeMapping) throws IOException{
		BandwidthDemand bwDem = null;
		BandwidthResource bwResource=null;
		SubstrateNode ssnode=null, dsnode=null;
		VirtualNode srcVnode = null, dstVnode = null;

		String preambule = "\\Problem : vne\n";
		String obj = "Minimize\n"+"obj : ";
		String constraint = "Subject To\n";
		String bounds = "Bounds\n";
		String general = "General\n";

		for (Iterator<VirtualLink> links = vNet.getEdges().iterator(); links.hasNext();) {
			VirtualLink tmpl = links.next();
			// Find their mapped SubstrateNodes
			srcVnode = vNet.getEndpoints(tmpl).getFirst();
			dstVnode = vNet.getEndpoints(tmpl).getSecond();
			// Get current VirtualLink demand
			bwDem = tmpl.getBandwidthDemand();
			for (SubstrateLink tmpsl:sNet.getEdges()){
				ssnode = sNet.getEndpoints(tmpsl).getFirst();
				dsnode = sNet.getEndpoints(tmpsl).getSecond();
				bwResource=tmpsl.getBandwidthResource();
				//objective
				obj = obj + " + "+MiscelFunctions.roundToDecimals(100*bwDem.getDemandedBandwidth()/(bwResource.getAvailableBandwidth()+0.001),4);
				obj = obj + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId();
				obj = obj + " + "+MiscelFunctions.roundToDecimals(100*bwDem.getDemandedBandwidth()/(bwResource.getAvailableBandwidth()+0.001),4);
				obj = obj + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId();
				//bounds
				bounds = bounds + "0 <= vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+" <= 1\n";
				bounds = bounds + "0 <= vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId()+" <= 1\n";
			}
			
			//flow constraints
			for(SubstrateNode snode:sNet.getVertices()){
				for(SubstrateNode tmmpsn:sNet.getNeighbors(snode)){
					constraint=constraint+" + vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+snode.getId()+"sd"+tmmpsn.getId();
					constraint=constraint+" - vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+tmmpsn.getId()+"sd"+snode.getId();
				}
				if(snode.equals(nodeMapping.get(srcVnode)))	constraint =constraint+" = 1\n";
				else if(snode.equals(nodeMapping.get(dstVnode))) constraint =constraint+" = -1\n";
				else	constraint =constraint+" = 0\n";
			}
		}
		
		//capacity constraint
		for (SubstrateLink tmpsl:sNet.getEdges()){
			ssnode = sNet.getEndpoints(tmpsl).getFirst();
			dsnode = sNet.getEndpoints(tmpsl).getSecond();
			bwResource = tmpsl.getBandwidthResource();
			for (VirtualLink tmpl: vNet.getEdges()) {
				srcVnode = vNet.getEndpoints(tmpl).getFirst();
				dstVnode = vNet.getEndpoints(tmpl).getSecond();
				bwDem = tmpl.getBandwidthDemand();
				//capacity constraint
				constraint=constraint+" + "+bwDem.getDemandedBandwidth() +
						" vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+
						" + "+bwDem.getDemandedBandwidth() +
						" vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId(); 
			}
			constraint = constraint +" <= " + bwResource.getAvailableBandwidth()+"\n";
		}
		obj = obj+ "\n";
		BufferedWriter writer = new BufferedWriter(new FileWriter(localPath));
		writer.write(preambule+obj+constraint+bounds+general+"END");
		writer.close();
	}
}
