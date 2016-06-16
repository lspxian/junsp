package vnreal.algorithms.linkmapping;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.naming.NamingException;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import vnreal.algorithms.AbstractLinkMapping;
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
/**
 * 
 * @author LI
 * This class implements the unsplitting multi-commodity flow problem with Cplex on the cloud magi paris 13.
 * The cloud magi paris 13 has a full version of cplex.
 * We first generate a .lp file with the function generateFile.
 * the lp file contains the strings representing objective, constraints, bounds, general. 
 * For example, vs1vd2ss5sd6, the number 1,2,5,6 are substrate nodes.
 * This string means the flow of virtual link 1 to 2 on the substrate link 5 to 6
 * This model is a node-arc based MCF, which includes all the possible paths, 
 * and the number of variables and constraints explode with the substrate network size. 
 */
public class UnsplittingLPCplex extends AbstractLinkMapping{
	private String localPath ;
	private String remotePath ;
	public UnsplittingLPCplex(SubstrateNetwork sNet) {
		super(sNet);
		this.localPath = "cplex/vne-mcf.lp";
		this.remotePath = "pytest/vne-mcf.lp";
	}
	public UnsplittingLPCplex(SubstrateNetwork sNet, String localPath, String remotePath) {
		super(sNet);
		this.localPath = localPath;
		this.remotePath = remotePath;
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet,Map<VirtualNode, SubstrateNode> nodeMapping) {
		//Map<String, String> solution = linkMappingWithoutUpdate(vNet, nodeMapping);
		Map<String, String> solution = linkMappingWithoutUpdateLocal(vNet, nodeMapping);		
		if(solution.size()==0){
			System.out.println("link no solution");
			ConstraintShortestPath csp = new ConstraintShortestPath(sNet);
			//TODO 
			if(csp.linkMapping(vNet, nodeMapping)){
				System.out.println("error important !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				throw new ArithmeticException("mcf fails but dijkstra succed !!!!!!!!!!!");
			}
			for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
				NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
			}
			return false;
		}
		//update
		updateResource(vNet, nodeMapping, solution);
		return true;
	}
	
	public Map<String,String> linkMappingWithoutUpdate(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		Remote remote = new Remote();
		Map<String, String> solution = new HashMap<String, String>();
		try {
			//generate .lp file
			this.generateFile(vNet, nodeMapping);
			
			//upload file
			remote.getSftp().put(localPath, remotePath);
			
			//solve the problem with python script, get output solution
			solution = remote.executeCmd("python pytest/mysolver.py "+remotePath+" o");
			
		} catch (JSchException | IOException | SftpException e) {
			e.printStackTrace();
		}
		remote.disconnect();
		return solution;
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
			
			for (AbstractDemand dem : tmpvl) {
				if (dem instanceof BandwidthDemand) {
					bwDem = (BandwidthDemand) dem;
					break;
				}
			}
			
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
		BandwidthDemand bwDem = null;
		BandwidthResource bwResource=null;
		SubstrateNode ssnode=null, dsnode=null;
		VirtualNode srcVnode = null, dstVnode = null;

		String preambule = "\\Problem : vne\n";
		String obj = "Minimize\n"+"obj : ";
		String constraint = "Subject To\n";
		String binary = "Binary\n";

		for (VirtualLink tmpl : vNet.getEdges()) {

			// Find their mapped SubstrateNodes
			srcVnode = vNet.getEndpoints(tmpl).getFirst();
			dstVnode = vNet.getEndpoints(tmpl).getSecond();
			
			// Get current VirtualLink demand
			for (AbstractDemand dem : tmpl) {
				if (dem instanceof BandwidthDemand) {
					bwDem = (BandwidthDemand) dem;
					break;
				}
			}
		
			for (SubstrateLink tmpsl : sNet.getEdges()){
				ssnode = sNet.getEndpoints(tmpsl).getFirst();
				dsnode = sNet.getEndpoints(tmpsl).getSecond();
				
				for(AbstractResource asrc : tmpsl){
					if(asrc instanceof BandwidthResource){
						bwResource = (BandwidthResource) asrc;
					}
				}
				
				//objective
				obj = obj + " + "+MiscelFunctions.roundToDecimals(100*bwDem.getDemandedBandwidth()/(bwResource.getAvailableBandwidth()+0.001),4);
//				obj = obj + " + "+MiscelFunctions.roundToDecimals(1000/(bwResource.getAvailableBandwidth()+0.001),4);
				obj = obj + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId();
				obj = obj + " + "+MiscelFunctions.roundToDecimals(100*bwDem.getDemandedBandwidth()/(bwResource.getAvailableBandwidth()+0.001),4);
//				obj = obj + " + "+MiscelFunctions.roundToDecimals(1000/(bwResource.getAvailableBandwidth()+0.001),4);
				obj = obj + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId();
				
				//binary
				binary = binary + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+"\n";
				binary = binary + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId()+"\n";
			}
			
			//flow constraints
			Collection<SubstrateNode> nextHop = new ArrayList<SubstrateNode>();
			for(SubstrateNode snode : sNet.getVertices()){
				nextHop = sNet.getNeighbors(snode);
				for(SubstrateNode tmmpsn : nextHop){
					constraint=constraint+" + vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+snode.getId()+"sd"+tmmpsn.getId();
					constraint=constraint+" - vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+tmmpsn.getId()+"sd"+snode.getId();
				}

				if(snode.equals(nodeMapping.get(srcVnode)))	constraint =constraint+" = 1\n";
				else if(snode.equals(nodeMapping.get(dstVnode))) constraint =constraint+" = -1\n";
				else	constraint =constraint+" = 0\n";
				
			}
			
		}

		
		//capacity constraint
		for (SubstrateLink tmpsl : sNet.getEdges()){
			ssnode = sNet.getEndpoints(tmpsl).getFirst();
			dsnode = sNet.getEndpoints(tmpsl).getSecond();
			
			for(AbstractResource asrc : tmpsl){
				if(asrc instanceof BandwidthResource){
					bwResource = (BandwidthResource) asrc;
				}
			}
			
			for (VirtualLink tmpl : vNet.getEdges()) {
				srcVnode = vNet.getEndpoints(tmpl).getFirst();
				dstVnode = vNet.getEndpoints(tmpl).getSecond();
				
					for (AbstractDemand dem : tmpl) {
						if (dem instanceof BandwidthDemand) {
							bwDem = (BandwidthDemand) dem;
							break;
						}
					}
					
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
		writer.write(preambule+obj+constraint+binary+"END");
		writer.close();
		
	}
}
