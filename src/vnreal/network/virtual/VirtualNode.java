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

import java.util.Random;

import li.multiDomain.Domain;
import vnreal.demands.AbstractDemand;
import vnreal.network.Node;
import vnreal.demands.CpuDemand;

/**
 * A virtual network node class.
 * 
 * @author Michael Duelli
 * @author Vlad Singeorzan
 */
public class VirtualNode extends Node<AbstractDemand> {
	protected Domain domain;	//for multi domain

	
	public VirtualNode() {
		super();
		this.domain=null;
	}

	
	public VirtualNode(Domain domain){
		super();
		this.domain = domain;
	}

	@Override
	public String toString() {
		String result= "VirtualNode(" + getId() + ")";
		if(domain!=null)
			result += "@domain("+domain.getCoordinateX()+","+domain.getCoordinateY()+")";
		return result;
	}

	@Override
	public String toStringShort() {
		return "VN(" + getId() + ")";
	}
	
	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public VirtualNode getCopy() {
		VirtualNode clone = new VirtualNode(domain);
		clone.setName(getName());

		for (AbstractDemand r : this) {
			clone.add(r.getCopy(clone));
		}

		return clone;
	}
	/**
	 * 
	 * @param random
	 * @return
	 */
	public boolean addResource(boolean random){
		CpuDemand cpu = new CpuDemand(this);
		double quantity = 10;
		if(random)
			quantity = new Random().nextDouble()*20;
		cpu.setDemandedCycles(quantity);;
		if(this.preAddCheck(cpu)){
			this.add(cpu);
			return true;
		}
		return false;
	}
}
