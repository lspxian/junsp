package vnreal.algorithms.linkmapping;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mulavito.algorithms.shortestpath.ksp.LocalBypass;
import mulavito.algorithms.shortestpath.ksp.Yen;
import vnreal.algorithms.AbstractLinkMapping;
import vnreal.algorithms.utils.LinkWeight;
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

public class SOD_BK extends AbstractLinkMapping{
	
	private static final int preselected_number = 3;
	private static final int bypass_number = 3;
	
	public SOD_BK(SubstrateNetwork sNet) {
		super(sNet);
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet,Map<VirtualNode, SubstrateNode> nodeMapping) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void generateFile(VirtualNetwork vNet,Map<VirtualNode, SubstrateNode> nodeMapping) throws IOException{
		String preambule = "\\Problem : SOD_BK : Shared Backup Network Provision for VNE (Guo2012)\n";
		String obj = "Minimize\n"+"obj : ";
		String constraint = "Subject To\n";
		String bounds = "Bounds\n";
		String general = "General\n";
		
		BandwidthResource bwResource = null;
		BandwidthDemand bwDem = null;
		SubstrateNode srcSnode = null, dstSnode = null;
		
		double delta = 0.01;
		for (Iterator<SubstrateLink> slink = sNet.getEdges().iterator();slink.hasNext();){
			SubstrateLink tmpsl = slink.next();
			for(AbstractResource asrc : tmpsl){
				if(asrc instanceof BandwidthResource){
					bwResource = (BandwidthResource) asrc;
				}
			}
			
			String capacity = ""; //capacity constraint
			
			for (Iterator<VirtualLink> vlink = vNet.getEdges().iterator(); vlink.hasNext();) {
				VirtualLink tmpvl = vlink.next();
				for (AbstractDemand dem : tmpvl) {
					if (dem instanceof BandwidthDemand) {
						bwDem = (BandwidthDemand) dem;
						break;
					}
				}
				
				// Find their mapped SubstrateNodes
				srcSnode = nodeMapping.get(vNet.getSource(tmpvl));
				dstSnode = nodeMapping.get(vNet.getDest(tmpvl));
				
				//pre-selected paths
				LinkWeight linkWeight = new LinkWeight();
				Yen<SubstrateNode, SubstrateLink> yen = new Yen(sNet,linkWeight);
				List<List<SubstrateLink>> paths = yen.getShortestPaths(srcSnode, dstSnode, SOD_BK.preselected_number);
				
				for(int i=0;i<paths.size();i++){
					List<SubstrateLink> path = paths.get(i);
					//Is(p)
					if(path.contains(tmpsl)){
						obj = obj + " + "+1/(delta + bwResource.getAvailableBandwidth());
						//Xp(v)
						obj = obj + " Xvl#" + tmpvl.getId() + "sp";
						capacity = capacity + " + Xvl#" + tmpvl.getId() + "sp";
						for(int j=0;j<path.size();j++){
							obj = obj + "#" +path.get(j).getId();
							capacity = capacity + "#" +path.get(j).getId();
						}
					}
				}
			}
			
			//Zs
			obj = obj + " + "+1/(delta + bwResource.getAvailableBandwidth());
			obj = obj + " Zs#" + tmpsl.getId();
			capacity = capacity + " + Zs#" + tmpsl.getId();
			capacity = capacity + " <= " + bwResource.getAvailableBandwidth()+"\n";
			bounds = bounds + " Zs#" + tmpsl.getId() + ">= 0 \n";
			constraint = constraint + capacity;
		}
		
		//primary flow constraint
		for (Iterator<VirtualLink> vlink = vNet.getEdges().iterator(); vlink.hasNext();) {
			VirtualLink tmpvl = vlink.next();
			for (AbstractDemand dem : tmpvl) {
				if (dem instanceof BandwidthDemand) {
					bwDem = (BandwidthDemand) dem;
					break;
				}
			}
			String primary_flow = "";
			// Find their mapped SubstrateNodes
			srcSnode = nodeMapping.get(vNet.getSource(tmpvl));
			dstSnode = nodeMapping.get(vNet.getDest(tmpvl));
			
			//pre-selected paths
			LinkWeight linkWeight = new LinkWeight();
			Yen<SubstrateNode, SubstrateLink> yen = new Yen(sNet,linkWeight);
			List<List<SubstrateLink>> paths = yen.getShortestPaths(srcSnode, dstSnode, SOD_BK.preselected_number);
			
			for(int i=0;i<paths.size();i++){
				List<SubstrateLink> path = paths.get(i);
				//Xp(v)
				primary_flow = primary_flow + " + Xvl#" + tmpvl.getId() + "sp";
				bounds = bounds + " Xvl#" + tmpvl.getId() + "sp";
				for(int j=0;j<path.size();j++){
					primary_flow = primary_flow + "#" +path.get(j).getId();
					bounds = bounds +  "#" +path.get(j).getId();
				}
				bounds = bounds + " >= 0 \n";
			}
			primary_flow = primary_flow + " = " + bwDem.getDemandedBandwidth() + "\n";
			constraint = constraint + primary_flow;
		}
		
		//restoration flow constraint
		for (Iterator<SubstrateLink> flink = sNet.getEdges().iterator();flink.hasNext();){
			SubstrateLink failure = flink.next();
			
			String restoration_flow = "";
				
			//R(f)
			LocalBypass<SubstrateNode, SubstrateLink> bypass = new LocalBypass(sNet, new LinkWeight());
			List<List<SubstrateLink>> localBypass = bypass.getShortestPaths(failure, SOD_BK.bypass_number);

			for(int i=0;i<localBypass.size();i++){
				List<SubstrateLink> localBypassi = localBypass.get(i);
				//yr(f)
				restoration_flow = restoration_flow + " + yf#" + failure.getId() + "r";
				bounds = bounds + " yf#" + failure.getId() + "r";
				for(int j=0;j<localBypassi.size();j++){
					restoration_flow = restoration_flow + "#" + localBypassi.get(j).getId();
					bounds = bounds + "#" + localBypassi.get(j).getId();
				}
				bounds = bounds + " >= 0 \n";
			}
				
			for (Iterator<VirtualLink> vlink = vNet.getEdges().iterator(); vlink.hasNext();) {
				VirtualLink tmpvl = vlink.next();
				
				// Find their mapped SubstrateNodes
				srcSnode = nodeMapping.get(vNet.getSource(tmpvl));
				dstSnode = nodeMapping.get(vNet.getDest(tmpvl));
				
				//pre-selected paths
				LinkWeight linkWeight = new LinkWeight();
				Yen<SubstrateNode, SubstrateLink> yen = new Yen(sNet,linkWeight);
				List<List<SubstrateLink>> paths = yen.getShortestPaths(srcSnode, dstSnode, SOD_BK.preselected_number);
				
				for(int i=0;i<paths.size();i++){
					List<SubstrateLink> path = paths.get(i);
					//If(p)
					if(path.contains(failure)){
						restoration_flow = restoration_flow + " - Xvl#" + tmpvl.getId() + "sp";
						for(int j=0;j<path.size();j++){
							restoration_flow = restoration_flow + "#" +path.get(j).getId();
						}
					}
				}
			}

			restoration_flow = restoration_flow + " = 0\n";
			constraint = constraint + restoration_flow;
		}
		
		//restoration bandwidth constraint
		for (Iterator<SubstrateLink> flink = sNet.getEdges().iterator();flink.hasNext();){
			SubstrateLink failure = flink.next();
			
			for (Iterator<SubstrateLink> slink = sNet.getEdges().iterator();slink.hasNext();){
				SubstrateLink tmpsl = slink.next();
				for(AbstractResource asrc : tmpsl){
					if(asrc instanceof BandwidthResource){
						bwResource = (BandwidthResource) asrc;
					}
				}
				String restoration_bw ="";
				
				//R(f)
				LocalBypass<SubstrateNode, SubstrateLink> bypass = new LocalBypass(sNet, new LinkWeight());
				List<List<SubstrateLink>> localBypass = bypass.getShortestPaths(failure, SOD_BK.bypass_number);
				//yr(f)
				boolean flag = false; // eliminate empty left term
				for(int i=0;i<localBypass.size();i++){
					List<SubstrateLink> localBypassi = localBypass.get(i);
					//Is(r) yr(f)
					if(localBypassi.contains(tmpsl)){
						flag = true;
						restoration_bw = restoration_bw + " + yf#" + failure.getId() + "r";
						for(int j=0;j<localBypassi.size();j++){
							restoration_bw = restoration_bw + "#" + localBypassi.get(j).getId();
						}
					}
				}
				if(flag == true){
					restoration_bw = restoration_bw  + " - Zs#" + tmpsl.getId() + " <= " 
							+ bwResource.getReservedBackupBw() + " - " + bwResource.getLinkBackupBw(failure)+"\n"; 					
				}
				constraint = constraint + restoration_bw ;
				
			}
		}
		
		
		
		obj = obj+ "\n";
		BufferedWriter writer = new BufferedWriter(new FileWriter("ILP-LP-Models/SOD_BK.lp"));
		writer.write(preambule+obj+constraint+bounds+general+"END");
		writer.close();
	}

}
