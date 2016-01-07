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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import edu.uci.ics.jung.graph.util.EdgeType;
import li.multiDomain.Domain;
import vnreal.algorithms.AbstractMultiDomainLinkMapping;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.algorithms.utils.Remote;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.InterLink;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;

public class MultiDomainAsOneDomain extends AbstractMultiDomainLinkMapping {

	public MultiDomainAsOneDomain(List<Domain> domains) {
		super(domains);
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet,
			Map<VirtualNode, SubstrateNode> nodeMapping) {
		
		Domain newDomain = merge(domains);
		
		Map<String, String> solution = this.linkMappingWithoutUpdateLocal(vNet, nodeMapping, newDomain);
		
		if(solution.size()==0){
			System.out.println("link no solution");
			for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
				NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
			}
			for(Domain d : domains){
				//restore node coordinate to [0,100]
				for(SubstrateNode snode : d.getVertices()){
					double x = snode.getCoordinateX()-d.getCoordinateX()*100;
					double y = snode.getCoordinateY()-d.getCoordinateY()*100;
					snode.setCoordinateX(x);
					snode.setCoordinateY(y);
				}
			}
			return false;
		}
		
		//update
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
			
			srcSnode = newDomain.getNodeFromID(srcSnodeId);
			dstSnode = newDomain.getNodeFromID(dstSnodeId);
			SubstrateLink tmpsl = newDomain.findEdge(srcSnode, dstSnode);
			
			newBwDem = new BandwidthDemand(tmpvl);
			newBwDem.setDemandedBandwidth(bwDem.getDemandedBandwidth()*flow);
			
			if(!NodeLinkAssignation.vlmSingleLinkSimple(newBwDem, tmpsl)){
				throw new AssertionError("But we checked before!");
			}
			
		}
		
		for(Domain domain : domains){
			for(SubstrateNode snode : domain.getVertices()){
				double x = snode.getCoordinateX()-domain.getCoordinateX()*100;
				double y = snode.getCoordinateY()-domain.getCoordinateY()*100;
				snode.setCoordinateX(x);
				snode.setCoordinateY(y);
			}
		}
		
		return true;
	}

	public Map<String,String> linkMappingWithoutUpdate(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping, Domain sNet) {
		Remote remote = new Remote();
		Map<String, String> solution = new HashMap<String, String>();
		try {
			//generate .lp file
			this.generateFile(vNet, nodeMapping, sNet);
			
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
	
	public Map<String,String> linkMappingWithoutUpdateLocal(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping, Domain sNet) {
		Map<String, String> solution = new HashMap<String, String>();
		//generate .lp file
		try {
			this.generateFile(vNet, nodeMapping,sNet);
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
	
	//generate cplex file for undirected multi commodity flow
		public void generateFile(VirtualNetwork vNet,Map<VirtualNode, SubstrateNode> nodeMapping, Domain sNet) throws IOException{
			BandwidthDemand bwDem = null;
			BandwidthResource bwResource=null;
			SubstrateNode ssnode=null, dsnode=null;
			VirtualNode srcVnode = null, dstVnode = null;

			String preambule = "\\Problem : vne\n";
			String obj = "Minimize\n"+"obj : ";
			String constraint = "Subject To\n";
			String bounds = "Bounds\n";
			String general = "General\n";
			int k=1;

			for (Iterator<VirtualLink> links = vNet.getEdges().iterator(); links.hasNext();) {
				VirtualLink tmpl = links.next();

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
			
				for (Iterator<SubstrateLink> slink = sNet.getEdges().iterator();slink.hasNext();){
					SubstrateLink tmpsl = slink.next();
					ssnode = sNet.getEndpoints(tmpsl).getFirst();
					dsnode = sNet.getEndpoints(tmpsl).getSecond();
					
					for(AbstractResource asrc : tmpsl){
						if(asrc instanceof BandwidthResource){
							bwResource = (BandwidthResource) asrc;
						}
					}
					
					if(tmpsl instanceof InterLink)	k=10;
					else k=1;
					//objective
//					obj = obj + " + "+bwDem.getDemandedBandwidth()/(bwResource.getAvailableBandwidth()+0.001)*k;
					obj = obj + " + "+bwDem.getDemandedBandwidth()*k;
					obj = obj + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId();
//					obj = obj + " + "+bwDem.getDemandedBandwidth()/(bwResource.getAvailableBandwidth()+0.001)*k;
					obj = obj + " + "+bwDem.getDemandedBandwidth()*k;
					obj = obj + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId();
					
					//integer in the <general>
					//general = general +  " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+"\n";
					
					//bounds
					bounds = bounds + "0 <= vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+" <= 1\n";
					bounds = bounds + "0 <= vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId()+" <= 1\n";
				}
				
				//flow constraints
				Collection<SubstrateNode> nextHop = new ArrayList<SubstrateNode>();
				for(Iterator<SubstrateNode> iterator = sNet.getVertices().iterator();iterator.hasNext();){
					SubstrateNode snode = iterator.next();
					nextHop = sNet.getNeighbors(snode);
					for(Iterator<SubstrateNode> it=nextHop.iterator();it.hasNext();){
						SubstrateNode tmmpsn = it.next();
						constraint=constraint+" + vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+snode.getId()+"sd"+tmmpsn.getId();
						constraint=constraint+" - vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+tmmpsn.getId()+"sd"+snode.getId();
					}

					if(snode.equals(nodeMapping.get(srcVnode)))	constraint =constraint+" = 1\n";
					else if(snode.equals(nodeMapping.get(dstVnode))) constraint =constraint+" = -1\n";
					else	constraint =constraint+" = 0\n";
					
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
			writer.write(preambule+obj+constraint+bounds+general+"END");
			writer.close();
			
		}

	
	
	//merge multi-domain
	private Domain merge(List<Domain> domains) {
		
		Domain newDomain = new Domain();
		for(Domain domain : domains){
			//add substrate node
			for(SubstrateNode snode : domain.getVertices()){
				double x = snode.getCoordinateX()+domain.getCoordinateX()*100;
				double y = snode.getCoordinateY()+domain.getCoordinateY()*100;
				snode.setCoordinateX(x);
				snode.setCoordinateY(y);
			}
			//add substrate link
			for(SubstrateLink sl : domain.getEdges()){
				newDomain.addEdge(sl, domain.getEndpoints(sl).getFirst(), domain.getEndpoints(sl).getSecond(), EdgeType.UNDIRECTED);
			}
			//add inter link
			for(InterLink il : domain.getInterLink()){
				if(!newDomain.getInterLink().contains(il)){
					newDomain.getInterLink().add(il);
					newDomain.addEdge(il, il.getNode1(), il.getNode2(), EdgeType.UNDIRECTED);
				}
			}
		}
		
		return newDomain;
	}
	
}
