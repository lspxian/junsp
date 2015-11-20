package vnreal.algorithms.linkmapping;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.util.EdgeType;
import li.multiDomain.Domain;
import vnreal.algorithms.AbstractMultiDomainLinkMapping;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.algorithms.utils.Remote;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.AugmentedLink;
import vnreal.network.substrate.AugmentedNetwork;
import vnreal.network.substrate.InterLink;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.AugmentedVirtualLink;
import vnreal.network.virtual.VirtualInterLink;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;

public class MultiDomainRanking extends AbstractMultiDomainLinkMapping {
	
	Map<Domain, VirtualNetwork> localVNets;
	Map<Domain, AugmentedNetwork> augmentedNets;
	Map<BandwidthDemand, BandwidthResource> mapping;
	List<VirtualLink> linkToMap;
	
	public MultiDomainRanking(List<Domain> domains) {
		super(domains);
		this.intialize();
	}
	public MultiDomainRanking(List<Domain> domains,String localPath, String remotePath){
		super(domains,localPath, remotePath);
		this.intialize();
	}
	
	private void intialize(){
		this.localVNets =  new LinkedHashMap<Domain, VirtualNetwork>();
		this.augmentedNets = new LinkedHashMap<Domain, AugmentedNetwork>();
		for(Domain d : domains){
			localVNets.put(d, new VirtualNetwork());
			augmentedNets.put(d, new AugmentedNetwork(d));	//intra substrate links
			for(InterLink tmplink : d.getInterLink()){
				augmentedNets.get(d).addEdge(tmplink, tmplink.getNode1(), tmplink.getNode2(), EdgeType.UNDIRECTED);	//inter substrate links
			}
		}		
		this.mapping = new LinkedHashMap<BandwidthDemand, BandwidthResource>();
		this.linkToMap = new ArrayList<VirtualLink>();
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		
		//initialize the links to map, delete the link once it's mapped
		this.linkToMap.addAll(vNet.getEdges());
		
		for(Domain domain : this.domains){
			this.createLocalVNet(domain, vNet, nodeMapping);

			//if there is no virtual links in this domain
			if(this.localVNets.get(domain).getEdgeCount()!=0){
				this.fulfillAugmentedNet(domain, vNet, nodeMapping);
				
				Map<String, String> solution = this.linkMappingWithoutUpdate(vNet, nodeMapping, this.augmentedNets.get(domain));
				
				if(solution.size()==0){
					System.out.println("link no solution");
					for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
						NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
					}
					return false;
				}
				this.updateResource(solution, domain, nodeMapping);
			}
			
		}
		
