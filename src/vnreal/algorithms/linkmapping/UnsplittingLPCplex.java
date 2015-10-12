package vnreal.algorithms.linkmapping;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections15.map.LinkedMap;

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
	private double wBw, wCpu;

	public UnsplittingLPCplex(SubstrateNetwork sNet,
			double cpuWeight, double bwWeight) {
		super(sNet);
		this.wBw = bwWeight;
		this.wCpu = cpuWeight;
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet,Map<VirtualNode, SubstrateNode> nodeMapping) {
		
		Remote remote = new Remote();
		
		try {
			//generate .lp file
			this.generateFile(vNet, nodeMapping);
			
			//upload file
			remote.getSftp().put("ILP-LP-Models/CPLEXvne.lp", "pytest/CPLEXvne.lp");
			
			//solve the problem with python script, get output solution
			Map<String, String> solution = remote.executeCmd("python pytest/mysolver.py pytest/CPLEXvne.lp o");
			if(solution.size()==0){
				System.out.println("link no solution");
				//delete node mapping
				for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
					NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
				}
				return false;
			}
			
			System.out.println(solution);
			//update resource according to solution
			BandwidthDemand originalBwDem = null, newBwDem;
			VirtualNode srcVnode,dstVnode;
			SubstrateNode srcSnode = null,dstSnode = null;
			int srcVnodeId, dstVnodeId, srcSnodeId, dstSnodeId;
			
			Map<SubstrateNode, VirtualNode> inverseNodeMapping = new LinkedMap<SubstrateNode, VirtualNode>();
			for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
				inverseNodeMapping.put(entry.getValue(), entry.getKey());
			}
			
			for(Map.Entry<String, String> entry : solution.entrySet()){
				String linklink = entry.getKey();
				double flow = Double.parseDouble(entry.getValue());
				srcVnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("vs")+2, linklink.indexOf("vd")));
				dstVnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("vd")+2, linklink.indexOf("ss")));
				srcSnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("ss")+2, linklink.indexOf("sd")));
				dstSnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("sd")+2));
				
				srcVnode = inverseNodeMapping.get(sNet.getNodeFromID(srcVnodeId));
				dstVnode = inverseNodeMapping.get(sNet.getNodeFromID(dstVnodeId));
				VirtualLink tmpvl = vNet.findEdge(srcVnode, dstVnode);
				
				for (AbstractDemand dem : tmpvl) {
					if (dem instanceof BandwidthDemand) {
						originalBwDem = (BandwidthDemand) dem;
						break;
					}
				}
				
				srcSnode = sNet.getNodeFromID(srcSnodeId);
				dstSnode = sNet.getNodeFromID(dstSnodeId);
				SubstrateLink tmpsl = sNet.findEdge(srcSnode, dstSnode);
				
				newBwDem = new BandwidthDemand(tmpvl);
				newBwDem.setDemandedBandwidth(MiscelFunctions
						.roundThreeDecimals(originalBwDem.getDemandedBandwidth()*flow));
				
				if(!NodeLinkAssignation.vlmSingleLinkSimple(newBwDem, tmpsl)){
					throw new AssertionError("But we checked before!");
				}
				
				
			}
			
			
		} catch (JSchException | IOException | SftpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		remote.disconnect();
		return true;
	}
	
	public void generateFile(VirtualNetwork vNet,Map<VirtualNode, SubstrateNode> nodeMapping) throws IOException{
		BandwidthDemand bwDem = null;
		BandwidthResource bwResource=null;
		SubstrateNode srcSnode = null, dstSnode = null, ssnode=null, dsnode=null;

		String preambule = "\\Problem : vne\n";
		String obj = "Minimize\n"+"obj : ";
		String constraint = "Subject To\n";
		String bounds = "Bounds\n";
		String general = "General\n";

		for (Iterator<VirtualLink> links = vNet.getEdges().iterator(); links.hasNext();) {
			VirtualLink tmpl = links.next();

			// Find their mapped SubstrateNodes
			srcSnode = nodeMapping.get(vNet.getEndpoints(tmpl).getFirst());
			dstSnode = nodeMapping.get(vNet.getEndpoints(tmpl).getSecond());
			
			if (!srcSnode.equals(dstSnode)) {
				// Get current VirtualLink demand
				for (AbstractDemand dem : tmpl) {
					if (dem instanceof BandwidthDemand) {
						bwDem = (BandwidthDemand) dem;
						break;
					}
				}
			
				for (Iterator<SubstrateLink> slink = sNet.getEdges().iterator();slink.hasNext();){
					SubstrateLink tmpsl = slink.next();
					ssnode = sNet.getEndpoints(tmpsl).getFirst();
					dsnode = sNet.getEndpoints(tmpsl).getSecond();
					
					for(AbstractResource asrc : tmpsl){
						if(asrc instanceof BandwidthResource){
							bwResource = (BandwidthResource) asrc;
						}
					}
					
					//objective
					obj = obj + " + "+bwDem.getDemandedBandwidth()/bwResource.getAvailableBandwidth();
					obj = obj + " vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId();
					
					//integer in the <general>
					general = general +  " vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+"\n";
				}
				
				//flow constraints
				Collection<SubstrateNode> nextHop = new ArrayList<SubstrateNode>();
				for(Iterator<SubstrateNode> iterator = sNet.getVertices().iterator();iterator.hasNext();){
					SubstrateNode snode = iterator.next();
					nextHop = sNet.getNeighbors(snode);
					for(Iterator it=nextHop.iterator();it.hasNext();){
						SubstrateNode tmmpsn = (SubstrateNode) it.next();
						constraint=constraint+" + vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+snode.getId()+"sd"+tmmpsn.getId();
						constraint=constraint+" - vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+tmmpsn.getId()+"sd"+snode.getId();
					}
					if(snode.equals(srcSnode))	constraint =constraint+" = 1\n";
					else if(snode.equals(dstSnode)) constraint =constraint+" = -1\n";
					else	constraint =constraint+" = 0\n";
					
				}
				
				
				//source and destination flow constraints
				/*
				ArrayList<SubstrateNode> nextHop = sNet.getNextHop(srcSnode);
				for(int i=0;i<nextHop.size();i++){
					constraint=constraint+" + vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+srcSnode.getId()+"sd"+nextHop.get(i).getId();
				}
				constraint =constraint+" = 1\n";
				
				ArrayList<SubstrateNode> lastHop = sNet.getLastHop(dstSnode);
				for(int i=0;i<lastHop.size();i++){
					constraint=constraint+" + vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+lastHop.get(i).getId()+"sd"+dstSnode.getId();
				}
				constraint =constraint+" = 1\n";
				
				//middle node flow constraints
				for(Iterator<SubstrateNode> iterator = sNet.getVertices().iterator();iterator.hasNext();){
					SubstrateNode snode = iterator.next();
					if((!snode.equals(srcSnode))&&(!snode.equals(dstSnode))){
						lastHop = sNet.getLastHop(snode);
						for(int i=0;i<lastHop.size();i++){
							constraint=constraint+" + vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+lastHop.get(i).getId()+"sd"+snode.getId();
						}
						nextHop = sNet.getNextHop(snode);
						for(int i=0;i<nextHop.size();i++){
							constraint=constraint+" - vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+snode.getId()+"sd"+nextHop.get(i).getId();
						}
						constraint =constraint+" = 0\n";
					}
				}*/
			}
		}
		
		//capacity constraint
		for (Iterator<SubstrateLink> slink = sNet.getEdges().iterator();slink.hasNext();){
			SubstrateLink tmpsl = slink.next();
			ssnode = sNet.getEndpoints(tmpsl).getFirst();
			dsnode = sNet.getEndpoints(tmpsl).getSecond();
			
			for(AbstractResource asrc : tmpsl){
				if(asrc instanceof BandwidthResource){
					bwResource = (BandwidthResource) asrc;
				}
			}
			
			for (Iterator<VirtualLink> links = vNet.getEdges().iterator(); links.hasNext();) {
				VirtualLink tmpl = links.next();
				srcSnode = nodeMapping.get(vNet.getEndpoints(tmpl).getFirst());
				dstSnode = nodeMapping.get(vNet.getEndpoints(tmpl).getSecond());
				
				if (!srcSnode.equals(dstSnode)) {
					for (AbstractDemand dem : tmpl) {
						if (dem instanceof BandwidthDemand) {
							bwDem = (BandwidthDemand) dem;
							break;
						}
					}
					
					//capacity constraint
					constraint=constraint+" + "+bwDem.getDemandedBandwidth() +
							" vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId(); 
				}
			}
			constraint = constraint +" <= " + bwResource.getAvailableBandwidth()+"\n";
		}
		
		obj = obj+ "\n";
		BufferedWriter writer = new BufferedWriter(new FileWriter("ILP-LP-Models/CPLEXvne.lp"));
		writer.write(preambule+obj+constraint+general+"END");
		writer.close();
		
	}
}
