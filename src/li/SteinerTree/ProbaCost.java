package li.SteinerTree;

import org.apache.commons.collections15.Transformer;

import vnreal.network.substrate.SubstrateLink;

public class ProbaCost implements Transformer<SubstrateLink, Double> {

	@Override
	public Double transform(SubstrateLink arg0) {
		//availability : log(1-p)
		return -Math.log(1-arg0.getProbability());	
	}

}
