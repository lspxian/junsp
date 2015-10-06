package vnreal.algorithms.linkmapping;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import vnreal.algorithms.AbstractLinkMapping;
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
		//TODO update
		
		
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
					
					//integer in the <general>
					//general = general +  " vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+"\n";
					
					//bounds
					bounds = bounds + "0 <= vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+" <= 1\n";
				}
				
				//flow constraints
				ArrayList<SubstrateNode> nextHop = sNet.getNextHop(srcSnode);
				for(Iterator<SubstrateNode> iterator = sNet.getVertices().iterator();iterator.hasNext();){
					SubstrateNode snode = iterator.next();
					nextHop = sNet.getHop(snode);
					for(int i=0;i<nextHop.size();i++){
						constraint=constraint+" + vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+snode.getId()+"sd"+nextHop.get(i).getId();
						constraint=constraint+" - vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+nextHop.get(i).getId()+"sd"+snode.getId();
					}
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
							" vs"+srcSnode.getId()+"vd"+dstSnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId(); 
				}
			}
			constraint = constraint +" <= " + bwResource.getAvailableBandwidth()+"\n";
		}
		
		obj = obj+ "\n";
		BufferedWriter writer = new BufferedWriter(new FileWriter("ILP-LP-Models/CPLEXvne.lp"));
		writer.write(preambule+obj+constraint+bounds+general+"END");
		writer.close();
		
	}

}
