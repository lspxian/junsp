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

import java.util.List;
import java.util.Random;
import java.util.SortedSet;

import protectionProba.MaxFlowPath;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.network.Link;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;

/**
 * A substrate network link class.
 * 
 * @author Michael Duelli
 * @author Vlad Singeorzan
 */
public class SubstrateLink extends Link<AbstractResource> implements Comparable<SubstrateLink>{

	protected double probability;
	protected boolean used;
	protected List<List<SubstrateLink>> ksp;
	protected SortedSet<MaxFlowPath> maxflow;
	
	public SubstrateLink() {
		super();
		//failure probability 
//		double random = new Random().nextGaussian();
//		probability = random*0.25e-5+1e-5;
		double random = new Random().nextDouble();
		probability = 0e-6+random*20e-6;
//		probability = 0.00001;
		used=false;
	}
	
	public SubstrateLink(Double failure){
		super();
		this.probability=failure;
	}

	public SubstrateLink getCopy() {
		SubstrateLink clone = new SubstrateLink();
		clone.setName(getName());

		for (AbstractResource r : this) {
			clone.add(r.getCopy(clone));
		}

		return clone;
	}

	@Override
	public String toString() {
		return "SubstrateLink(" + getId() + ")";
	}

	@Override
	public String toStringShort() {
		return "SL(" + getId() + ")";
	}
	
	public boolean addResource(double random){
		BandwidthResource bw=new BandwidthResource(this);
		bw.setBandwidth(MiscelFunctions.roundThreeDecimals(100+random*50));
		this.add(bw);
		return true;
	}

	@Override
	public int compareTo(SubstrateLink o) {
		if(this.getId()>o.getId())	return 1;
		else if(this.getId()<o.getId())	return -1;
		else return 0;
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}

	public BandwidthResource getBandwidthResource(){
		BandwidthResource bwres = null;
		for(AbstractResource abs : this.get()){
			if(abs instanceof BandwidthResource){
				bwres = (BandwidthResource) abs;
				break;
			}
		}
		return bwres;
	}

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}

	public List<List<SubstrateLink>> getKsp() {
		return ksp;
	}

	public void setKsp(List<List<SubstrateLink>> ksp) {
		this.ksp = ksp;
	}

}