		return true;
	}
	
	private void createLocalVNet(Domain domain, VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping){
		//Create virtual network for each domain, transform virtual link to virtual inter link, this means to add source domain and destination domain.
		VirtualNetwork tmpvn = this.localVNets.get(domain);
		for(VirtualLink vlink : this.linkToMap){
			VirtualNode vSource = vNet.getEndpoints(vlink).getFirst();
			VirtualNode vDest = vNet.getEndpoints(vlink).getSecond();
			SubstrateNode sSource = nodeMapping.get(vSource);
			SubstrateNode sDest = nodeMapping.get(vDest);

			if(domain.containsVertex(sSource)&&domain.containsVertex(sDest)){	//virtual intra link
				tmpvn.addEdge(vlink, vSource, vDest, EdgeType.UNDIRECTED);
			}
			else if(domain.containsVertex(sSource)||domain.containsVertex(sDest))	//virtual inter link
				tmpvn.addEdge(new VirtualInterLink(vlink,vSource,vDest), vSource, vDest, EdgeType.UNDIRECTED);
		}

	}
	
	private void fulfillAugmentedNet(Domain domain, VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping){
		VirtualNetwork tmpvn = this.localVNets.get(domain);
		AugmentedNetwork an = this.augmentedNets.get(domain);
		for(VirtualLink vl : tmpvn.getEdges()){
			if(vl instanceof VirtualInterLink){
				VirtualInterLink vil = (VirtualInterLink) vl;
				VirtualNode vnode1 = vil.getNode1();
				VirtualNode vnode2 = vil.getNode2();
				SubstrateNode snode1 = nodeMapping.get(vnode1);
				SubstrateNode snode2 = nodeMapping.get(vnode2);
				SubstrateNode dijkDest = null, dijkSource = null;
				Domain exterDomain = null;
				if(vnode1.getDomain().equals(domain)){
					exterDomain = vnode2.getDomain();
					dijkDest = snode2;
				}
				else if(vnode2.getDomain().equals(domain)){
					exterDomain = vnode1.getDomain();
					dijkDest = snode1;
				}
				else	continue;
				for(InterLink ilink : domain.getInterLink()){
					if(domain.containsVertex(ilink.getNode1()))
						dijkSource = ilink.getNode2();
					else if(domain.containsVertex(ilink.getNode2()))
						dijkSource = ilink.getNode1();
					
					if(exterDomain.containsVertex(dijkSource)&&
							(!dijkSource.equals(dijkDest))&&			//mapped substrate node is the border node
							(!an.existLink(dijkSource, dijkDest))){		//augmented link does not exist in the augmented network
						
						DijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode, SubstrateLink>(exterDomain);
						AugmentedLink al = new AugmentedLink();
						double cost = (double) dijkstra.getDistance(dijkSource, dijkDest);
						//System.out.println(cost);
						al.setCost(cost);
						al.addResource(100/(cost));	//normally random(0,1), here random = 100 means that it has infinite bw
						an.addEdge(al, dijkSource, dijkDest, EdgeType.UNDIRECTED);	//augmented links
						
					}
				}
			}
		}
		
	}
	
	public Map<String,String> linkMappingWithoutUpdate(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping, AugmentedNetwork an) {
		Remote remote = new Remote();
		Map<String, String> solution = new HashMap<String, String>();
		try {
			//generate .lp file
			this.generateFile(vNet, nodeMapping, an);
			
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
	
	private void generateFile(VirtualNetwork vNet,Map<VirtualNode, SubstrateNode> nodeMapping, AugmentedNetwork an) throws IOException{
		Domain tmpDomain = (Domain) an.getRoot(); 
		BandwidthDemand bwDem = null;
		BandwidthResource bwResource=null;
		SubstrateNode ssnode=null, dsnode=null;
		VirtualNode srcVnode = null, dstVnode = null;

		String preambule = "\\Problem : vne\n";
		String obj = "Minimize\n"+"obj : ";
		String constraint = "Subject To\n";
		String bounds = "Bounds\n";
		String general = "General\n";

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
		
			for (Iterator<SubstrateLink> slink = an.getEdges().iterator();slink.hasNext();){
				SubstrateLink tmpsl = slink.next();
				ssnode = an.getEndpoints(tmpsl).getFirst();
				dsnode = an.getEndpoints(tmpsl).getSecond();
				
				if((tmpl instanceof VirtualInterLink)||tmpDomain.containsEdge(tmpsl)){	//for multi domain
					for(AbstractResource asrc : tmpsl){
						if(asrc instanceof BandwidthResource){
							bwResource = (BandwidthResource) asrc;
						}
					}
					
					//objective
					obj = obj + " + "+bwDem.getDemandedBandwidth()/bwResource.getAvailableBandwidth();
					obj = obj + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId();
					obj = obj + " + "+bwDem.getDemandedBandwidth()/bwResource.getAvailableBandwidth();
					obj = obj + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId();
					
					//integer in the <general>
					//general = general +  " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+"\n";
					
					//bounds
					bounds = bounds + "0 <= vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+" <= 1\n";
					bounds = bounds + "0 <= vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId()+" <= 1\n";
				}
				
			}
			
			//flow constraints
			Collection<SubstrateNode> nextHop = new ArrayList<SubstrateNode>();
			for(Iterator<SubstrateNode> iterator = an.getVertices().iterator();iterator.hasNext();){
				SubstrateNode snode = iterator.next();
				if(!(tmpl instanceof VirtualInterLink)&&(!tmpDomain.containsVertex(snode)))	continue;	//multi domain
				nextHop = an.getNeighbors(snode);
				for(Iterator<SubstrateNode> it=nextHop.iterator();it.hasNext();){
					SubstrateNode tmmpsn = it.next();
					if((tmpl instanceof VirtualInterLink)||(tmpDomain.containsVertex(snode)&&tmpDomain.containsVertex(tmmpsn))){	//for multi domain
						constraint=constraint+" + vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+snode.getId()+"sd"+tmmpsn.getId();
						constraint=constraint+" - vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+tmmpsn.getId()+"sd"+snode.getId();
					}
				}

				if(snode.equals(nodeMapping.get(srcVnode)))	constraint =constraint+" = 1\n";
				else if(snode.equals(nodeMapping.get(dstVnode))) constraint =constraint+" = -1\n";
				else	constraint =constraint+" = 0\n";
				
			}
			
		}

		
		//capacity constraint are only for the links of original domain, but not augmented network
		for (Iterator<SubstrateLink> slink = tmpDomain.getAllLinks().iterator();slink.hasNext();){
			SubstrateLink tmpsl = slink.next();
			if(tmpsl instanceof InterLink){
				ssnode = ((InterLink) tmpsl).getNode1();
				dsnode = ((InterLink) tmpsl).getNode2();
			}else{
				ssnode = tmpDomain.getEndpoints(tmpsl).getFirst();
				dsnode = tmpDomain.getEndpoints(tmpsl).getSecond();
			}
			
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
	
	private void updateResource(Map<String, String> solution, Domain domain, Map<VirtualNode, SubstrateNode> nodeMapping){
		VirtualNetwork tmpvn = this.localVNets.get(domain);
		AugmentedNetwork an = this.augmentedNets.get(domain);
		//update in the first domain and create virtual links for second domain
		VirtualNode srcVnode = null, dstVnode = null;
		SubstrateNode srcSnode = null,dstSnode = null;
		int srcVnodeId, dstVnodeId, srcSnodeId, dstSnodeId;
		for(Map.Entry<String, String> entry : solution.entrySet()){
			String linklink = entry.getKey();
			double flow = Double.parseDouble(entry.getValue());
			srcVnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("vs")+2, linklink.indexOf("vd")));
			dstVnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("vd")+2, linklink.indexOf("ss")));
			srcSnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("ss")+2, linklink.indexOf("sd")));
			dstSnodeId = Integer.parseInt(linklink.substring(linklink.indexOf("sd")+2));
			
			srcVnode = tmpvn.getNodeFromID(srcVnodeId);
			dstVnode = tmpvn.getNodeFromID(dstVnodeId);
			srcSnode = an.getNodeFromID(srcSnodeId);
			dstSnode = an.getNodeFromID(dstSnodeId);
			
			VirtualLink tmpvl = tmpvn.findEdge(srcVnode, dstVnode);
			SubstrateLink tmpsl = an.findEdge(srcSnode, dstSnode);
			BandwidthDemand bwDem=null,newBwDem;
			BandwidthResource tmpbd=null;
			Domain exterDomain = null;
			for(AbstractDemand dem : tmpvl){
				if (dem instanceof BandwidthDemand) {
					bwDem = (BandwidthDemand) dem;
					break;
				}
			}
			
			
			if(tmpsl instanceof AugmentedLink){
				VirtualInterLink tmpvil = (VirtualInterLink) tmpvl;
				if(domain.containsVertex(nodeMapping.get(dstVnode))){
					VirtualNode tmp = dstVnode;
					dstVnode = srcVnode;
					srcVnode = tmp;
				}
				exterDomain = dstVnode.getDomain();
				
				if(nodeMapping.get(dstVnode).equals(srcSnode)){
					SubstrateNode tmp = dstSnode;
					dstSnode = srcSnode;
					srcSnode = tmp;
				}
				
				VirtualNode newVNode = new VirtualNode();
				nodeMapping.put(newVNode, srcSnode);
				AugmentedVirtualLink newVLink = new AugmentedVirtualLink(domain, tmpvil.getOrigLink());	//original virtual link
				BandwidthDemand bw=new BandwidthDemand(newVLink);
				bw.setDemandedBandwidth(bwDem.getDemandedBandwidth()*flow);	
				newVLink.add(bw);
				//add augmented virtual link to second domain
				this.localVNets.get(exterDomain).addEdge(newVLink, newVNode, dstVnode, EdgeType.UNDIRECTED);
			}
			else {
				//update
				if(tmpvl instanceof VirtualInterLink){
					VirtualInterLink tmpvil = (VirtualInterLink) tmpvl;
					newBwDem = new BandwidthDemand(tmpvil.getOrigLink());
				}
				else{
					newBwDem = new BandwidthDemand(tmpvl);						
				}
				newBwDem.setDemandedBandwidth(bwDem.getDemandedBandwidth()*flow);
				for(AbstractResource absRes : tmpsl){
					if(absRes instanceof BandwidthResource){
						tmpbd = (BandwidthResource) absRes;
					}
				}
				if(!NodeLinkAssignation.vlmSingleLinkSimple(newBwDem, tmpsl)){
					throw new AssertionError("But we checked before!");
				}
				else{
					mapping.put(newBwDem, tmpbd);
					this.linkToMap.remove(tmpvl);
				}
			}
		}
	}
}
