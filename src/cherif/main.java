package cherif;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import vnreal.algorithms.linkmapping.UnsplittingLPCplex;
import vnreal.algorithms.nodemapping.AvailableResourcesNodeMapping;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.resources.BandwidthResource;

public class main {
public static void main(String[] args) throws IOException {
		SubstrateNetwork sn=new SubstrateNetwork(false,true); //control the directed or undirected
		sn.alt2network("data/cost239");
		
		List Events = new ArrayList<VnEvent>();	  // The list of events 
		double simulationTime = 50000.0;			 //Simulation Time 
		double time=0.0;							 //for Arrivals Time
		int i=0;
		double lambda = 4.0/100.0;
		sn.addAllResource(true);

		Transformer<SubstrateLink, Double> weightTrans = new Transformer<SubstrateLink,Double>(){
			public Double transform(SubstrateLink link){
				return 1/((BandwidthResource)link.get().get(0)).getAvailableBandwidth();
			}
		};
		
		Transformer<SubstrateLink, Double> basicTrans = new Transformer<SubstrateLink,Double>(){
			public Double transform(SubstrateLink link){
				return 1.0;
			}
		};
	
		
		//virtual network list
		List<VirtualNetwork> vns = new ArrayList<VirtualNetwork>();
		for( i=0;i<100;i++){
			VirtualNetwork vn = new VirtualNetwork(1,false);
			vn.alt2network("data/vir"+i);
			vn.addAllResource(true);
			//System.out.println("virtual network\n"+vn);
			vns.add(vn);
			//System.out.println("LifeTime Print");
			//System.out.println(vn.getLifetime());
			
		}
	
		//Network stack
	
		NetworkStack netst = new NetworkStack(sn,vns);	
		
		/*for(int i=0;i<1;i++){
			System.out.println("virtual network "+i+": \n"+vns.get(i));
			/*Node Mapping and VnEvent Use
			//node mapping
			AvailableResourcesNodeMapping arnm = new AvailableResourcesNodeMapping(sn,50,true,false);
			
			if(arnm.nodeMapping(vns.get(i))){
				System.out.println("node mapping succes, virtual netwotk "+i);
			}else{
				System.out.println("node resource error, virtual network "+i);
				continue;
			}
			Map<VirtualNode, SubstrateNode> nodeMapping = arnm.getNodeMapping();
			System.out.println(nodeMapping);*/
			
		
			
			/*SOD_BK sod_bk = new SOD_BK(sn);
			sod_bk.linkMapping(vns.get(i), nodeMapping);
			//sod_bk.generateFile(vns.get(i), nodeMapping);*/
			i=0;
			while( (time <=simulationTime)	&& (i <vns.size())) 
			{
				VnEvent ArrivalEvent = new VnEvent(vns.get(i),time,0);
				Events.add(ArrivalEvent);
				VnEvent DepartureEvent = new VnEvent(vns.get(i),time+vns.get(i).getLifetime(),1);
				Events.add(DepartureEvent);
				time+=MiscelFunctions.negExponential(lambda);
				i++;
			}

			Collections.sort(Events);
		//	System.out.println("After The sort\n");
			int j=0;
		//	System.out.println(sn);
		for(i=0;i<Events.size();i++)
			{	
				VnEvent currentEvent;
				currentEvent=(VnEvent) Events.get(i);
				if(currentEvent.getFlag()==0)
					{
					j++;
					System.out.println("virtual network "+i+": \n"+currentEvent.getConcernedVn());
					//node mapping
					AvailableResourcesNodeMapping arnm = new AvailableResourcesNodeMapping(sn,50,true,false);
					
					if(arnm.nodeMapping(currentEvent.getConcernedVn())){
						System.out.println("node mapping succes, virtual netwotk "+j);
					}else{
						System.out.println("node resource error, virtual network "+j);
						continue;
					}
					Map<VirtualNode, SubstrateNode> nodeMapping = arnm.getNodeMapping();
					System.out.println(nodeMapping);
					
					//link mapping
					
					UnsplittingLPCplex ulpc = new UnsplittingLPCplex(sn,0.3,0.7);
					ulpc.linkMapping(currentEvent.getConcernedVn(), nodeMapping);
					}
					
				else
				{
					
					System.out.println("Liberation Ressources");
					NodeLinkDeletion.freeRessource(currentEvent.getConcernedVn(), sn);
				}
				//System.out.println(sn);
			}
		}
}


