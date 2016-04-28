/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2010-2011, The VNREAL Project Team.
 * 
 * This work has been funded by the European FP7
 * Network of Excellence "Euro-NF" (grant agreement no. 216366)
 * through the Specific Joint Developments and Experiments Project
 * "Virtual Network Resource Embedding Algorithms" (VNREAL). 
 *
 * The VNREAL Project Team consists of members from:
 * - University of Wuerzburg, Germany
 * - Universitat Politecnica de Catalunya, Spain
 * - University of Passau, Germany
 * See the file AUTHORS for details and contact information.
 * 
 * This file is part of ALEVIN (ALgorithms for Embedding VIrtual Networks).
 *
 * ALEVIN is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License Version 3 or later
 * (the "GPL"), or the GNU Lesser General Public License Version 3 or later
 * (the "LGPL") as published by the Free Software Foundation.
 *
 * ALEVIN is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * or the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License and
 * GNU Lesser General Public License along with ALEVIN; see the file
 * COPYING. If not, see <http://www.gnu.org/licenses/>.
 *
 * ***** END LICENSE BLOCK ***** */
package vnreal.network.virtual;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.collections15.Factory;

import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.demands.CpuDemand;
import vnreal.mapping.Mapping;
import vnreal.network.Network;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;
import vnreal.resources.CpuResource;
import edu.uci.ics.jung.graph.util.Pair;
import li.multiDomain.Domain;

/**
 * A virtual network built upon the physical substrate.
 * 
 * @author Michael Duelli
 */
