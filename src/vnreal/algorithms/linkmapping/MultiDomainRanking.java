package vnreal.algorithms.linkmapping;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.util.EdgeType;
import li.multiDomain.Domain;
import li.multiDomain.LinkStressComparator;
import vnreal.algorithms.AbstractMultiDomainLinkMapping;
import vnreal.algorithms.utils.MiscelFunctions;
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
import vnreal.resources.CostResource;

/**
 * compare
 * @author shuopeng
 *
 */
public class MultiDomainRanking extends AbstractMultiDomainLinkMapping {
	
	Map<Domain, VirtualNetwork> localVNets;
	Map<Domain, AugmentedNetwork> augmentedNets;
	Map<BandwidthDemand, BandwidthResource> mapping;
	List<VirtualLink> linkToMap;
	
	public MultiDomainRanking(List<Domain> domains) {
		super(domains);
		this.initialize();
	}
	public MultiDomainRanking(List<Domain> domains,String localPath, String remotePath){
		super(domains,localPath, remotePath);
		this.initialize();
	}
	
	public Map<BandwidthDemand, BandwidthResource> getMapping() {
		return mapping;
	}
	
	private void initialize(){
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
//		Collections.sort(this.domains,new LinkStressComparator());
//		sortDomain();
		
		for(Domain domain : this.domains){
			this.createLocalVNet(domain, vNet, nodeMapping);

			//if there is no virtual links in this domain
			if(this.localVNets.get(domain).getEdgeCount()!=0){
				this.fulfillAugmentedNet(domain, vNet, nodeMapping);
//				this.localPath="tmp/MultiDomainRanking-"+vNet.getId()+"-"+domain.getId()+".lp";	// print mcf to file TODO
//				Map<String, String> solution = this.linkMappingWithoutUpdate(this.localVNets.get(domain), nodeMapping, this.augmentedNets.get(domain));
				Map<String, String> solution = this.linkMappingWithoutUpdateLocal(this.localVNets.get(domain), nodeMapping, this.augmentedNets.get(domain));
				
				if(solution.size()==0){
					System.out.println("link no solution");
					for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
						NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
					}
					//delete resources already distributed !!!
					for(Map.Entry<BandwidthDemand, BandwidthResource>e : mapping.entrySet()){
						e.getKey().free(e.getValue());
					}
					System.out.println(domain);
					
					return false;
				}
				this.updateResource(solution, domain, nodeMapping);
			}
			
		}
		
