package vnreal.algorithms.nodemapping;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import vnreal.algorithms.AbstractNodeMapping;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.demands.CpuDemand;
import vnreal.network.substrate.AugmentedNetwork;
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

	protected CordinatedNodeLinkMapping(SubstrateNetwork sNet, boolean subsNodeOverload) {
		super(sNet, subsNodeOverload);
	}
	
	//no overload by default
	protected CordinatedNodeLinkMapping(SubstrateNetwork sNet){
		super(sNet);
	}

	@Override
	public boolean nodeMapping(VirtualNetwork vNet) {
		
		
		
		
		return false;
	}
	
	
	//generate cplex file for undirected multi commodity flow
	/**
	 * 
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

				for(MetaNode metaNode : aNet.getMetaNodes()){
					for(AbstractDemand asrc2 : metaNode.getRoot()){
						if(asrc2 instanceof CpuDemand){
							cpuDem = (CpuDemand) asrc2;
							break;
						}
						
						//obj
						obj = obj + " + "+cpuDem.getDemandedCycles()/(cpuResource.getAvailableCycles()+0.001);
						obj = obj + " Xm"+metaNode.getId()+"w"+tmpNode.getId();
					}
				}
					
					
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
				

			
				//link
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

}
