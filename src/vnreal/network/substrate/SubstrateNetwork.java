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
package vnreal.network.substrate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import li.multiDomain.Domain;

import org.apache.commons.collections15.Factory;

import vnreal.network.Network;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;
import vnreal.resources.CpuResource;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * The physical substrate underlying all virtual networks.
 * 
 * @author Michael Duelli
 */
@SuppressWarnings("serial")
public class SubstrateNetwork extends
		Network<AbstractResource, SubstrateNode, SubstrateLink> {
	
	
	/**
	 * constructor used by filter.transform(graph)  
	 */
	
	public SubstrateNetwork(){	
		super(false);	//par defaut, undirected
	}
	
	public SubstrateNetwork(boolean directed){
		super(directed); //true : directed, false: undirected
	}
	
	
	public SubstrateNetwork(boolean autoUnregisterConstraints, boolean directed) {
		super(autoUnregisterConstraints, directed);
	}

	@Override
	public String getLabel() {
		return "Substrate Network";
	}

	@Override
	public int getLayer() {
		return 0;
	}

	@Override
	public Factory<SubstrateLink> getEdgeFactory() {
		return new Factory<SubstrateLink>() {
			@Override
			public SubstrateLink create() {
				return new SubstrateLink();
			}
		};
	}

	@Override
	public String toString() {
		String result = "NODES:\n";
		for (SubstrateNode n : getVertices()) {
			result += n + "("+n.getCoordinateX()+","+n.getCoordinateY()+")"+"\n";
			for (AbstractResource r : n.get()) {
				result += "  " + r.toString() + "\n";
			}
		}

		result += "\nEDGES:\n";
		for (SubstrateLink l : getEdges()) {
			Pair<SubstrateNode> pair = getEndpoints(l);
			result += l + "  (" + pair.getFirst().getId() + "<->"
					+ pair.getSecond().getId() + ") \n";
			for (AbstractResource r : l.get()) {
				result += "  " + r.toString() + "\n";
			}
		}

		return result;
	}
	
	public String probaToString(){
		String result = "NODES:\n";
		for (SubstrateNode n : getVertices()) {
			result += n + "("+n.getCoordinateX()+","+n.getCoordinateY()+")"+"\n";
//			for (AbstractResource r : n.get()) {
//				result += "  " + r.toString() + "\n";
//			}
		}
		result += "\nEDGES:\n";
		for (SubstrateLink l : getEdges()) {
			Pair<SubstrateNode> pair = getEndpoints(l);
			result += l + "  (" + pair.getFirst().getId() + "<->"
					+ pair.getSecond().getId() + ") \n";
			result += " failure probability: " + l.getProbability()+"\n";
//			for (AbstractResource r : l.get()) {
//				result += "  " + r.toString() + "\n";
//			}
		}

		return result;
	}

	@Override
	public SubstrateNetwork getInstance(boolean autoUnregister) {
		return new SubstrateNetwork(autoUnregister);
	}

	public SubstrateNetwork getCopy(){
		SubstrateNetwork result = new SubstrateNetwork();
		getCopy(false, result);
		return result;
	}
	
	@Override
	public SubstrateNetwork getCopy(boolean autoUnregister) {
		SubstrateNetwork result = new SubstrateNetwork(autoUnregister);
		getCopy(false, result);
		return result;
	}
	
	public void getCopy(SubstrateNetwork sNetwork) {
		getCopy(false, sNetwork);
	}


	public SubstrateNetwork getCopy(boolean autoUnregister, boolean deepCopy) {
		SubstrateNetwork result = new SubstrateNetwork(autoUnregister);
		getCopy(deepCopy, result);
		return result;
	}
	
	public void getCopy(boolean deepCopy, SubstrateNetwork result) {

		HashMap<String, SubstrateNode> map = new HashMap<String, SubstrateNode>();

		LinkedList<SubstrateLink> originalLinks = new LinkedList<SubstrateLink>(
				getEdges());
		SubstrateNode tmpSNode, tmpDNode;
		SubstrateLink tmpSLink;
		for (Iterator<SubstrateNode> tempSubsNode = getVertices().iterator(); tempSubsNode
				.hasNext();) {
			tmpSNode = tempSubsNode.next();
			if (deepCopy) {
				SubstrateNode clone = tmpSNode.getCopy();
				result.addVertex(clone);
				map.put(tmpSNode.getName(), clone);
			} else {
				result.addVertex(tmpSNode);
			}
		}
		for (Iterator<SubstrateLink> tempItSubLink = originalLinks.iterator(); tempItSubLink
				.hasNext();) {
			tmpSLink = tempItSubLink.next();
			
			tmpSNode = this.getEndpoints(tmpSLink).getFirst();
			tmpDNode = this.getEndpoints(tmpSLink).getSecond();

			if (deepCopy) {
				result.addEdge(tmpSLink.getCopy(),
						map.get(tmpSNode.getName()),
						map.get(tmpDNode.getName()));
			} else {
				result.addEdge(tmpSLink, tmpSNode, tmpDNode);
			}
		}
	}
	
	//Down cast
	public void copy(SubstrateNetwork sNet){	
		SubstrateNode tmpSNode, tmpDNode;
		SubstrateLink tmpSLink;
		for (Iterator<SubstrateLink> tempItSubLink = sNet.getEdges().iterator(); tempItSubLink
				.hasNext();) {
			tmpSLink = tempItSubLink.next();
			tmpSNode = sNet.getEndpoints(tmpSLink).getFirst();
			tmpDNode = sNet.getEndpoints(tmpSLink).getSecond();

			this.addEdge(tmpSLink, tmpSNode, tmpDNode);
		}
	}

	public void generateDuplicateEdges() {
		Collection<SubstrateLink> edges = new LinkedList<SubstrateLink>(getEdges());
		for (SubstrateLink edge : edges) {
			Pair<SubstrateNode> nodes = getEndpoints(edge);

			boolean done = false;
			for (SubstrateLink e : getOutEdges(nodes.getSecond())) {
				if (getEndpoints(e).getSecond() == nodes.getFirst()) {
					done = true;
					e.removeAll();
					for (AbstractResource r : edge) {
						e.add(r.getCopy(e));
					}
					e.setName(edge.getName() + "_dup");
				}
			}

			if (!done) {
				SubstrateLink newEdge = edge.getCopy();
				newEdge.setName(edge.getName() + "_dup");
				addEdge(newEdge, nodes.getSecond(), nodes.getFirst());
			}
		}
	}
	
	/*****************shuopeng*********************/
	
	public ArrayList<SubstrateNode> getHop(SubstrateNode n){
		ArrayList<SubstrateNode> hop = new ArrayList<SubstrateNode>();
		for(Iterator<SubstrateLink> link = this.getEdges().iterator();link.hasNext();){
			SubstrateLink slink = link.next();
			if(this.getEndpoints(slink).getFirst().equals(n))
				hop.add(this.getEndpoints(slink).getSecond());
			else if(this.getEndpoints(slink).getSecond().equals(n))
				hop.add(this.getEndpoints(slink).getFirst());
		}
		return hop;
	}

	/**
	 * convert .alt to network, the .alt is generated by it-gtm
	 * @param filePath
	 * @throws IOException 
	 * @override
	 */
	public void alt2network(String filePath) throws IOException{
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
				SubstrateNode nd=new SubstrateNode();
				line = line.substring(line.indexOf(" ")+1);
				line = line.substring(line.indexOf(" ")+1);

				nd.setCoordinateX(Double.parseDouble(line.substring(0, line.indexOf(" "))));
				nd.setCoordinateY(Double.parseDouble(line.substring(line.indexOf(" ")+1)));
				this.addVertex(nd);
				
			}
			if((edge==true)&&(!line.contains("EDGES"))&&(!line.isEmpty())){
				Object[] arr = this.getVertices().toArray();
				SubstrateNode start =  (SubstrateNode)arr[Integer.parseInt(line.substring(0, line.indexOf(" ")))];
				line = line.substring(line.indexOf(" ")+1);
				SubstrateNode end = (SubstrateNode)arr[Integer.parseInt(line.substring(0, line.indexOf(" ")))];
				this.addEdge(new SubstrateLink(), start, end);
				//double direction
				//this.addEdge(new SubstrateLink(), end, start);
			}
		}
		br.close();
	}
	
	public boolean addAllResource(boolean random){
		double value=1;
		for(SubstrateNode sbnd : this.getVertices()){
			if(random) value =  new Random().nextDouble();
			sbnd.addResource(value);
		}
		
		for(SubstrateLink sl:this.getEdges()){
			if(random)	value = new Random().nextDouble();
			sl.addResource(value);
		}
		return true;
	}
	
	public void addInfiniteResource(){
		for(SubstrateNode sbnd : this.getVertices()){
			CpuResource cpu = new CpuResource(sbnd);
			cpu.setCycles(100000.);
			sbnd.add(cpu);
		}
		for(SubstrateLink sl:this.getEdges()){
			BandwidthResource bw = new BandwidthResource(sl);
			bw.setBandwidth(100000.);
			sl.add(bw);
		}
		
	}
	
	public SubstrateNode getNodeFromID(int id){
		for(SubstrateNode sbnd : this.getVertices()){
			if(sbnd.getId()==id)
				return sbnd;
		}
		return null;
	}
	
	/*
	public List<Domain> divide4Domain(){
		List<Domain> multiNet = new ArrayList<Domain>();
		Domain dn1 = new Domain();
		Domain dn2 = new Domain();
		Domain dn3 = new Domain();
		Domain dn4 = new Domain();
		multiNet.add(dn1);
		multiNet.add(dn2);
		multiNet.add(dn3);
		multiNet.add(dn4);
		
		for(SubstrateLink sl : this.getEdges()){
			SubstrateNode source = this.getEndpoints(sl).getFirst();
			SubstrateNode dest = this.getEndpoints(sl).getSecond();
			double sx = source.getCoordinateX();
			double sy = source.getCoordinateY();
			double dx = dest.getCoordinateX();
			double dy = dest.getCoordinateY();
			if(sx<50&&sy<50){
				if(dx<50&&dy<50)	dn1.addEdge(sl, source, dest);
				else if(dx>=50&&dy<50)	dn1.addInterLink(sl, source, dest, dn2);
				else if(dx>=50&&dy>=50) dn1.addInterLink(sl, source, dest, dn3);
				else if(dx<50&&dy>=50) dn1.addInterLink(sl, source, dest, dn4);
			}
			else if(sx>=50&&sy<50){
				if(dx<50&&dy<50)	dn2.addInterLink(sl, source, dest, dn1);
				else if(dx>=50&&dy<50)	dn2.addEdge(sl, source, dest);
				else if(dx>=50&&dy>=50) dn2.addInterLink(sl, source, dest, dn3);
				else if(dx<50&&dy>=50) dn2.addInterLink(sl, source, dest, dn4);
			}
			else if(sx>=50&&sy>=50){
				if(dx<50&&dy<50)	dn3.addInterLink(sl, source, dest, dn1);
				else if(dx>=50&&dy<50)	dn3.addInterLink(sl, source, dest, dn2);
				else if(dx>=50&&dy>=50) dn3.addEdge(sl, source, dest);
				else if(dx<50&&dy>=50) dn3.addInterLink(sl, source, dest, dn4);
			}
			else{
				if(dx<50&&dy<50)	dn4.addInterLink(sl, source, dest, dn1);
				else if(dx>=50&&dy<50)	dn4.addInterLink(sl, source, dest, dn2);
				else if(dx>=50&&dy>=50) dn4.addInterLink(sl, source, dest, dn3);
				else if(dx<50&&dy>=50) dn4.addEdge(sl, source, dest);
			}
		}
		
		return multiNet;
		
	}*/
}
