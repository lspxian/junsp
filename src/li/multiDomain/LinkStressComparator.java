package li.multiDomain;

import java.util.Comparator;

import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.mapping.Mapping;
import vnreal.network.substrate.SubstrateLink;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;

public class LinkStressComparator implements Comparator<Domain> {

	@Override
	public int compare(Domain d1, Domain d2) {
		double occupy1=0,occupy2=0,capacity1=0,capacity2=0,stress1, stress2;
		for (SubstrateLink sl : d1.getEdges()) {
			for (AbstractResource res : sl) {
				if(res instanceof BandwidthResource){
					occupy1 += ((BandwidthResource) res).getOccupiedBandwidth();
					capacity1 += ((BandwidthResource) res).getBandwidth();					
				}
			}
		}
		stress1 = occupy1/capacity1 ; 
		
		for (SubstrateLink sl : d2.getEdges()) {
			for (AbstractResource res : sl) {
				if(res instanceof BandwidthResource){
					occupy2 += ((BandwidthResource) res).getOccupiedBandwidth();
					capacity2 += ((BandwidthResource) res).getBandwidth();					
				}
			}
		}
		stress2 = occupy2/capacity2;
		
		if(stress1-stress2>0) return -1;
		else if(stress1-stress2<0) return 1;
		else return 0;
	}

}
