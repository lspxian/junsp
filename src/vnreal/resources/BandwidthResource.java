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
package vnreal.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import protectionProba.Risk;
import vnreal.ExchangeParameter;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.constraints.AbstractConstraint;
import vnreal.constraints.ILinkConstraint;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.demands.DemandVisitorAdapter;
import vnreal.mapping.Mapping;
import vnreal.network.Link;
import vnreal.network.NetworkEntity;
import vnreal.network.substrate.SubstrateLink;

/**
 * A resource for bandwidth in Mbit/s.
 * 
 * N.b.: This resource is applicable for links only.
 * 
 * @author Michael Duelli
 * @since 2010-09-10
 */
public final class BandwidthResource extends AbstractResource implements
		ILinkConstraint {
	private double bandwidth;	//total capacity
	private double primaryCap;	//primary capacity
	private double backupCap;	//backup capacity
	private double occupiedPrimary=0.0;		//occupied primary bw
	private double occupiedBandwidth = 0;	//total occupied bw 
	private double reservedBackupBw=0.0;	//backup occupied bw, used if no separation
	private List<Risk> risks=new ArrayList<Risk>();
	
	public BandwidthResource(Link<? extends AbstractConstraint> owner) {
		super(owner);
	}
	public BandwidthResource(Link<? extends AbstractConstraint> owner, String name) {
		super(owner, name);
	}
	
	public double getPrimaryCap() {
		return primaryCap;
	}
	public void setPrimaryCap(double primaryCap) {
		this.primaryCap = primaryCap;
	}
	public double getOccupiedPrimary() {
		return occupiedPrimary;
	}
	public void setOccupiedPrimary(double occupiedPrimary) {
		this.occupiedPrimary = occupiedPrimary;
	}
	public double getOccupiedBandwidth() {
		return occupiedBandwidth;
	}
	public void setOccupiedBandwidth(double occupiedBandwidth) {
		this.occupiedBandwidth = occupiedBandwidth;
	}
	public double getBackupCap() {
		return backupCap;
	}
	public void setBackupCap(double backupCap) {
		this.backupCap = backupCap;
	}
	
	public double getReservedBackupBw() {
		return reservedBackupBw;
	}
	public void setReservedBackupBw(double reservedBackupBw) {
		this.reservedBackupBw = reservedBackupBw;
	}
	@ExchangeParameter
	public void setBandwidth(Double bandwidth) {
		this.bandwidth = MiscelFunctions.roundThreeDecimals(bandwidth);
	}

	@ExchangeParameter
	public Double getBandwidth() {
		return this.bandwidth;
	}

	public Double getAvailableBandwidth() {				//primary available
		if(backupCap!=0)	return primaryCap-occupiedPrimary;	//separation 
		else	return bandwidth - occupiedBandwidth;			//not separated
	}
	
	public Double getBackupAvailable(SubstrateLink failure){
		if(backupCap!=0) return backupCap-getBackupOccupiedBW(failure);
		else	return bandwidth-occupiedPrimary-getBackupOccupiedBW(failure);
	}
	
	public double getBackupOccupiedBW(SubstrateLink sl){
		Risk risk=this.findRiskByLink(sl);
		if(risk!=null) return risk.getTotal();
		else return 0.0;
	}

	@Override
	public boolean accepts(AbstractDemand dem) {
		return dem.getAcceptsVisitor().visit(this);
	}

	@Override
	public boolean fulfills(AbstractDemand dem) {
		return dem.getFulfillsVisitor().visit(this);
	}

	@Override
	protected DemandVisitorAdapter createOccupyVisitor() {
		return new DemandVisitorAdapter() {
			@Override
			public boolean visit(BandwidthDemand dem) {
				if (fulfills(dem)) {
					occupiedPrimary += dem.getDemandedBandwidth();
					occupiedPrimary = MiscelFunctions.roundThreeDecimals(occupiedPrimary);
					occupiedBandwidth=occupiedPrimary+reservedBackupBw;
					new Mapping(dem, getThis());
					return true;
				} else
					return false;
			}
		};
	}

	@Override
	protected DemandVisitorAdapter createFreeVisitor() {
		return new DemandVisitorAdapter() {
			@Override
			public boolean visit(BandwidthDemand dem) {
				if (getMapping(dem) != null) {
					//use the bandwidth of the mapping to free ressource, this works for splitting
					occupiedPrimary -= dem.getDemandedBandwidth();
					occupiedPrimary = MiscelFunctions.roundThreeDecimals(occupiedPrimary);
					occupiedBandwidth=occupiedPrimary+reservedBackupBw;
					return getMapping(dem).unregister();
				} else
					return false;
			}
		};
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("BandwidthResource: bandwidth=");
		sb.append(getBandwidth());
		sb.append("Mbit/s");
		sb.append(" Primary bandwidth="+occupiedPrimary+"/"+primaryCap);
		if (getMappings().size() > 0)
			sb.append(getMappingsString());

		sb.append("\n Total backup bandwidth="+maxRiskTotal()+"/"+backupCap);
//		if(this.getBackupMappings().size()>0)
//			sb.append(getBackupMappingsString());
		sb.append("\n");
		for(Risk r : risks){
			sb.append(r);
		}
		return sb.toString();
	}

	@Override
	//not work
	public AbstractResource getCopy(
			NetworkEntity<? extends AbstractConstraint> owner) {

		BandwidthResource clone = new BandwidthResource(
				(Link<? extends AbstractConstraint>) owner, this.getName());
		clone.bandwidth = bandwidth;
		clone.primaryCap=this.primaryCap;
		clone.backupCap=this.backupCap;
		clone.occupiedPrimary=this.occupiedPrimary;
		clone.reservedBackupBw=this.reservedBackupBw;
		clone.occupiedBandwidth=this.occupiedBandwidth;
		return clone;
	}
	
	public boolean reset(){
		occupiedPrimary=0.0;
		occupiedBandwidth=0.0;
		reservedBackupBw=0.0;
		this.risks.clear();
		this.unregisterAll();
		return true;
	}
	
	public List<Risk> getRisks() {
		return risks;
	}

	public boolean backupAssignation(BandwidthDemand bwd, boolean share, SubstrateLink failure){
		if(share){
			Risk risk=this.findRiskByLink(failure);
			if(risk!=null)	risk.addDemand(bwd);
			else	risks.add(new Risk(failure,bwd));
			
			double maxTotal =this.maxRiskTotal();
			if(maxTotal>reservedBackupBw){
				reservedBackupBw = maxTotal;
				reservedBackupBw = MiscelFunctions.roundThreeDecimals(reservedBackupBw);
				occupiedBandwidth = MiscelFunctions.roundThreeDecimals(occupiedPrimary + reservedBackupBw);
			}
		}
		else{
			reservedBackupBw += bwd.getDemandedBandwidth();
			reservedBackupBw = MiscelFunctions.roundThreeDecimals(reservedBackupBw);
			occupiedBandwidth = MiscelFunctions.roundThreeDecimals(occupiedPrimary + reservedBackupBw);
		}
		new Mapping(bwd, getThis(), true); //add backup mapping
		return true;
	}
	
	public boolean backupAssignation(BandwidthDemand bwd, boolean share, Collection<SubstrateLink> failures){
		if(share){
			for(SubstrateLink sl: failures){
				boolean newRisk=true;
				for(Risk risk:risks){
					if(risk.getNe().equals(sl)){
						newRisk=false;
						risk.addDemand(bwd);
						break;
					}
				}
				if(newRisk){
					risks.add(new Risk(sl,bwd));
				}
			}
			
			double maxTotal =this.maxRiskTotal();
			if(maxTotal>reservedBackupBw){
				reservedBackupBw = maxTotal;
				reservedBackupBw = MiscelFunctions.roundThreeDecimals(reservedBackupBw);
				occupiedBandwidth = MiscelFunctions.roundThreeDecimals(occupiedPrimary + reservedBackupBw);
			}
		}
		else{
			reservedBackupBw += bwd.getDemandedBandwidth();
			reservedBackupBw = MiscelFunctions.roundThreeDecimals(reservedBackupBw);
			occupiedBandwidth = MiscelFunctions.roundThreeDecimals(occupiedPrimary + reservedBackupBw);
		}
		new Mapping(bwd, getThis(), true); //add backup mapping
		return true;
	}
	
	public boolean backupFree(BandwidthDemand bwd, boolean share){
		List<Mapping> list=getMappingBackup(bwd);
		if(list!=null){
			if(share){
				//use iterator to remove an element of the list in a loop
				for(Iterator<Risk> iterator=risks.iterator();iterator.hasNext();){
					Risk risk = iterator.next();
					risk.findAndRemove(bwd);
					if(risk.getDemands().isEmpty()){
						iterator.remove();
					}						
				}
				
				double maxRiskTotal=maxRiskTotal();
				if(maxRiskTotal<reservedBackupBw){
					reservedBackupBw = MiscelFunctions.roundThreeDecimals(maxRiskTotal);
					occupiedBandwidth = MiscelFunctions.roundThreeDecimals(occupiedPrimary + maxRiskTotal);
				}
				for(Mapping m:list)
					m.unregisterBackup();
			}
			else{
				reservedBackupBw -= bwd.getDemandedBandwidth();
				reservedBackupBw = MiscelFunctions.roundThreeDecimals(reservedBackupBw);
				occupiedBandwidth = MiscelFunctions.roundThreeDecimals(occupiedPrimary + reservedBackupBw);
				for(Mapping m:list)
					m.unregisterBackup();
			}
		}
		return true;
	}
	
	public double maxRiskTotal(){
		double max = 0.0;
		for(Risk r:risks){
			double tmp = r.getTotal();
			if(tmp>max)
				max=tmp;
		}
		return max;
	}
	
	public Risk findRiskByLink(SubstrateLink sl){
		for(Risk r:this.risks){
			if(r.getNe().equals(sl)){
				return r;
			}
		}
		return null;
	}
	
	
}
