package vnreal.algorithms.linkmapping;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.map.LinkedMap;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import mulavito.algorithms.shortestpath.ksp.LocalBypass;
import mulavito.algorithms.shortestpath.ksp.Yen;
import vnreal.algorithms.AbstractLinkMapping;
import vnreal.algorithms.utils.LinkWeight;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.algorithms.utils.Remote;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.mapping.Mapping;
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
	private Map<VirtualLink, List<List<SubstrateLink>>> preselectedPath = new LinkedMap<VirtualLink, List<List<SubstrateLink>>>();
	private Map<SubstrateLink, List<List<SubstrateLink>>> bypassPath = new LinkedMap<SubstrateLink, List<List<SubstrateLink>>>();
	
	
	public SOD_BK(SubstrateNetwork sNet) {
		super(sNet);
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet,Map<VirtualNode, SubstrateNode> nodeMapping) {
		
		Remote remote = new Remote();

		try {
			//generate .lp file
			this.generateFile(vNet, nodeMapping);
			//upload file
			remote.getSftp().put("ILP-LP-Models/SOD_BK.lp", "pytest/SOD_BK.lp");
			
			//solve the problem with python script, get output solution
			Map<String, String> solution = remote.executeCmd("python pytest/mysolver.py pytest/SOD_BK.lp o");
			System.out.println(solution);
			//classify the variables
			Map<String, String> xpv = new LinkedMap<String, String>();
			Map<String, String> yrf = new LinkedMap<String, String>();
			Map<String, String> zs = new LinkedMap<String, String>();
			for(Map.Entry<String, String> entry : solution.entrySet()){
				if(entry.getKey().startsWith("X"))
					xpv.put(entry.getKey(), entry.getValue());
				else if(entry.getKey().startsWith("y"))
					yrf.put(entry.getKey(), entry.getValue());
				else if(entry.getKey().startsWith("Z"))
					zs.put(entry.getKey(), entry.getValue());
			}
			
			//update resource according to solution
			BandwidthDemand bwDem = null, newBwDem;
			BandwidthResource bwResource = null;
			
			for (Iterator<SubstrateLink> slink = sNet.getEdges().iterator();slink.hasNext();){
				SubstrateLink tmpsl = slink.next();
				for(AbstractResource asrc : tmpsl){
					if(asrc instanceof BandwidthResource){
						bwResource = (BandwidthResource) asrc;
						break;
					}
				}
				double additionalBw=0;
				if(zs.get("Zs#"+tmpsl.getId())!=null){
					//Zs(t+1) = Zs(t) + zs
					bwResource.setReservedBackupBw(bwResource.getReservedBackupBw()+Double.parseDouble(zs.get("Zs#"+tmpsl.getId())));
					//zs
					additionalBw = additionalBw + Double.parseDouble(zs.get("Zs#"+tmpsl.getId())); 
				}
				for (Iterator<VirtualLink> vlink = vNet.getEdges().iterator(); vlink.hasNext();) {
					VirtualLink tmpvl = vlink.next();
					for(AbstractDemand admd :tmpvl){
						if(admd instanceof BandwidthDemand){
							bwDem = (BandwidthDemand) admd;
							break;
						}
					}
					
					for(int i=0;i<preselectedPath.get(tmpvl).size();i++){
						List<SubstrateLink> path = preselectedPath.get(tmpvl).get(i);
						String stringxpv = "";
						//Is(p)
						if(path.contains(tmpsl)){
							//Xp(v)
							stringxpv = stringxpv + "Xvl#" + tmpvl.getId() + "sp";
							for(int j=0;j<path.size();j++){
								stringxpv = stringxpv + "#" +path.get(j).getId();
							}
						}
						if(xpv.get(stringxpv)!=null){
							//xpv sum
							additionalBw = additionalBw + Double.parseDouble(xpv.get(stringxpv));
							bwResource.setPrimaryBw(bwResource.getPrimaryBw()+Double.parseDouble(xpv.get(stringxpv)));
							newBwDem = new BandwidthDemand(tmpvl);
							newBwDem.setDemandedBandwidth(Double.parseDouble(xpv.get(stringxpv)));
							
							new Mapping(newBwDem,bwResource);
							/*
							if(!NodeLinkAssignation.vlmSingleLinkSimple(newBwDem, tmpsl)){
								throw new AssertionError("But we checked before!");
							}*/
							
						}
					}
				}
				//Rs(t+1)
				bwResource.setOccupiedBandwidth(bwResource.getOccupiedBandwidth()+additionalBw);
				
				for (Iterator<SubstrateLink> flink = sNet.getEdges().iterator();flink.hasNext();){
					SubstrateLink failure = flink.next();
					
					double additionalBackup =0.0;
					for(int i=0;i<bypassPath.get(failure).size();i++){
						List<SubstrateLink> localBypassi = bypassPath.get(failure).get(i);
						String stringyrf="";
						//Is(r) yr(f)
						if(localBypassi.contains(tmpsl)){
							stringyrf = stringyrf + "yf#" + failure.getId() + "r";
							for(int j=0;j<localBypassi.size();j++){
								stringyrf = stringyrf + "#" + localBypassi.get(j).getId();
							}
						}
						if(yrf.get(stringyrf)!=null){
							additionalBackup = additionalBackup + Double.parseDouble(yrf.get(stringyrf));
							//bwResource.getBackupBw().put(failure, Double.parseDouble(yrf.get(stringyrf)));
						}
						
					}
					//Ysf(t+1)
					if(bwResource.getBackupBw().get(failure)!=null)
						bwResource.getBackupBw().replace(failure, bwResource.getBackupBw().get(failure)+additionalBackup);
					else if(additionalBackup!=0.0)
						bwResource.getBackupBw().put(failure, additionalBackup);
				}
			}
			
		} catch (IOException | SftpException | JSchException e) {
			e.printStackTrace();
		}
		
		remote.disconnect();
		return true;
	}

	public boolean freeResource(){
		
		BandwidthDemand bwDem = null, newBwDem;
		BandwidthResource bwResource = null;
		
		for (Iterator<SubstrateLink> slink = sNet.getEdges().iterator();slink.hasNext();){
			SubstrateLink tmpsl = slink.next();
			for(AbstractResource asrc : tmpsl){
				if(asrc instanceof BandwidthResource){
					bwResource = (BandwidthResource) asrc;
					break;
				}
			}
			
			
		}
		
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
		
		//P(v) : pre-selected paths
		for (Iterator<VirtualLink> vlink = vNet.getEdges().iterator(); vlink.hasNext();) {
			VirtualLink tmpvl = vlink.next();
			srcSnode = nodeMapping.get(vNet.getSource(tmpvl));
			dstSnode = nodeMapping.get(vNet.getDest(tmpvl));
			LinkWeight linkWeight = new LinkWeight();
			Yen<SubstrateNode, SubstrateLink> yen = new Yen(sNet,linkWeight);
			preselectedPath.put(tmpvl, yen.getShortestPaths(srcSnode, dstSnode, SOD_BK.preselected_number));
		}
	
		double delta = 0.01;
		//begin objective
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
						
				for(int i=0;i<preselectedPath.get(tmpvl).size();i++){
					List<SubstrateLink> path = preselectedPath.get(tmpvl).get(i);
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
			
			for(int i=0;i<preselectedPath.get(tmpvl).size();i++){
				List<SubstrateLink> path = preselectedPath.get(tmpvl).get(i);
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
				
			//R(f) : bypass
			LocalBypass<SubstrateNode, SubstrateLink> bypass = new LocalBypass(sNet, new LinkWeight());
			bypassPath.put(failure, bypass.getShortestPaths(failure, SOD_BK.bypass_number));

			for(int i=0;i<bypassPath.get(failure).size();i++){
				List<SubstrateLink> localBypassi = bypassPath.get(failure).get(i);
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
				
				for(int i=0;i<preselectedPath.get(tmpvl).size();i++){
					List<SubstrateLink> path = preselectedPath.get(tmpvl).get(i);
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
				

				//yr(f)
				boolean flag = false; // eliminate empty left term
				for(int i=0;i<bypassPath.get(failure).size();i++){
					List<SubstrateLink> localBypassi = bypassPath.get(failure).get(i);
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
					double ZSYsf = bwResource.getReservedBackupBw()-bwResource.getLinkBackupBw(failure);
					restoration_bw = restoration_bw  + " - Zs#" + tmpsl.getId() + " <= "  + ZSYsf +"\n";		
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
