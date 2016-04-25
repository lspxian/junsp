package li.SteinerTree;

import java.util.Comparator;

import vnreal.network.substrate.SubstrateLink;

public class LinkProbaComparator implements Comparator<SubstrateLink> {

	@Override
	public int compare(SubstrateLink o1, SubstrateLink o2) {
		//TODO
		if(o1.getProbability()>o2.getProbability())
			return 1;
		else if(o1.getProbability()<o2.getProbability())
			return -1;
		else return 0;
	}

}
