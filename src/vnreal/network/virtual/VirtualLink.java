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

import java.util.HashMap;
import java.util.Map;
import li.multiDomain.Domain;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.constraints.ILinkConstraint;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.network.Link;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;

/**
 * A virtual network link class.
 * 
 * @author Michael Duelli
 * @author Vlad Singeorzan
 */
public class VirtualLink extends Link<AbstractDemand> {
	private Map<SubstrateNetwork, Map<SubstrateLink, Double>> solution;

	
	public VirtualLink() {
		super();
		this.solution = new HashMap<SubstrateNetwork, Map<SubstrateLink, Double>>();
		setName(getId() + "");
	}

	public Map<SubstrateNetwork, Map<SubstrateLink, Double>> getSolution() {
		return solution;
	}

	public void setSolution(Map<SubstrateNetwork, Map<SubstrateLink, Double>> solution) {
		this.solution = solution;
	}

	@Override
	public String toString() {
		return "VirtualLink(" + getId() + ")";
	}

	@Override
	public String toStringShort() {
		return "VL(" + getId() + ")";
	}

	@Override
	public boolean preAddCheck(AbstractDemand t) {
		// Only allow to add this type.
		// To implement multipath algorithms it is important that more than one
		// virtual link demand could be added to each virtual link (each demand
		// will be fulfilled by each path of the solution).
		if (t instanceof ILinkConstraint) {/*
											 * if
											 * (!this.containsConstraintType(t))
											 * return true; else {
											 * System.err.println
											 * ("Cannot add constraint " + t +
											 * " to link " + this +
											 * " because it already has a constraint of this type."
											 * ); return false; }
											 */
			return true;
		}

		else {
			System.err.println("Cannot add non-ILinkConstraint " + t.getClass().getSimpleName()
					+ " to link " + this);
			return false;
		}
	}

	public VirtualLink getCopy() {
		VirtualLink clone = new VirtualLink();
		
		for (AbstractDemand d : this) {
			clone.add(d.getCopy(clone));
		}
		 return clone;
	}
	
	public boolean addResource(double random){
		BandwidthDemand bw=new BandwidthDemand(this);
		double quantity = 10;
		if(random!=1.0)	
			quantity = 0+MiscelFunctions.roundThreeDecimals(random*20);
		bw.setDemandedBandwidth(quantity);
		this.add(bw);
		return true;
	}
	
	public void setBW(){
		for(AbstractDemand d : this){
			if(d instanceof BandwidthDemand)
				((BandwidthDemand) d).setDemandedBandwidth(((BandwidthDemand) d).getDemandedBandwidth()/10);
		}
	}
	
	public double getCost(SubstrateNetwork d){
		Double cost = 0.;
		if(solution.get(d).isEmpty()){
			return 10000;
		}
		else{
			for(Map.Entry<SubstrateLink, Double> entry : solution.get(d).entrySet())
				for(AbstractResource r : entry.getKey()){
					if(r instanceof BandwidthResource)
						cost += entry.getValue()/((BandwidthResource) r).getAvailableBandwidth();
					break;
				}
			return cost;
		}
			
	}
	
	public double getMinCostValue(){
		double cost = 10000;
		for(Map.Entry<SubstrateNetwork, Map<SubstrateLink, Double>> e : solution.entrySet()){
			double currentCost = this.getCost(e.getKey());
			if(currentCost<cost){
				cost = currentCost;
			}
		}
		return cost;
	}
	
	public Map<SubstrateLink, Double> getMinCostMap(){
		Map<SubstrateLink, Double> result = null;
		double cost = 10000;
		for(Map.Entry<SubstrateNetwork, Map<SubstrateLink, Double>> e : solution.entrySet()){
			double currentCost = this.getCost(e.getKey());
			if(currentCost<cost){
				cost = currentCost;
				result = e.getValue();
			}
		}
		return result;
	}
	
	public BandwidthDemand getBandwidthDemand(){
		BandwidthDemand bwdem = null;
		for(AbstractDemand abd : this.get()){
			if(abd instanceof BandwidthDemand){
				bwdem = (BandwidthDemand) abd;
				break;
			} 
		}
		return bwdem;
	}
}
