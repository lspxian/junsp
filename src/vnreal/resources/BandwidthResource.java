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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.map.LinkedMap;

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
	private double bandwidth;
	private double occupiedBandwidth = 0;
	
	//for backup 
	private double primaryBw = 0;
	private double reservedBackupBw = 0; // max of delta, Z
	//private Map<Link<? extends AbstractConstraint>,Double> backupBw; //Yfs of guo, or delta of yazid
	private List<Risk> risks=new ArrayList<Risk>();
	

	public double getPrimaryBw() {
		return primaryBw;
	}

	public void setPrimaryBw(double primaryBw) {
		this.primaryBw = primaryBw;
	}

	public double getReservedBackupBw() {
		return reservedBackupBw;
	}

	public void setReservedBackupBw(double reservedBackupBw) {
		this.reservedBackupBw = reservedBackupBw;
	}
/*
	public void updateReservedBackupBw() {
		double max = 0 ;
		for(Map.Entry<Link<? extends AbstractConstraint>, Double> entry: backupBw.entrySet()){
			if(entry.getValue()>max)
				max = entry.getValue();
		}
		reservedBackupBw = max;
	}

	public Map<Link<? extends AbstractConstraint>, Double> getBackupBw() {
		return backupBw;
	}
	
	public double getLinkBackupBw(Link<? extends AbstractConstraint> failure){
		if(backupBw.containsKey(failure))
			return  backupBw.get(failure);
		else return 0;
	}

	public void setBackupBw(Map<Link<? extends AbstractConstraint>, Double> backupBw) {
		this.backupBw = backupBw;
	}*/

	/*
	 * Method for the distributed algorithm
	 */
	public void setOccupiedBandwidth(Double occupiedBandwidth) {
		this.occupiedBandwidth = MiscelFunctions.roundThreeDecimals(occupiedBandwidth);
	}

	public double getOccupiedBandwidth() {
		return occupiedBandwidth;
	}

	public BandwidthResource(Link<? extends AbstractConstraint> owner) {
		super(owner);
		//backupBw = new LinkedMap<Link<? extends AbstractConstraint>,Double>();
	}
	
	public BandwidthResource(Link<? extends AbstractConstraint> owner, String name) {
		super(owner, name);
	//	backupBw = new LinkedMap<Link<? extends AbstractConstraint>,Double>();
	}
	

	@ExchangeParameter
	public void setBandwidth(Double bandwidth) {
		this.bandwidth = MiscelFunctions.roundThreeDecimals(bandwidth);
	}

	@ExchangeParameter
	public Double getBandwidth() {
		return this.bandwidth;
	}

	public Double getAvailableBandwidth() {
		return bandwidth - occupiedBandwidth;
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
					primaryBw += dem.getDemandedBandwidth();
					primaryBw = MiscelFunctions.roundThreeDecimals(primaryBw);
					occupiedBandwidth = MiscelFunctions.roundThreeDecimals(primaryBw + reservedBackupBw);
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
					primaryBw -= dem.getDemandedBandwidth();
					primaryBw = MiscelFunctions.roundThreeDecimals(primaryBw);
					occupiedBandwidth = MiscelFunctions.roundThreeDecimals(primaryBw + reservedBackupBw);
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
		sb.append(" occupied bandwidth="+occupiedBandwidth+"\n");
		sb.append(" Primary bandwidth="+primaryBw);
		if (getMappings().size() > 0)
			sb.append(getMappingsString());

		sb.append("\n Reserved backup bandwidth="+reservedBackupBw);
		if(this.getBackupMappings().size()>0){
			sb.append(getBackupMappingsString());
		}
		sb.append("\n");
		for(Risk r : risks){
			sb.append(r);
		}
	/*	for(Map.Entry<Link<? extends AbstractConstraint>, Double> entry: backupBw.entrySet()){
			sb.append("bw="+entry.getValue()+"@"+entry.getKey().toString()+" ");
		}*/
		return sb.toString();
	}

	@Override
	//not work
	public AbstractResource getCopy(
			NetworkEntity<? extends AbstractConstraint> owner) {

		BandwidthResource clone = new BandwidthResource(
				(Link<? extends AbstractConstraint>) owner, this.getName());
		clone.bandwidth = bandwidth;
		clone.occupiedBandwidth = occupiedBandwidth;

		return clone;
	}
	
	public boolean reset(){
		this.setOccupiedBandwidth(0.0);
		this.primaryBw=0.0;
		this.reservedBackupBw=0.0;
		this.risks.clear();
		this.unregisterAll();
		return true;
	}
	
	public List<Risk> getRisks() {
		return risks;
	}

	public boolean backupAssignation(BandwidthDemand bwd, boolean share, SubstrateLink failure){
		if(share){
			boolean newRisk=true;
			for(Risk risk:risks){
				if(risk.getNe().equals(failure)){
					newRisk=false;
					risk.addDemand(bwd);
					break;
				}
			}
			if(newRisk){
				risks.add(new Risk(failure,bwd));
			}
			
			double maxTotal =this.maxRiskTotal();
			if(maxTotal>reservedBackupBw){
				reservedBackupBw = maxTotal;
				reservedBackupBw = MiscelFunctions.roundThreeDecimals(reservedBackupBw);
				occupiedBandwidth = MiscelFunctions.roundThreeDecimals(primaryBw + reservedBackupBw);
			}
		}
		else{
			reservedBackupBw += bwd.getDemandedBandwidth();
			reservedBackupBw = MiscelFunctions.roundThreeDecimals(reservedBackupBw);
			occupiedBandwidth = MiscelFunctions.roundThreeDecimals(primaryBw + reservedBackupBw);
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
				occupiedBandwidth = MiscelFunctions.roundThreeDecimals(primaryBw + reservedBackupBw);
			}
		}
		else{
			reservedBackupBw += bwd.getDemandedBandwidth();
			reservedBackupBw = MiscelFunctions.roundThreeDecimals(reservedBackupBw);
			occupiedBandwidth = MiscelFunctions.roundThreeDecimals(primaryBw + reservedBackupBw);
		}
		new Mapping(bwd, getThis(), true); //add backup mapping
		return true;
	}
	
	public boolean backupFree(BandwidthDemand bwd, boolean share){
		if(this.getMappingBackup(bwd)!=null){
			if(share){
				//use iterator to remove an element of the list in a loop
				for(Iterator<Risk> iterator=risks.iterator();iterator.hasNext();){
					Risk risk = iterator.next();
					risk.removeDemand(bwd);
					if(risk.getDemands().isEmpty())
						iterator.remove();
				}
				
				reservedBackupBw = 0.0;
				occupiedBandwidth = MiscelFunctions.roundThreeDecimals(primaryBw);
				for(Risk risk:risks){
					double riskTotal = risk.getTotal();
					if(riskTotal>=reservedBackupBw){
						reservedBackupBw = riskTotal;
						reservedBackupBw = MiscelFunctions.roundThreeDecimals(reservedBackupBw);
						occupiedBandwidth = MiscelFunctions.roundThreeDecimals(primaryBw + reservedBackupBw);
					}
				}
				while(this.getMappingBackup(bwd)!=null)
					this.getMappingBackup(bwd).unregisterBackup();
			}
			else{
				reservedBackupBw -= bwd.getDemandedBandwidth();
				reservedBackupBw = MiscelFunctions.roundThreeDecimals(reservedBackupBw);
				occupiedBandwidth = MiscelFunctions.roundThreeDecimals(primaryBw + reservedBackupBw);
				while(this.getMappingBackup(bwd)!=null)
					this.getMappingBackup(bwd).unregisterBackup();
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
