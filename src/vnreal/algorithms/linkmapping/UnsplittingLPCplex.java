package vnreal.algorithms.linkmapping;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

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
		// TODO Auto-generated method stub
		return false;
	}
	
	public void generateFile(VirtualNetwork vNet,Map<VirtualNode, SubstrateNode> nodeMapping) throws IOException{
		BandwidthDemand originalBwDem = null, newBwDem;
		BandwidthResource bwResource=null;
		SubstrateNode srcSnode = null, dstSnode = null, ssnode=null, dsnode=null;
		SubstrateLink tSLink;

		String preambule = "\\Problem : vne\n";
		String obj = "Minimize\n"+"obj : ";
		String constraint = "Subject To\n";
		String bounds = "Bounds\n";
		

		for (Iterator<VirtualLink> links = vNet.getEdges().iterator(); links.hasNext();) {
			VirtualLink tmpl = links.next();
			mappedLinks++; // increase number of processed.

			// Find their mapped SubstrateNodes
			srcSnode = nodeMapping.get(vNet.getSource(tmpl));
			dstSnode = nodeMapping.get(vNet.getDest(tmpl));
			
			if (!srcSnode.equals(dstSnode)) {
				// Get current VirtualLink demand
				for (AbstractDemand dem : tmpl) {
					if (dem instanceof BandwidthDemand) {
						originalBwDem = (BandwidthDemand) dem;
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
					obj = obj + originalBwDem.getDemandedBandwidth()/bwResource.getAvailableBandwidth();
					obj = obj + " n"+srcSnode.getId()+"n"+dstSnode.getId()+"n"+ssnode.getId()+"n"+dsnode.getId()+" + ";
					
					

					
				}
				
				//source and destination constraints
				ArrayList<SubstrateNode> nextHop = sNet.getNextHop(srcSnode);
				for(int i=0;i<nextHop.size();i++){
					constraint=constraint+" n"+srcSnode.getId()+"n"+dstSnode.getId()+"n"+srcSnode.getId()+"n"+nextHop.get(i).getId()+" + ";
				}
				constraint =constraint+"0 = 1\n";
				
				ArrayList<SubstrateNode> lastHop = sNet.getLastHop(dstSnode);
				for(int i=0;i<lastHop.size();i++){
					constraint=constraint+" n"+srcSnode.getId()+"n"+dstSnode.getId()+"n"+lastHop.get(i).getId()+"n"+dstSnode.getId()+" + ";
				}
				constraint =constraint+"0 = 1\n";
				
				//middle node constraints
				for(Iterator<SubstrateNode> iterator = sNet.getVertices().iterator();iterator.hasNext();){
					SubstrateNode snode = iterator.next();
					if((!snode.equals(srcSnode))&&(!snode.equals(dstSnode))){
						
					}
						
				}
			}
			
		}
		
		
		
		obj = obj+ "0\n";
		BufferedWriter writer = new BufferedWriter(new FileWriter("ILP-LP-Models/CPLEXvne.lp"));
		writer.write(preambule+obj+constraint+bounds+"END");
		writer.close();
		
	}
}
