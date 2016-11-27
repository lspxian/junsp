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
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import probabilityBandwidth.AbstractProbaLinkMapping;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.algorithms.utils.Remote;
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

public class ProtectionEnabledPrimaryMapping extends AbstractProbaLinkMapping {

	private String localPath ;
	public ProtectionEnabledPrimaryMapping(SubstrateNetwork sNet) {
		super(sNet);
		this.localPath = "cplex/vne-mcf.lp";
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet,Map<VirtualNode, SubstrateNode> nodeMapping) {
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
		return true;
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
			bwDem=tmpvl.getBandwidthDemand();
			
			srcSnode = sNet.getNodeFromID(srcSnodeId);
			dstSnode = sNet.getNodeFromID(dstSnodeId);
			SubstrateLink tmpsl = sNet.findEdge(srcSnode, dstSnode);
			
			newBwDem = new BandwidthDemand(tmpvl);
			newBwDem.setDemandedBandwidth(bwDem.getDemandedBandwidth()*flow);
			
			if(!NodeLinkAssignation.vlmSingleLinkSimple(newBwDem, tmpsl)){
				throw new AssertionError("But we checked before!");
			}
		}
	}
	
	//generate cplex file for undirected multi commodity flow
	public void generateFile(VirtualNetwork vNet,Map<VirtualNode, SubstrateNode> nodeMapping) throws IOException{
		SubstrateNode ssnode=null, dsnode=null;
		VirtualNode srcVnode = null, dstVnode = null;

		String preambule = "\\Problem : vne\n";
		String obj = "Minimize\n"+"obj : ";
		String constraint = "Subject To\n";
		String bounds = "Bounds\n";
		String general = "General\n";

		for (VirtualLink tmpl:vNet.getEdges()) {
			// Find their mapped SubstrateNodes
			srcVnode = vNet.getEndpoints(tmpl).getFirst();
			dstVnode = vNet.getEndpoints(tmpl).getSecond();
		
			for (SubstrateLink tmpsl:sNet.getEdges()){
				ssnode = sNet.getEndpoints(tmpsl).getFirst();
				dsnode = sNet.getEndpoints(tmpsl).getSecond();
				
				//objective
				double pontential = this.sNet.backupPotential(tmpsl);
				obj = obj + " + "+MiscelFunctions.roundToDecimals(1/(pontential+0.001),3);
				obj = obj + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId();
				obj = obj + " + "+MiscelFunctions.roundToDecimals(1/(pontential+0.001),3);
				obj = obj + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId();
				
				//integer in the <general>
				//general = general +  " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+"\n";
				
				//bounds
				bounds = bounds + "0 <= vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+" <= 1\n";
				bounds = bounds + "0 <= vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId()+" <= 1\n";
			}
			
			//flow constraints
			Collection<SubstrateNode> nextHop = new ArrayList<SubstrateNode>();
			for(SubstrateNode snode:sNet.getVertices()){
				nextHop = sNet.getNeighbors(snode);
				for(SubstrateNode tmmpsn:nextHop){
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
			BandwidthResource bwResource=tmpsl.getBandwidthResource();
			
			for (VirtualLink tmpl:vNet.getEdges()) {
				srcVnode = vNet.getEndpoints(tmpl).getFirst();
				dstVnode = vNet.getEndpoints(tmpl).getSecond();
				BandwidthDemand bwDem=tmpl.getBandwidthDemand();
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
