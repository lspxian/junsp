package cherif;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
import vnreal.evaluations.metrics.AcceptedRatio;
import vnreal.evaluations.metrics.AcceptedVnrRatio;
import vnreal.evaluations.metrics.Cost;
import vnreal.evaluations.metrics.LinkCostPerVnr;
import vnreal.evaluations.metrics.LinkUtilization;
import vnreal.evaluations.metrics.NodeUtilization;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.resources.BandwidthResource;

public class MainTest {
public static void main(String[] args) throws IOException {
		SubstrateNetwork sn=new SubstrateNetwork(false,true); //control the directed or undirected
		sn.alt2network("data/cost239");
		
		List<VnEvent> events = new ArrayList<VnEvent>();	  // The list of events 
		double simulationTime = 50000.0;			 //Simulation Time 
		double time=0.0;							 //for Arrivals Time
		int i=0,j=0;
	
		long duree=0;
		double capacity=0.0,sum=0.0;
		double lambda = 4.0/100.0;
		int acepted = 0,rejected = 0;
		
		
		
		BufferedWriter fout = new BufferedWriter(new FileWriter("aceptedratio.txt"));
		BufferedWriter fout1 = new BufferedWriter(new FileWriter("cost.txt"));
		BufferedWriter fout2 = new BufferedWriter(new FileWriter("costpermapped.txt"));
		BufferedWriter fout3 = new BufferedWriter(new FileWriter("costrevenue.txt"));
		BufferedWriter fout4 = new BufferedWriter(new FileWriter("linkcost.txt"));
		BufferedWriter fout5 = new BufferedWriter(new FileWriter("linkutilization.txt"));
		BufferedWriter fout6 = new BufferedWriter(new FileWriter("nodeutilization.txt"));
		/*for(int k=0;k<=15;k++)
		{
			periodTest = MiscelFunctions.negExponential(meanVn);
			System.out.println(periodTest+"\n");
		}*/
		
		sn.addAllResource(true);
		
		//virtual network list
		List<VirtualNetwork> vns = new ArrayList<VirtualNetwork>();
		for( i=0;i<500;i++){
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
		/*Ratios */
		List<AcceptedVnrRatio> Ratios = new ArrayList<AcceptedVnrRatio>();
		
			i=0;
			while( (time <=simulationTime)	&& (i <vns.size())) 
			{
				VnEvent ArrivalEvent = new VnEvent(vns.get(i),time,0);
				events.add(ArrivalEvent);
				VnEvent DepartureEvent = new VnEvent(vns.get(i),time+vns.get(i).getLifetime(),1);
				events.add(DepartureEvent);
				time+=MiscelFunctions.negExponential(lambda);
				//System.out.println(time);
				i++;
			}
			Collections.sort(events);
		//	System.out.println("After The sort\n");
			
		//	System.out.println(sn);
			int k=0;
		for(i=0;i<events.size();i++)
			{	
				VnEvent currentEvent;
				currentEvent=(VnEvent) events.get(i);
				if(currentEvent.getFlag()==0)
					{
						j++;
					//System.out.println("virtual network "+i+": \n"+currentEvent.getConcernedVn());
					//node mapping
					AvailableResourcesNodeMapping arnm = new AvailableResourcesNodeMapping(sn,50,true,false);
					
					if(arnm.nodeMapping(currentEvent.getConcernedVn())){
						acepted++;
						//System.out.println("vn accepté :" + acepted);
						//System.out.println("node mapping succes, virtual netwotk "+j);
					}else{
						rejected++;
						//System.out.println("vn rejeté :" + rejected);
						//System.out.println("node resource error, virtual network "+j);
						continue;
					}
					Map<VirtualNode, SubstrateNode> nodeMapping = arnm.getNodeMapping();
					System.out.println(nodeMapping);
					
					//link mapping
					
					UnsplittingLPCplex ulpc = new UnsplittingLPCplex(sn,0.3,0.7);
					ulpc.linkMapping(currentEvent.getConcernedVn(), nodeMapping);
					System.out.println("Duree d'execution :"+duree);
					}
				else
				{
					
					System.out.println("Liberation Ressources");
					NodeLinkDeletion.freeRessource(currentEvent.getConcernedVn(), sn);
				}
				//if((j%8)==0)
				//{
				/*for (SubstrateLink sl : sn.getEdges()) {
					for (AbstractResource res : sl.get()) {
						capacity += ((BandwidthResource) res).getBandwidth();
					}
				}
				for (SubstrateLink sl : sn.getEdges()) {
					for (AbstractResource res : sl.get()) {
						for (Mapping m : res.getMappings()) {
							AbstractDemand dem = m.getDemand();
							if (dem instanceof BandwidthDemand) {
								sum += ((BandwidthDemand) dem).getDemandedBandwidth();
							}
						}
					}
				}
				System.out.println("Link utilisation =" + sum/capacity);*/
					try {
						AcceptedRatio acceptedRatio = new AcceptedRatio();
						Cost cost = new Cost();
						/*CostPerMappedNetwork costpermapped = new CostPerMappedNetwork();
						CostRevenue costrevenue = new CostRevenue(false);*/
						LinkCostPerVnr linkcost = new LinkCostPerVnr();
						LinkUtilization linkutilization = new LinkUtilization();
						NodeUtilization nodeutilization = new NodeUtilization();
					/*	linkcost.setStack(netst);
						costrevenue.setStack(netst);
						costpermapped.setStack(netst);
						cost.setStack(netst);*/
						
					//MappedRevenue mappedRevenue = new MappedRevenue(true);
					//mappedRevenue.setStack(netst);
						
						fout.write(currentEvent.getAoDTime()+" " +acceptedRatio.calculate(acepted,rejected));
						fout.write("\n");
						fout1.write(currentEvent.getAoDTime()+" " +cost.calculateCost(sn));
						fout1.write("\n");
						/*fout2.write(currentEvent.getAoDTime()+" " +costpermapped.calculate());
						fout2.write("\n");
						fout3.write(currentEvent.getAoDTime()+" " +costrevenue.calculate());
						fout3.write("\n");*/
						fout4.write(currentEvent.getAoDTime()+" " +linkcost.linkCost(sn,acepted));
						fout4.write("\n");
						fout5.write(currentEvent.getAoDTime()+" " +linkutilization.calculate(sn)/*sum/capacity*/);
						fout5.write("\n");
						fout6.write(currentEvent.getAoDTime()+" " +nodeutilization.calculate(sn));
						fout6.write("\n");

					k++;
					}catch(FileNotFoundException e){
						e.printStackTrace();
					}catch(IOException e){
						e.printStackTrace();
					//}
					
				}

			}
		fout.close();
		fout1.close();
		fout2.close();
		fout3.close();
		fout4.close();
		fout5.close();
		fout6.close();
		}

}