@SuppressWarnings("serial")
public final class VirtualNetwork extends
		Network<AbstractDemand, VirtualNode, VirtualLink> {
	/** The layer resp. virtual network id which start from 0. */
	private final int layer;
	private String name = null;
	private double lifetime=0.0;	//To know the lifetime of a Vn in the Substrate Network
	private double mu=1000;			//The mean of the lifetime
	
	public VirtualNetwork(int layer, boolean autoUnregisterConstraints, boolean directed){
		super(autoUnregisterConstraints, directed);
		this.layer = layer;
	}
	
	public VirtualNetwork(int layer, boolean autoUnregisterConstraints) {
		super(autoUnregisterConstraints);
		this.layer = layer;
	}

	public VirtualNetwork(int layer) {
		this(layer, false,false);
	}
	
	public VirtualNetwork(){
		super(false, false);
		this.layer = 1;
	}

	@Override
	public boolean addVertex(VirtualNode vertex) {
			return super.addVertex(vertex);
	}

	@Override
	public boolean addEdge(VirtualLink edge, VirtualNode v, VirtualNode w) {
		return super.addEdge(edge, new Pair<VirtualNode>(v, w));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getLabel() {
		return "Virtual Network (" + layer + ")";
	}

	@Override
	public int getLayer() {
		return layer;
	}

	@Override
	public Factory<VirtualLink> getEdgeFactory() {
		return new Factory<VirtualLink>() {
			@Override
			public VirtualLink create() {
				return new VirtualLink();
			}
		};
	}

	@Override
	public String toString() {
		String result = "NODES:\n";
		for (VirtualNode n : getVertices()) {
			result += n + "("+n.getCoordinateX()+","+n.getCoordinateY()+")"+"\n";
			for (AbstractDemand d : n.get()) {
				result += "  " + d.toString() + "\n";
			}
		}

		result += "\nEDGES:\n";
		for (VirtualLink l : getEdges()) {
			Pair<VirtualNode> pair = getEndpoints(l);
			result += l + "  (" + pair.getFirst().getId() + "<->"
					+ pair.getSecond().getId() + ")\n";
			for (AbstractDemand d : l.get()) {
				result += "  " + d.toString() + "\n";
			}
		}

		return result;
	}

	@Override
	public VirtualNetwork getInstance(boolean autoUnregister) {
		return new VirtualNetwork(getLayer(), autoUnregister);
	}

	@Override
	public VirtualNetwork getCopy(boolean autoUnregister) {
		VirtualNetwork copyVNetwork = new VirtualNetwork(getLayer(),
				autoUnregister);

		LinkedList<VirtualLink> originalLinks = new LinkedList<VirtualLink>(
				getEdges());
		VirtualNode tmpSNode, tmpDNode;
		VirtualLink tmpSLink;
		for (Iterator<VirtualNode> tempSubsNode = getVertices().iterator(); tempSubsNode
				.hasNext();) {
			tmpSNode = tempSubsNode.next();
			copyVNetwork.addVertex(tmpSNode);
		}
		for (Iterator<VirtualLink> tempItSubLink = originalLinks.iterator(); tempItSubLink
				.hasNext();) {
			tmpSLink = tempItSubLink.next();
			tmpSNode = this.getEndpoints(tmpSLink).getFirst();
			tmpDNode = this.getEndpoints(tmpSLink).getSecond();
			copyVNetwork.addEdge(tmpSLink, tmpSNode, tmpDNode);
		}

		return copyVNetwork;
	}
	
	
	public void generateDuplicateEdges() {
		Collection<VirtualLink> edges = new LinkedList<VirtualLink>(getEdges());
		for (VirtualLink edge : edges) {
			Pair<VirtualNode> nodes = getEndpoints(edge);

			boolean done = false;
			for (VirtualLink e : getOutEdges(nodes.getSecond())) {
				if (getEndpoints(e).getSecond() == nodes.getFirst()) {
					done = true;
					e.removeAll();
					for (AbstractDemand r : edge) {
						e.add(r.getCopy(e));
					}
					e.setName(edge.getName() + "_dup");
				}
			}

			if (!done) {
				VirtualLink newEdge = edge.getCopy();
				newEdge.setName(edge.getName() + "_dup");
				addEdge(newEdge, nodes.getSecond(), nodes.getFirst());
			}
		}
	}

	@Override
	public void alt2network(String filePath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String line=null;

		boolean node=false, edge=false;
		while((line=br.readLine())!=null){
			if(line.contains("VERTICES"))
				node=true;
			else if(line.contains("EDGES")){
				node=false;
				edge=true;
			}
			if((node==true)&&(!line.contains("VERTICES"))&&(!line.isEmpty())){
				VirtualNode nd=new VirtualNode(); //layer, don't know what it means, 1 for all
				line = line.substring(line.indexOf(" ")+1);
				line = line.substring(line.indexOf(" ")+1);

				nd.setCoordinateX(Double.parseDouble(line.substring(0, line.indexOf(" "))));
				nd.setCoordinateY(Double.parseDouble(line.substring(line.indexOf(" ")+1)));
				this.addVertex(nd);
			
				
			}
			if((edge==true)&&(!line.contains("EDGES"))&&(!line.isEmpty())){
				//System.out.println(line);
				Object[] arr = this.getVertices().toArray();
				VirtualNode start =  (VirtualNode)arr[Integer.parseInt(line.substring(0, line.indexOf(" ")))];
				line = line.substring(line.indexOf(" ")+1);
				VirtualNode end = (VirtualNode)arr[Integer.parseInt(line.substring(0, line.indexOf(" ")))];
				this.addEdge(new VirtualLink(), start, end);
				
				//double direction
				//this.addEdge(new VirtualLink(layer), end, start);
				
				
			}
		}
		br.close();
		
	}
	
	public boolean addAllResource(boolean random){
		for(VirtualNode sbnd : this.getVertices()){
			sbnd.addResource(random);
		}
		Iterator<VirtualLink> it=this.getEdges().iterator();
		while(it.hasNext()){
			double value = 1;
			if(random)	value = new Random().nextDouble();
			VirtualLink sblk1 = (VirtualLink)it.next();
			//VirtualLink sblk2 = (VirtualLink)it.next();
			sblk1.addResource(value);
			//sblk2.addResource(value);
		}
		lifetime = MiscelFunctions.negExponential(1.0/mu);
		return true;
	}
	
	public void reconfigResource(List<Domain> multiDomain){
		for(Domain domain : multiDomain){
			for(VirtualNode vnode : this.getVertices()){
				if(domain.getCoordinateX()<=vnode.getCoordinateX()/100.0&&
						vnode.getCoordinateX()/100.0<domain.getCoordinateX()+1&&
						domain.getCoordinateY()<=vnode.getCoordinateY()/100.0&&
						vnode.getCoordinateY()/100.0<domain.getCoordinateY()+1){
					vnode.setDomain(domain); 	//the virtual node belong to a domain
				}
			}
		}
		for(VirtualLink vl : this.getEdges()){
			VirtualNode vSource = this.getEndpoints(vl).getFirst();
			VirtualNode vDest = this.getEndpoints(vl).getSecond();
			if(vSource.getDomain().equals(vDest.getDomain()))
				vl.setBW();
		}
	}
	
	public VirtualNode getNodeFromID(int id){
		for(VirtualNode vtnd : this.getVertices()){
			if(vtnd.getId()==id)
				return vtnd;
		}
		return null;
	}
	/*Added */
	public double getLifetime()
	{
		return lifetime;
	}
	public void setLifetim(double mean)
	{
		this.lifetime = MiscelFunctions.negExponential(1.0/mean);
	}
	
	//scale coordinate
	public void scale(double x, double y){
		for(VirtualNode vnode : this.getVertices()){
			vnode.setCoordinateX(vnode.getCoordinateX()*x);
			vnode.setCoordinateY(vnode.getCoordinateY()*y);
		}
	}
	
	
	public double getTotalCost(List<Domain> domains){
		CpuDemand tmpCpuDem;
		BandwidthDemand tmpBwDem,currentBW;
		double nodeCost=0.0, linkCost=0.0;
		
		ArrayList<SubstrateLink> allLinks = new ArrayList<SubstrateLink>();
		for(Domain d : domains){
			for(SubstrateLink link: d.getAllLinks()){
				if(!allLinks.contains(link))
					allLinks.add(link);
			}
		}
		
		for(VirtualNode vnode : this.getVertices()){
			for(AbstractDemand ad : vnode){
				if(ad instanceof CpuDemand){
					tmpCpuDem = (CpuDemand)ad;
					nodeCost += tmpCpuDem.getDemandedCycles();
					break;
				}
			}
		}
		
		for(VirtualLink vl : this.getEdges()){
			for(AbstractDemand ad : vl){
				if(ad instanceof BandwidthDemand){
					tmpBwDem = (BandwidthDemand)ad;
					for (SubstrateLink sl : allLinks) {
						for (AbstractResource res : sl) {
							if (res instanceof BandwidthResource) {
								for (Mapping f : res.getMappings()) {
									currentBW = (BandwidthDemand) f.getDemand();
									if(tmpBwDem.getOwner().equals(currentBW.getOwner())){
										linkCost += currentBW.getDemandedBandwidth();
										break;
									}
								}
								break;
							}
						}
					}
					
					break;
				}
			}
		}
		return (nodeCost + linkCost);
	}
	
	public double getTotalCost(SubstrateNetwork sn){
		CpuDemand tmpCpuDem;
		BandwidthDemand tmpBwDem,currentBW;
		double nodeCost=0.0, linkCost=0.0;
		
		for(VirtualNode vnode : this.getVertices()){
			for(AbstractDemand ad : vnode){
				if(ad instanceof CpuDemand){
					tmpCpuDem = (CpuDemand)ad;
					nodeCost += tmpCpuDem.getDemandedCycles();
					break;
				}
			}
		}
		
		for(VirtualLink vl : this.getEdges()){
			for(AbstractDemand ad : vl){
				if(ad instanceof BandwidthDemand){
					tmpBwDem = (BandwidthDemand)ad;
					for (SubstrateLink sl : sn.getEdges()) {
						for (AbstractResource res : sl) {
							if (res instanceof BandwidthResource) {
								for (Mapping f : res.getMappings()) {
									currentBW = (BandwidthDemand) f.getDemand();
									if(tmpBwDem.getOwner().equals(currentBW.getOwner())){
										linkCost += currentBW.getDemandedBandwidth();
										break;
									}
								}
								break;
							}
						}
					}
					
					break;
				}
			}
		}
		return (nodeCost + linkCost);
		
	}
	
}
