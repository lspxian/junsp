package vnreal.algorithms.linkmapping;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections15.map.LinkedMap;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import vnreal.algorithms.AbstractLinkMapping;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.algorithms.utils.NodeLinkAssignation;
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

public class MultiCommodityFlow extends AbstractLinkMapping {

	public MultiCommodityFlow(SubstrateNetwork sNet) {
		super(sNet);
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet,
			Map<VirtualNode, SubstrateNode> nodeMapping) {
		Map<String, String> solution = linkMappingWithoutUpdate(vNet, nodeMapping);
		if(solution.size()==0){
			System.out.println("link no solution");
			return false;
		}
		System.out.println(solution);
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
			remote.getSftp().put("ILP-LP-Models/vne-mcf.lp", "pytest/vne-mcf.lp");
			
			//solve the problem with python script, get output solution
			solution = remote.executeCmd("python pytest/mysolver.py pytest/vne-mcf.lp o");
			
		} catch (JSchException | IOException | SftpException e) {
			e.printStackTrace();
		}
		remote.disconnect();
		return solution;
	}
	
	public void updateResource(VirtualNetwork vNet,  Map<VirtualNode, SubstrateNode> nodeMapping, Map<String,String> solution){
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
			
			//for undirected network, flow 0->1 and 1->0 are added to 0<->1, so if we have a flow 1->0, 
			//we have to change the s and d to meet the original link 0->1
			
			if(srcSnodeId>dstSnodeId){
				int tmp = srcSnodeId;
				srcSnodeId = dstSnodeId;
				dstSnodeId = tmp;
			}
			
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
	}
	
	//generate cplex file for undirected multi commodity flow
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
			srcSnode = nodeMapping.get(vNet.getSource(tmpl));
			dstSnode = nodeMapping.get(vNet.getDest(tmpl));
			
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
					ssnode = sNet.getSource(tmpsl);
					dsnode = sNet.getDest(tmpsl);
					
					for(AbstractResource asrc : tmpsl){
						if(asrc instanceof BandwidthResource){
							bwResource = (BandwidthResource) asrc;
						}
					}
					
					//objective
					obj = obj + " + "+bwDem.getDemandedBandwidth()/bwResource.getAvailableBandwidth();
					obj = obj + " vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId();
					obj = obj + " + "+bwDem.getDemandedBandwidth()/bwResource.getAvailableBandwidth();
					obj = obj + " vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId();
					
					//integer in the <general>
					//general = general +  " vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+"\n";
					
					//bounds
					bounds = bounds + "0 <= vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+" <= 1\n";
					bounds = bounds + "0 <= vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId()+" <= 1\n";
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
					//nextHop = sNet.getHop(snode);
					/*
					for(int i=0;i<nextHop.size();i++){
						constraint=constraint+" + vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+snode.getId()+"sd"+nextHop.get(i).getId();
						constraint=constraint+" - vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+nextHop.get(i).getId()+"sd"+snode.getId();
					}*/
					if(snode.equals(srcSnode))	constraint =constraint+" = 1\n";
					else if(snode.equals(dstSnode)) constraint =constraint+" = -1\n";
					else	constraint =constraint+" = 0\n";
					
				}
				
			}
		}
		
		//capacity constraint
		for (Iterator<SubstrateLink> slink = sNet.getEdges().iterator();slink.hasNext();){
			SubstrateLink tmpsl = slink.next();
			ssnode = sNet.getSource(tmpsl);
			dsnode = sNet.getDest(tmpsl);
			
			for(AbstractResource asrc : tmpsl){
				if(asrc instanceof BandwidthResource){
					bwResource = (BandwidthResource) asrc;
				}
			}
			
			for (Iterator<VirtualLink> links = vNet.getEdges().iterator(); links.hasNext();) {
				VirtualLink tmpl = links.next();
				srcSnode = nodeMapping.get(vNet.getSource(tmpl));
				dstSnode = nodeMapping.get(vNet.getDest(tmpl));
				
				if (!srcSnode.equals(dstSnode)) {
					for (AbstractDemand dem : tmpl) {
						if (dem instanceof BandwidthDemand) {
							bwDem = (BandwidthDemand) dem;
							break;
						}
					}
					
					//capacity constraint
					constraint=constraint+" + "+bwDem.getDemandedBandwidth() +
							" vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+
							" + "+bwDem.getDemandedBandwidth() +
							" vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId(); 
				}
			}
			constraint = constraint +" <= " + bwResource.getAvailableBandwidth()+"\n";
		}
		
		obj = obj+ "\n";
		BufferedWriter writer = new BufferedWriter(new FileWriter("ILP-LP-Models/vne-mcf.lp"));
		writer.write(preambule+obj+constraint+bounds+general+"END");
		writer.close();
		
	}

}
