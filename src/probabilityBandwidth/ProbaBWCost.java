package probabilityBandwidth;

import org.apache.commons.collections15.Transformer;

import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.virtual.VirtualLink;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;

public class ProbaBWCost implements Transformer<SubstrateLink, Double> {

	private double bwDemand;
	
	public ProbaBWCost(VirtualLink vl){
		BandwidthDemand bwDem =null;
		for(AbstractDemand abd : vl){
			if(abd instanceof BandwidthDemand){
				bwDem = (BandwidthDemand)abd;
				break;
			}
		}
		this.bwDemand = bwDem.getDemandedBandwidth();
	}
	@Override
	public Double transform(SubstrateLink arg0) {
		BandwidthResource bwRes = null;
		for(AbstractResource abr : arg0){
			if(abr instanceof AbstractResource){
				bwRes = (BandwidthResource) abr;
				break;
			}
		}

		// bw constraint
		if(this.bwDemand>bwRes.getAvailableBandwidth())
			return 1000.0;		
		//availability : log(1-p)	
		else return -Math.log(1-arg0.getProbability());
	}

}
