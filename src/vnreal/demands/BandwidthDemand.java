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
package vnreal.demands;

import vnreal.ExchangeParameter;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.ILinkConstraint;
import vnreal.network.Link;
import vnreal.network.NetworkEntity;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;
import vnreal.resources.ResourceVisitorAdapter;

/**
 * A demand for bandwidth in Mbit/s.
 * 
 * N.b.: This demand is applicable for links only.
 * 
 * @author Michael Duelli
 * @since 2010-09-10
 */
public final class BandwidthDemand extends AbstractDemand implements
		ILinkConstraint {
	private double demandedBandwidth;

	public BandwidthDemand(Link<? extends AbstractConstraint> owner) {
		super(owner);
	}
	
	

	public BandwidthDemand(NetworkEntity<? extends AbstractConstraint> ne, String name) {
		super(ne, name);
	}



	@ExchangeParameter
	public void setDemandedBandwidth(Double bandwidth) {
		this.demandedBandwidth = MiscelFunctions.roundThreeDecimals(bandwidth);
	}

	@ExchangeParameter
	public Double getDemandedBandwidth() {
		return demandedBandwidth;
	}

	@Override
	protected ResourceVisitorAdapter createAcceptsVisitor() {
		return new ResourceVisitorAdapter() {
			@Override
			public boolean visit(BandwidthResource res) {
				return true;
			}
		};
	}

	@Override
	protected ResourceVisitorAdapter createFulfillsVisitor() {
		return new ResourceVisitorAdapter() {
			@Override
			public boolean visit(BandwidthResource res) {
				if(MiscelFunctions.roundThreeDecimals(getDemandedBandwidth()+res.getOccupiedBandwidth())>res.getBandwidth())
				{
					System.out.println(getDemandedBandwidth());
					System.out.println(res.getOccupiedBandwidth());
					System.out.println(res.getAvailableBandwidth());
					System.out.println(res.getBandwidth());
					System.out.println(res.getOwner());
					System.out.println(BandwidthDemand.this.getOwner());	//to get outer class, use OuterClass.this
					System.out.println("pause");
					
					if(getDemandedBandwidth()-res.getAvailableBandwidth()<=0.001){
						BandwidthDemand.this.demandedBandwidth = BandwidthDemand.this.demandedBandwidth - 0.001;
						return true;
					}
					return false;
				}
				return true;
			}
		};
	}

	@Override
	public boolean occupy(AbstractResource res) {
		return res.getOccupyVisitor().visit(this);
	}

	@Override
	public boolean free(AbstractResource res) {
		return res.getFreeVisitor().visit(this);
	}

	@Override
	public String toString() {
		return new String("BandwidthDemand: demanded bandwidth="
				+ getDemandedBandwidth() + " Mbit/s");
	}

	@Override
	public AbstractDemand getCopy(NetworkEntity<? extends AbstractDemand> owner) {
		
		BandwidthDemand clone = new BandwidthDemand((Link<? extends AbstractConstraint>) owner, this.getName());
		clone.demandedBandwidth = this.demandedBandwidth;

		return clone;
	}
}
