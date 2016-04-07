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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import vnreal.algorithms.AbstractNodeMapping;
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
		
		this.createAugmentedNetwork(vNet, an, virToMeta);	
		try {
			this.generateFile(vNet, an, virToMeta);
			
//			Map<String, String> solution = this.cplexOptimizationLocal();
			Map<String, String> solution = this.cplexOptimizationCloud();
			System.out.println(solution);
			
			//TODO	cplex result analysis and rounding method

			
			
			
			
		} catch (IOException e1) {
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
		AvailableResourcesNodeMapping arnm = new AvailableResourcesNodeMapping(this.sNet,50,true,false);
		
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
				
				for (Iterator<SubstrateLink> slink = aNet.getEdges().iterator();slink.hasNext();){
					SubstrateLink tmpsl = slink.next();
					ssnode = aNet.getEndpoints(tmpsl).getFirst();
					dsnode = aNet.getEndpoints(tmpsl).getSecond();
					
					for(AbstractResource asrc : tmpsl){
						if(asrc instanceof BandwidthResource){
							bwResource = (BandwidthResource) asrc;
							break;
						}
					}
					
					//objective
					obj = obj + " + "+bwDem.getDemandedBandwidth()/(bwResource.getAvailableBandwidth()+0.001);
//					obj = obj + " + "+bwDem.getDemandedBandwidth();
					obj = obj + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId();
					obj = obj + " + "+bwDem.getDemandedBandwidth()/(bwResource.getAvailableBandwidth()+0.001);
//					obj = obj + " + "+bwDem.getDemandedBandwidth();
					obj = obj + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId();
					
					//integer in the <general>
					//general = general +  " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+"\n";
					
					//bounds
					bounds = bounds + "0 <= vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+" <= 1\n";
					bounds = bounds + "0 <= vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId()+" <= 1\n";
				}
				
				//flow constraints
				Collection<SubstrateNode> nextHop = new ArrayList<SubstrateNode>();
				for(Iterator<SubstrateNode> iterator = aNet.getVertices().iterator();iterator.hasNext();){
					SubstrateNode snode = iterator.next();
					nextHop = aNet.getNeighbors(snode);
					for(Iterator<SubstrateNode> it=nextHop.iterator();it.hasNext();){
						SubstrateNode tmmpsn = it.next();
						constraint=constraint+" + vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+snode.getId()+"sd"+tmmpsn.getId();
						constraint=constraint+" - vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+tmmpsn.getId()+"sd"+snode.getId();
					}

					if(snode.equals(virToMeta.get(srcVnode)))	constraint =constraint+" = 1\n";
					else if(snode.equals(virToMeta.get(dstVnode))) constraint =constraint+" = -1\n";
					else	constraint =constraint+" = 0\n";
					
				}
				
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
			
			obj = obj+ "\n";
			BufferedWriter writer = new BufferedWriter(new FileWriter(localPath));
			writer.write(preambule+obj+constraint+bounds+general+"END");
			writer.close();
			
		}

}
