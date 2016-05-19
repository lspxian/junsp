package li.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import li.gt_itm.Generator;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.mapping.Mapping;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;

public class EventTest {

	public static void main(String[] args) throws IOException {
		
		double simulationTime = 10000.0;
		SubstrateNetwork sn=new SubstrateNetwork(); //undirected by default 
	
		Generator.createSubNet();
		sn.alt2network("./gt-itm/sub");
//			sn.alt2network("data/cost239");
//			sn.alt2network("sndlib/germany50");

		sn.addAllResource(true);
		
		List<NetEvent> events = new ArrayList<NetEvent>();
		double time=0.0;
		int lambda=4;
//		while(time<simulationTime){
//			VirtualNetwork vn = new VirtualNetwork();
//			Generator.createVirNet();
//			vn.alt2network("./gt-itm/sub");
//			vn.addAllResource(true);
//			
//			double departureTime = time+vn.getLifetime();
//			events.add(new VnEvent(vn,time,0)); //arrival event
//			//if(departureTime<=simulationTime)
//				events.add(new VnEvent(vn,departureTime,1)); // departure event
//			time+=MiscelFunctions.negExponential(lambda/100.0); //generate next vn arrival event
//		}
		
		for(SubstrateLink sl : sn.getEdges()){
			time=MiscelFunctions.negExponential(sl.getProbability());
			while(time<simulationTime){
				events.add(new FailureEvent(time,sl));
				time+=MiscelFunctions.negExponential(sl.getProbability());
			}
			BandwidthResource bw = (BandwidthResource)sl.get().get(0);
			for(Mapping m :bw.getMappings()){
				VirtualLink vl=(VirtualLink)m.getDemand().getOwner();
				
			}
		}
		
		Collections.sort(events);
		
		System.out.println(events);
		
	}

}
