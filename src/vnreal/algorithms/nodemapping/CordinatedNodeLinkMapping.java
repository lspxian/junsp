package vnreal.algorithms.nodemapping;

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
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import vnreal.algorithms.AbstractNodeMapping;
import vnreal.algorithms.linkmapping.MultiCommodityFlow;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.algorithms.utils.Remote;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.demands.CpuDemand;
import vnreal.network.substrate.AugmentedNetwork;
import vnreal.network.substrate.MetaLink;
import vnreal.network.substrate.MetaNode;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;
import vnreal.resources.CpuResource;

public class CordinatedNodeLinkMapping extends AbstractNodeMapping {
	protected String localPath="ILP-LP-Models/tr2mcf.lp";
	protected String remotePath = "pytest/vne-mcf.lp";

	
	public CordinatedNodeLinkMapping(SubstrateNetwork sNet, boolean subsNodeOverload) {
		super(sNet, subsNodeOverload);
	}
	
	//no overload by default
	public CordinatedNodeLinkMapping(SubstrateNetwork sNet){
		super(sNet);
	}
	
	public CordinatedNodeLinkMapping(SubstrateNetwork sNet,String localPath){
		super(sNet);
		this.localPath=localPath;
	}

	@Override
	public boolean nodeMapping(VirtualNetwork vNet) {
		
		AugmentedNetwork an = new AugmentedNetwork(this.sNet);
		Map<VirtualNode, MetaNode> virToMeta=new HashMap<VirtualNode, MetaNode>();
		Double res=0.0,xns=0.0,max=0.0;
		int indice=-1;
		
		this.createAugmentedNetwork(vNet, an, virToMeta);	
		try {
			this.generateFile(vNet, an, virToMeta);
			
			Map<String, String> solution = this.cplexOptimizationLocal();
//			Map<String, String> solution = this.cplexOptimizationCloud();
			if(solution.isEmpty()){
				System.out.println("not good");
			}
			System.out.println(solution);
			
			//TODO	cplex result analysis and rounding method
			
			Set<String> keys = new TreeSet<String>();
			String kind1="",kind2="";
		/*	keys = solution.keySet();
			Iterator<String> it = keys.iterator();
			while (it.hasNext()){
				String key1 = it.next();
				if (key1.startsWith("X")){
					kind1=key1.substring(key1.indexOf("Xm")+2, key1.indexOf("w"));
					//System.out.println(kind1);
					kind2=key1.substring(key1.indexOf("w")+1);
					//System.out.println(kind2);
					for(Entry<VirtualNode, MetaNode> entry :virToMeta.entrySet()){
						if (entry.getValue().getId() == Integer.parseInt(kind1))
							nodeMapping.put(entry.getKey(), sNet.getNodeFromID(Integer.parseInt(kind2)));
					}
				}
			}
			System.out.println(nodeMapping);	*/
			//For match a virtualNode with a unique substrateNode
			for (VirtualNode vn : vNet.getVertices()){
				String vind = String.valueOf(virToMeta.get(vn).getId()).trim();
				//System.out.println(vind);
				
				for (SubstrateNode sn : sNet.getVertices()){
					String sind = String.valueOf(sn.getId()).trim();
					//System.out.println(sind);
					keys = solution.keySet();
					Iterator<String> it2 = keys.iterator();
					//Get the indice from the cplex result
					while (it2.hasNext()){
						String key = it2.next();
						if (key.startsWith("v")){
							kind1=key.substring(key.indexOf("ss")+2, key.indexOf("sd"));
							//System.out.println(kind1);
							kind2=key.substring(key.indexOf("sd")+2);
							//System.out.println(kind2);
						}
						if (key.startsWith("X")){
							kind1=key.substring(key.indexOf("Xm")+2, key.indexOf("w"));
							//System.out.println(kind1);
							kind2=key.substring(key.indexOf("w")+1);
							//System.out.println(kind2);
						}
						//To calculate the best substrateNode
						if ((kind1.equals(sind) || kind2.equals(sind)) && (kind1.equals(vind) || kind2.equals(vind)) && key.startsWith("v"))
							res += Double.parseDouble(solution.get(key));
						if ((kind1.equals(sind) || kind2.equals(sind)) && (kind1.equals(vind) || kind2.equals(vind)) && key.startsWith("X"))
							xns = Double.parseDouble(solution.get(key));
					};
					res = res*xns;
					if((res > max)&&(
							!nodeMapping.values().contains(sNet.getNodeFromID(Integer.parseInt(sind))))){
						max = res;
						indice = Integer.parseInt(sind);
					}
					res = 0.0;
					xns = 0.0;
				}
				
				
				System.out.println(max);
				System.out.println(indice);
				//Put the correspondence in a HashMap
				if (indice != -1)
				nodeMapping.put(vn, sNet.getNodeFromID(indice));
				max = 0.0;
				indice = -1;
			}
			//To show the Map
			System.out.println(nodeMapping);
		
			for(Entry<VirtualNode, SubstrateNode> e :nodeMapping.entrySet()){
				if (NodeLinkAssignation.vnm(e.getKey(), e.getValue())) {
				} else {
					throw new AssertionError("But we checked before!");
				}
			}
			
			
			
			
		}catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param vNet	virtual network demand
	 * @param an	augmented network(with meta nodes and meta links)
	 * @param virToMeta	virtual node and meta node correspondence
	 */
	public void createAugmentedNetwork(VirtualNetwork vNet, AugmentedNetwork an, Map<VirtualNode, MetaNode> virToMeta){
		List<SubstrateNode> candidates;
		AvailableResourcesNodeMapping arnm = new AvailableResourcesNodeMapping(this.sNet,30,true,false);
		
		for(Iterator<VirtualNode> itt = vNet.getVertices().iterator(); itt
			.hasNext();)
		{
			VirtualNode currNode = itt.next();
			MetaNode mnode = new MetaNode(currNode);
			mnode.setCoordinateX(currNode.getCoordinateX());
			mnode.setCoordinateY(currNode.getCoordinateY());
			//mnode.addResource(currNode.);
			an.addVertex(mnode);
			virToMeta.put(currNode, mnode);
			candidates = arnm.SearchCandidates(currNode);
			System.out.println(mnode+" : "+candidates);
			for (SubstrateNode node : candidates ){
				MetaLink mlink = new MetaLink();
				mlink.addResource(1000);
				an.addEdge(mlink, mnode, node);
			}
			
		}
	}
	
	/**
	 * perform cplex linear optimization using local installed ILOG cplex file
	 * @return
	 * @throws IOException
	 */
	public Map<String, String> cplexOptimizationLocal() throws IOException{
		Map<String, String> solution = new HashMap<String, String>();
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
		return solution;
	}
	
	/**
	 * perform cplex linear optimization using cloud Paris 13
	 * @return
	 * @throws IOException
	 */
	public Map<String, String> cplexOptimizationCloud() throws IOException{
		Remote remote = new Remote();
		Map<String, String> solution = new HashMap<String, String>();
		try {
			
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
	
	/**
	 * generate cplex file for undirected multi commodity flow
	 * @param vNet	virtual network demand
	 * @param aNet	augmented network(with meta nodes and meta links)
	 * @param virToMeta	virtual node and meta node correspondence
	 * @throws IOException
	 */
		public void generateFile(VirtualNetwork vNet,
				AugmentedNetwork aNet,
				Map<VirtualNode, MetaNode> virToMeta) throws IOException{
			BandwidthDemand bwDem = null;
			BandwidthResource bwResource=null;
			CpuResource cpuResource = null;
			CpuDemand cpuDem=null;
			SubstrateNode ssnode=null, dsnode=null;
			VirtualNode srcVnode = null, dstVnode = null;

			String preambule = "\\Problem : vne\n";
			String obj = "Minimize\n"+"obj : ";
			String constraint = "Subject To\n";
			String bounds = "Bounds\n";
			String general = "General\n";

			/*--------nodes--------*/
			for(SubstrateNode tmpNode : aNet.getRoot().getVertices()){
				for(AbstractResource asrc : tmpNode){
					if(asrc instanceof CpuResource){
						cpuResource = (CpuResource) asrc;
						break;
					}
				}
				
				//constraint = constraint + "0";
				for(MetaNode metaNode : aNet.getMetaNodes()){
					if(aNet.findEdge(tmpNode, metaNode)!=null){
						for(AbstractDemand asrc2 : metaNode.getRoot()){
							if(asrc2 instanceof CpuDemand){
								cpuDem = (CpuDemand) asrc2;
								break;
							}
						}
						
						//obj
						obj = obj + " + "+cpuDem.getDemandedCycles()/(cpuResource.getAvailableCycles()+0.001);
						obj = obj + " Xm"+metaNode.getId()+"w"+tmpNode.getId();
						
						//constraints : capacity
						constraint = constraint + " + "+cpuDem.getDemandedCycles();
						constraint = constraint + " Xm"+metaNode.getId()+"w"+tmpNode.getId();
						
						//bounds
						bounds = bounds + "0<=Xm"+metaNode.getId()+"w"+tmpNode.getId()+"<=1\n";						
					}
				}
				constraint = constraint + "<="+cpuResource.getAvailableCycles()+"\n";
			}
			
			for(SubstrateNode tmpNode : aNet.getRoot().getVertices()){
				//constraint = constraint + "0";
				for(MetaNode metaNode : aNet.getMetaNodes()){
					if(aNet.findEdge(tmpNode, metaNode)!=null){
						constraint = constraint + "+Xm"+metaNode.getId()+"w"+tmpNode.getId();						
					}
				}
				constraint = constraint + "<=1\n";
			}
			
			for(MetaNode metaNode : aNet.getMetaNodes()){
				for(SubstrateNode tmpNode : aNet.getRoot().getVertices()){
					//constraints
					if(aNet.findEdge(tmpNode, metaNode)!=null){
						constraint = constraint + " +Xm"+metaNode.getId()+"w"+tmpNode.getId();						
					}
				}
				constraint = constraint + "=1\n";
			}
			
			
			
			/*--------links-------*/
			
			for (VirtualLink tmpl : vNet.getEdges()) {

				// Find their mapped SubstrateNodes
				srcVnode = vNet.getEndpoints(tmpl).getFirst();
				dstVnode = vNet.getEndpoints(tmpl).getSecond();
				
				// Get current VirtualLink demand
				bwDem = tmpl.getBandwidthDemand();
				
				
				
				for (Iterator<SubstrateLink> slink = aNet.getEdges().iterator();slink.hasNext();){
					SubstrateLink tmpsl = slink.next();
					ssnode = aNet.getEndpoints(tmpsl).getFirst();
					dsnode = aNet.getEndpoints(tmpsl).getSecond();
					bwResource = tmpsl.getBandwidthResource();
					
					//objective
					obj = obj + " + "+bwDem.getDemandedBandwidth()/(bwResource.getAvailableBandwidth()+0.001);
					obj = obj + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId();
					obj = obj + " + "+bwDem.getDemandedBandwidth()/(bwResource.getAvailableBandwidth()+0.001);
					obj = obj + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId();
					
					//bounds
					bounds = bounds + "0 <= vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+" <= 1\n";
					bounds = bounds + "0 <= vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId()+" <= 1\n";
				}
				
				//flow constraints
				Collection<SubstrateNode> nextHop ;
				for(SubstrateNode snode: sNet.getVertices()){
					nextHop = aNet.getNeighbors(snode);
					for(SubstrateNode tmmpsn : nextHop){
						SubstrateLink thisSL = aNet.findEdge(snode, tmmpsn);
						if((thisSL instanceof MetaLink)&&(!(tmmpsn.equals(virToMeta.get(srcVnode))||tmmpsn.equals(virToMeta.get(dstVnode)))))
						{}
						else{
							constraint=constraint+" + vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+snode.getId()+"sd"+tmmpsn.getId();
							constraint=constraint+" - vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+tmmpsn.getId()+"sd"+snode.getId();							
						}
					}
					constraint =constraint+" = 0\n";
		
				}
				
				nextHop = aNet.getNeighbors(virToMeta.get(srcVnode));
				for(SubstrateNode tmmpsn : nextHop){
					constraint=constraint+" + vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+virToMeta.get(srcVnode).getId()+"sd"+tmmpsn.getId();
					constraint=constraint+" - vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+tmmpsn.getId()+"sd"+virToMeta.get(srcVnode).getId();							
				}
				constraint =constraint+" = 1\n";
				nextHop = aNet.getNeighbors(virToMeta.get(dstVnode));
				for(SubstrateNode tmmpsn : nextHop){
					constraint=constraint+" + vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+virToMeta.get(dstVnode).getId()+"sd"+tmmpsn.getId();
					constraint=constraint+" - vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+tmmpsn.getId()+"sd"+virToMeta.get(dstVnode).getId();							
				}
				constraint =constraint+" = -1\n";
				
			}
			
			//capacity constraint
			for (Iterator<SubstrateLink> slink = aNet.getEdges().iterator();slink.hasNext();){
				SubstrateLink tmpsl = slink.next();
				ssnode = aNet.getEndpoints(tmpsl).getFirst();
				dsnode = aNet.getEndpoints(tmpsl).getSecond();
				
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
			
			for(MetaNode metaNode : aNet.getMetaNodes()){
				for(SubstrateNode tmpNode:aNet.getNeighbors(metaNode)){
					for(VirtualLink vlink:vNet.getEdges()){
						srcVnode = vNet.getEndpoints(vlink).getFirst();
						dstVnode = vNet.getEndpoints(vlink).getSecond();
						
						//f and x constraint
						constraint=constraint+" + vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+metaNode.getId()+"sd"+tmpNode.getId();
						constraint=constraint+" + vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+tmpNode.getId()+"sd"+metaNode.getId();
						
					}
					
					constraint = constraint + " -10 Xm"+metaNode.getId()+"w"+tmpNode.getId()+"<=0\n";
				}
			}
			
			obj = obj+ "\n";
			BufferedWriter writer = new BufferedWriter(new FileWriter(localPath));
			writer.write(preambule+obj+constraint+bounds+general+"END");
			writer.close();
			
		}
		
		
		
		

}
