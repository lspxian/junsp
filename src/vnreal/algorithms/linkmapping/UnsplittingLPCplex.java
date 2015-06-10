package vnreal.algorithms.linkmapping;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import vnreal.algorithms.AbstractLinkMapping;
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

public class UnsplittingLPCplex extends AbstractLinkMapping{
	private double wBw, wCpu;

	public UnsplittingLPCplex(SubstrateNetwork sNet,
			double cpuWeight, double bwWeight) {
		super(sNet);
		this.wBw = bwWeight;
		this.wCpu = cpuWeight;
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet,
			Map<VirtualNode, SubstrateNode> nodeMapping) {
		
		JSch jsch = new JSch();
		String user = "li.shuopeng";
		String host = "magi.univ-paris13.fr";
		int port = 2822;
		try {
			Session session=jsch.getSession(user, host, port);
			session.connect();
		    Channel channel=session.openChannel("sftp");
		    channel.connect();
		    ChannelSftp c=(ChannelSftp)channel;
		    
			
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return false;
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
					obj = obj + " n"+srcSnode.getId()+"n"+dstSnode.getId()+"n"+ssnode.getId()+"n"+dsnode.getId();
					
					
					//integer in the <general>
					general = general +  " n"+srcSnode.getId()+"n"+dstSnode.getId()+"n"+ssnode.getId()+"n"+dsnode.getId()+"\n";
					
				}
				
				//source and destination flow constraints
				ArrayList<SubstrateNode> nextHop = sNet.getNextHop(srcSnode);
				for(int i=0;i<nextHop.size();i++){
					constraint=constraint+" + n"+srcSnode.getId()+"n"+dstSnode.getId()+"n"+srcSnode.getId()+"n"+nextHop.get(i).getId();
				}
				constraint =constraint+" = 1\n";
				
				ArrayList<SubstrateNode> lastHop = sNet.getLastHop(dstSnode);
				for(int i=0;i<lastHop.size();i++){
					constraint=constraint+" + n"+srcSnode.getId()+"n"+dstSnode.getId()+"n"+lastHop.get(i).getId()+"n"+dstSnode.getId();
				}
				constraint =constraint+" = 1\n";
				
				//middle node flow constraints
				for(Iterator<SubstrateNode> iterator = sNet.getVertices().iterator();iterator.hasNext();){
					SubstrateNode snode = iterator.next();
					if((!snode.equals(srcSnode))&&(!snode.equals(dstSnode))){
						lastHop = sNet.getLastHop(snode);
						for(int i=0;i<lastHop.size();i++){
							constraint=constraint+" + n"+srcSnode.getId()+"n"+dstSnode.getId()+"n"+lastHop.get(i).getId()+"n"+snode.getId();
						}
						nextHop = sNet.getNextHop(snode);
						for(int i=0;i<nextHop.size();i++){
							constraint=constraint+" - n"+srcSnode.getId()+"n"+dstSnode.getId()+"n"+snode.getId()+"n"+nextHop.get(i).getId();
						}
						constraint =constraint+" = 0\n";
					}
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
							" n"+srcSnode.getId()+"n"+dstSnode.getId()+"n"+ssnode.getId()+"n"+dsnode.getId(); 
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