		return true;
	}
	
	private void sortDomain(){
		Collections.sort(this.domains, new Comparator<Domain>(){
			public int compare(Domain arg0, Domain arg1) {
				return -1;
			}
			/*
			@Override
			public int compare(Domain arg0, Domain arg1) {
				Random random = new Random();
				double dob = random.nextDouble();
				if(dob>0.5){
					return 1;
				}
				else return -1;
			}*/
			
		});
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
				SubstrateNode dijkDest = null, dijkSource = null;
				VirtualInterLink vil = (VirtualInterLink) vl;
				VirtualNode vnode1 = vil.getNode1();
				VirtualNode vnode2 = vil.getNode2();
				
				if(vnode1.getDomain().equals(domain)){}
				else if(vnode2.getDomain().equals(domain)){
					VirtualNode tmpnode = vnode2;
					vnode2 = vnode1;
					vnode1 = tmpnode;
				}
				else	continue;
				
				Domain exterDomain = vnode2.getDomain();
				dijkDest = nodeMapping.get(vnode2);;
				
				for(InterLink ilink : domain.getInterLink()){
					if(domain.containsVertex(ilink.getNode1()))
						dijkSource = ilink.getNode2();
					else if(domain.containsVertex(ilink.getNode2()))
						dijkSource = ilink.getNode1();
					
					if(exterDomain.containsVertex(dijkSource)&&
							(!dijkSource.equals(dijkDest))			//mapped substrate node is the border node
							&&(!an.existLink(dijkSource, dijkDest,vnode2))  //augmented link does not exist in the augmented network TODO
							){		
						
						AugmentedLink al = new AugmentedLink(vnode2);
						CostResource cost = new CostResource(al);	//cost = sum(1/Rbw)
						cost.setCost(exterDomain.cumulatedBWCost2(dijkSource, dijkDest));
						al.add(cost);
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
	
	public Map<String,String> linkMappingWithoutUpdateLocal(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping, AugmentedNetwork an) {
		Map<String, String> solution = new HashMap<String, String>();
		//generate .lp file
		try {
			this.generateFile(vNet, nodeMapping,an);
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
	
	private void generateFile(VirtualNetwork vNet,Map<VirtualNode, SubstrateNode> nodeMapping, AugmentedNetwork an) throws IOException{
		
		Domain tmpDomain = (Domain) an.getRoot(); 
		BandwidthDemand bwDem = null;
		BandwidthResource bwResource=null;
		CostResource cost = null;
		SubstrateNode ssnode=null, dsnode=null;
		VirtualNode srcVnode = null, dstVnode = null, vlDestination=null;
		int k=1;

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
			bwDem=tmpl.getBandwidthDemand();
			
			//virtual intra link and augmented virtual link
			if(!(tmpl instanceof VirtualInterLink)){
				//substrate links contains domain intra links
				for (Iterator<SubstrateLink> slink = tmpDomain.getEdges().iterator();slink.hasNext();){
					SubstrateLink tmpsl = slink.next();
					ssnode = an.getEndpoints(tmpsl).getFirst();
					dsnode = an.getEndpoints(tmpsl).getSecond();
					bwResource=tmpsl.getBandwidthResource();
					
					//objective
					if(tmpsl instanceof InterLink)	k=10;
					else k=1;
					obj = obj + " + "+MiscelFunctions.roundToDecimals(bwDem.getDemandedBandwidth()/(bwResource.getAvailableBandwidth()+0.001)*k,4);
//					obj = obj + " + "+bwDem.getDemandedBandwidth()*k;
					obj = obj + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId();
					obj = obj + " + "+MiscelFunctions.roundToDecimals(bwDem.getDemandedBandwidth()/(bwResource.getAvailableBandwidth()+0.001)*k,4);
//					obj = obj + " + "+bwDem.getDemandedBandwidth()*k;
					obj = obj + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId();
					//bounds
					bounds = bounds + "0 <= vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+" <= 1\n";
					bounds = bounds + "0 <= vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId()+" <= 1\n";
				}
				
				//flow constraints
				Collection<SubstrateNode> nextHop = new ArrayList<SubstrateNode>();
				for(Iterator<SubstrateNode> iterator = tmpDomain.getVertices().iterator();iterator.hasNext();){
					SubstrateNode snode = iterator.next();
					nextHop = tmpDomain.getNeighbors(snode);
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
			else{	//inter virtual links
				if(tmpDomain.containsVertex(nodeMapping.get(srcVnode))){
					vlDestination = dstVnode;
				}else if(tmpDomain.containsVertex(nodeMapping.get(dstVnode))){
					vlDestination = srcVnode;
				}
				
				for (Iterator<SubstrateLink> slink = an.getEdges().iterator();slink.hasNext();){
					SubstrateLink tmpsl = slink.next();
					ssnode = an.getEndpoints(tmpsl).getFirst();
					dsnode = an.getEndpoints(tmpsl).getSecond();
					//intra substrate links and inter substrate links
					if(!(tmpsl instanceof AugmentedLink)){
						bwResource = tmpsl.getBandwidthResource();
						//objective
						if(tmpsl instanceof InterLink)	k=1;
						else k=1;
						obj = obj + " + "+MiscelFunctions.roundToDecimals(bwDem.getDemandedBandwidth()/(bwResource.getAvailableBandwidth()+0.001)*k,4);
//						obj = obj + " + "+bwDem.getDemandedBandwidth()*k;
						obj = obj + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId();
						obj = obj + " + "+MiscelFunctions.roundToDecimals(bwDem.getDemandedBandwidth()/(bwResource.getAvailableBandwidth()+0.001)*k,4);
//						obj = obj + " + "+bwDem.getDemandedBandwidth()*k;
						obj = obj + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId();
						//bounds
						bounds = bounds + "0 <= vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+" <= 1\n";
						bounds = bounds + "0 <= vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId()+" <= 1\n";
					}
					else{
						AugmentedLink tmpal = (AugmentedLink)tmpsl;
						//augmented substrate links correspond to virtual inter links which means node mapping of srcVnode and dstVnode 
						if(tmpal.getDestNode().equals(vlDestination)){
							for(AbstractResource asrc : tmpsl){
								if(asrc instanceof CostResource){
									cost = (CostResource) asrc;
									break;
								}
							}
							//objective
							obj = obj + " + "+bwDem.getDemandedBandwidth()*cost.getCost();
							obj = obj + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId();
							obj = obj + " + "+bwDem.getDemandedBandwidth()*cost.getCost();
							obj = obj + " vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId();
							//bounds
							bounds = bounds + "0 <= vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+" <= 1\n";
							bounds = bounds + "0 <= vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId()+" <= 1\n";
						}
					}
				}
				
				//flow constraints
				Collection<SubstrateNode> nextHop = new ArrayList<SubstrateNode>();
				for(Iterator<SubstrateNode> iterator = an.getVertices().iterator();iterator.hasNext();){
					SubstrateNode snode = iterator.next();
					nextHop = an.getNeighbors(snode);
					boolean flag = false;
					for(Iterator<SubstrateNode> it=nextHop.iterator();it.hasNext();){
						//TODO
						SubstrateNode tmmpsn = it.next();
						if((!tmpDomain.containsVertex(snode))&&
								(!tmpDomain.containsVertex(tmmpsn))&&
								(!an.existLink(snode, tmmpsn, vlDestination)))
									continue;
						
						constraint=constraint+" + vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+snode.getId()+"sd"+tmmpsn.getId();
						constraint=constraint+" - vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+tmmpsn.getId()+"sd"+snode.getId();
						flag = true;
					}
					if(snode.equals(nodeMapping.get(srcVnode)))	constraint =constraint+" = 1\n";
					else if(snode.equals(nodeMapping.get(dstVnode))) constraint =constraint+" = -1\n";
					else if(flag)	constraint =constraint+" = 0\n";
				}
				
			}
			
			
		}

		
		//capacity constraint are only for the links of original domain, but not augmented network
		//intra link
		for (Iterator<SubstrateLink> slink = tmpDomain.getEdges().iterator();slink.hasNext();){
			SubstrateLink tmpsl = slink.next();
			ssnode = tmpDomain.getEndpoints(tmpsl).getFirst();
			dsnode = tmpDomain.getEndpoints(tmpsl).getSecond();
			
			for(AbstractResource asrc : tmpsl){
				if(asrc instanceof BandwidthResource){
					bwResource = (BandwidthResource) asrc;
					break;
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
			double bdValue=MiscelFunctions.roundThreeDecimals(bwResource.getAvailableBandwidth()-0.1);
			if(bdValue<=0.1) bdValue=0;
			constraint = constraint +" <= " + bdValue+"\n";
		}
		//inter link
		for (Iterator<InterLink> slink = tmpDomain.getInterLink().iterator();slink.hasNext();){
			InterLink tmpsl = slink.next();
			ssnode = ((InterLink) tmpsl).getNode1();
			dsnode = ((InterLink) tmpsl).getNode2();
			
			for(AbstractResource asrc : tmpsl){
				if(asrc instanceof BandwidthResource){
					bwResource = (BandwidthResource) asrc;
					break;
				}
			}
			boolean flag = false;
			for (Iterator<VirtualLink> links = vNet.getEdges().iterator(); links.hasNext();) {
				VirtualLink tmpl = links.next();
				if(tmpl instanceof VirtualInterLink){
					flag = true;
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
			}
			if(flag){
				double bdValue=MiscelFunctions.roundThreeDecimals(bwResource.getAvailableBandwidth()-0.1);
				if(bdValue<=0.1) bdValue=0;
				constraint = constraint +" <= " + bdValue+"\n";
			}
		}
		
		obj = obj+ "\n";
		BufferedWriter writer = new BufferedWriter(new FileWriter(localPath));
		writer.write(preambule+obj+constraint+bounds+general+"END");
		writer.close();
		
	}
	
	private void updateResource(Map<String, String> solution, Domain domain, Map<VirtualNode, SubstrateNode> nodeMapping){
		VirtualNetwork tmpvn = this.localVNets.get(domain);
		AugmentedNetwork an = this.augmentedNets.get(domain);
		LinkedHashMap<BandwidthDemand, SubstrateLink> assignation = new LinkedHashMap<BandwidthDemand, SubstrateLink>();
		
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
				this.linkToMap.remove(tmpvil.getOrigLink());	//remove virtual link which is the original link of virtual inter link
			}
			else {
				//update
				boolean flag=true;
				VirtualLink originalLink = tmpvl ; 
				if((tmpvl instanceof VirtualInterLink)){
					originalLink = ((VirtualInterLink) tmpvl).getOrigLink();
				}
				else if((tmpvl instanceof AugmentedVirtualLink)){
					originalLink = ((AugmentedVirtualLink) tmpvl).getOriginalVL();
				}
				
				for(Map.Entry<BandwidthDemand, SubstrateLink> e : assignation.entrySet()){
					if(e.getValue().equals(tmpsl)&&e.getKey().getOwner().equals(originalLink)){	//this virtual link demand already exists
						e.getKey().setDemandedBandwidth(e.getKey().getDemandedBandwidth()+bwDem.getDemandedBandwidth()*flow);
						flag = false;
						break;
					}
				}
				if(flag){
					newBwDem = new BandwidthDemand(originalLink);
					newBwDem.setDemandedBandwidth(bwDem.getDemandedBandwidth()*flow);
					assignation.put(newBwDem, tmpsl);
				}
				
			}
		}
		//update
		for(Map.Entry<BandwidthDemand, SubstrateLink> entry : assignation.entrySet()){
			BandwidthDemand newBwDem = entry.getKey();
			SubstrateLink tmpsl = entry.getValue();
			BandwidthResource tmpbd = null;
			
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
				this.linkToMap.remove(newBwDem.getOwner());
			}
		}
		
	}
}
