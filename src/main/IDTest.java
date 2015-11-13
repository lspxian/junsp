package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections15.Predicate;

import edu.uci.ics.jung.algorithms.filters.EdgePredicateFilter;
import li.multiDomain.Domain;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class IDTest {

	public static void main(String[] args) throws IOException {
		SubstrateNetwork sn=new SubstrateNetwork(false,true); //control the directed or undirected
		sn.alt2network("data/cost239");
		sn.addAllResource(true);
			
		EdgePredicateFilter<SubstrateNode,SubstrateLink> filter = new EdgePredicateFilter<SubstrateNode,SubstrateLink>(
				new Predicate<SubstrateLink>() {
					@Override
					public boolean evaluate(SubstrateLink sl) {
						
						return true;
					}
				});
		SubstrateNetwork sn2 = (SubstrateNetwork) filter.transform(sn);
			
		System.out.println(sn.getId());
		System.out.println(sn);
		System.out.println(sn2.getId());
		System.out.println(sn2);
		
	}

}
