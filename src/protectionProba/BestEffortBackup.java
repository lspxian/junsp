package protectionProba;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.resources.BandwidthResource;

public class BestEffortBackup extends AbstractBackupMapping {
	
	public BestEffortBackup(SubstrateNetwork sNet) {
		super(sNet);
	}
	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<BandwidthDemand, SubstrateLink> primary) {
		Map<String, String> solutionB = linkMappingWithoutUpdateLocal(vNet, primary);		
		if(solutionB.size()==0){
			System.out.println("Backup link no solution");
			NodeLinkDeletion.freeResource(vNet, sNet);
			return false;
		}
		//update
		updateResource(vNet, solutionB);
		return true;
	}
	
	public Map<String,String> linkMappingWithoutUpdateLocal(VirtualNetwork vNet,Map<BandwidthDemand, SubstrateLink> primary) {
		Map<String, String> solutionB = new HashMap<String, String>();
		//generate .lp file
		try {
			this.generateFile(vNet,primary);
			Process p = Runtime.getRuntime().exec("python cplex/mysolver.py cplex/vne-mcf.lp o");
			InputStream in = p.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String readLine;
			boolean solBegin=false;
			while (((readLine = br.readLine()) != null)) {
				if(solBegin==true){
					System.out.println(readLine);
					StringTokenizer st = new StringTokenizer(readLine, " ");
					solutionB.put(st.nextToken(), st.nextToken());
				}
				if(solBegin==false&&readLine.equals("The solutions begin here : "))
					solBegin=true;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return solutionB;
	}
	
	public void updateResource(VirtualNetwork vNet, Map<String,String> solutionB){
		
		for(Map.Entry<String, String> entry : solutionB.entrySet()){
			String linklink = entry.getKey();
			if(linklink.startsWith("B")){
				linklink = linklink.replaceAll("[^0-9]+", " ");
				List<String> list=Arrays.asList(linklink.split(" "));
				VirtualLink tmpvl = vNet.findEdge(
						vNet.getNodeFromID(Integer.parseInt(list.get(4))), 
						vNet.getNodeFromID(Integer.parseInt(list.get(5))));				
				SubstrateLink primaryLink = sNet.findEdge(
						sNet.getNodeFromID(Integer.parseInt(list.get(2))),
						sNet.getNodeFromID(Integer.parseInt(list.get(3))));
				SubstrateLink backupLink = sNet.findEdge(
						sNet.getNodeFromID(Integer.parseInt(list.get(0))),
						sNet.getNodeFromID(Integer.parseInt(list.get(1))));
				if(!NodeLinkAssignation.backup(tmpvl, primaryLink, backupLink, true))
					throw new AssertionError("But we checked before!");
			}
		}
	}
	
	public void generateFile(VirtualNetwork vNet, Map<BandwidthDemand, SubstrateLink> solutionP) throws IOException{
		String preambule = "\\Problem : vne\n";
		StringBuilder obj = new StringBuilder("Minimize\n"+"obj : ");
		StringBuilder constraint = new StringBuilder("Subject To\n");
		StringBuilder bounds = new StringBuilder("Bounds\n");
		StringBuilder binary = new StringBuilder("Binary\n");
		
		Set<SubstrateLink> riskLinks=new HashSet<SubstrateLink>();
		for(Map.Entry<BandwidthDemand, SubstrateLink> e:solutionP.entrySet()){
			riskLinks.add(e.getValue());
		}
		
		//Objective
		for(SubstrateLink sl:riskLinks){
			long iid = sNet.getEndpoints(sl).getFirst().getId();
			long jid = sNet.getEndpoints(sl).getSecond().getId();
			double logp=-Math.log(1-sl.getProbability())*1000;
			obj.append(" + "+logp+"-"+logp+" Yn"+iid+"n"+jid);
			obj.append(" + "+"detn"+iid+"d"+jid);
			
			binary.append(" Yn"+iid+"n"+jid+"\n");	//Y binary 
			bounds.append("detn"+iid+"d"+jid+">=0\n");	//delta>=0
		}
		
		for(SubstrateLink sl:sNet.getEdges()){
			BandwidthResource bwr=sl.getBandwidthResource();
			long mid = sNet.getEndpoints(sl).getFirst().getId();
			long nid = sNet.getEndpoints(sl).getSecond().getId();
			
			for(SubstrateLink rl:riskLinks){
				if(!rl.equals(sl)){
					Risk risk=bwr.findRiskByLink(rl);
					long iid = sNet.getEndpoints(rl).getFirst().getId();
					long jid = sNet.getEndpoints(rl).getSecond().getId();
					//additional bakckup bandwidth constraint
					for(Map.Entry<BandwidthDemand, SubstrateLink> e:solutionP.entrySet()){
						if(e.getValue().equals(rl)){
							double bw=e.getKey().getDemandedBandwidth();
							VirtualLink vl=(VirtualLink) e.getKey().getOwner();
							long vaid = vNet.getEndpoints(vl).getFirst().getId();
							long vbid = vNet.getEndpoints(vl).getSecond().getId();
							//additional bakckup bandwidth constraint
							String b1=" Bs"+mid+"d"+nid+"Rn"+iid+"n"+jid+"Vn"+vaid+"n"+vbid;
							String b2=" Bs"+nid+"d"+mid+"Rn"+iid+"n"+jid+"Vn"+vaid+"n"+vbid;
							constraint.append("+ "+bw+b1);
							constraint.append("+ "+bw+b2);
							
							//b binary
							binary.append(b1+"\n");
							binary.append(b2+"\n");
						}
					}
					//additional backup bandwidth constraint
					constraint.append(" - detn"+mid+"d"+nid+"+");
					constraint.append(risk.getTotal()-bwr.getReservedBackupBw());
					constraint.append("<=0\n");
				}
			}
			
			//bandwidth capacity
			constraint.append("detn"+mid+"d"+nid+"<="+bwr.getAvailableBandwidth()+"\n");
		}
		//backup flow constraint
		for(Map.Entry<BandwidthDemand, SubstrateLink> e:solutionP.entrySet()){
			VirtualLink vl=(VirtualLink) e.getKey().getOwner();
			long vaid = vNet.getEndpoints(vl).getFirst().getId();
			long vbid = vNet.getEndpoints(vl).getSecond().getId();
			
			long iid = sNet.getEndpoints(e.getValue()).getFirst().getId();
			long jid = sNet.getEndpoints(e.getValue()).getSecond().getId();
			for(SubstrateNode mnode:sNet.getVertices()){
				long mid=mnode.getId();
				Collection<SubstrateNode> nextHop = sNet.getNeighbors(mnode);
				for(SubstrateNode nnode:nextHop){
					long nid=nnode.getId();
					if(!(((mid==iid)&&(nid==jid))||((mid==jid)&&(nid==iid)))){
						constraint.append("+ "+" Bs"+mid+"d"+nid);
						constraint.append("Rn"+iid+"n"+jid);
						constraint.append("Vn"+vaid+"n"+vbid);
						
						constraint.append("- "+" Bs"+nid+"d"+mid);
						constraint.append("Rn"+iid+"n"+jid);
						constraint.append("Vn"+vaid+"n"+vbid);
					}
				}
				if(mid==iid) constraint.append(" - Yn"+iid+"n"+jid+" =0\n");
				else if(mid==jid) constraint.append(" + Yn"+iid+"n"+jid+" =0\n");
				else constraint.append(" =0\n");
			}
		}
		
		obj.append("\n");
		BufferedWriter writer = new BufferedWriter(new FileWriter("cplex/vne-mcf.lp"));
		writer.write(preambule+obj+constraint+bounds+binary+"END");
		writer.close();
	}
}