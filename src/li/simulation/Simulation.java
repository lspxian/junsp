package li.simulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import li.evaluation.metrics.AcceptedRatioL;
import li.evaluation.metrics.CostL;
import li.evaluation.metrics.CostPerMappedNetworkL;
import li.evaluation.metrics.CostRevenueL;
import li.evaluation.metrics.LinkCostPerVnrL;
import li.evaluation.metrics.LinkUtilizationL;
import li.evaluation.metrics.MappedRevenueL;
import li.evaluation.metrics.Metric;
import li.evaluation.metrics.NodeUtilizationL;
import li.evaluation.metrics.RevenueCostL;
import li.evaluation.metrics.TotalRevenueL;
import li.evaluation.metrics.tempCostRevenueL;
import vnreal.algorithms.linkmapping.PathSplittingVirtualLinkMapping;
import vnreal.algorithms.linkmapping.UnsplittingLPCplex;
import vnreal.algorithms.nodemapping.AvailableResourcesNodeMapping;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class Simulation {
	private SubstrateNetwork sn;
	private ArrayList<VirtualNetwork> vns;
	private ArrayList<VirtualNetwork> mappedVNs;
	private ArrayList<VirtualNetwork> vnEvent;
	private ArrayList<VnEvent> events;
	private ArrayList<Metric> metrics;
	private double simulationTime = 20000.0;
	private double time = 0.0;
	private int accepted = 0;
	private int rejected = 0;
	private double lambda = 4.0/100.0;
	public int getAccepted() {
		return accepted;
	}

	public int getRejected() {
		return rejected;
	}
	public SubstrateNetwork getSubstrateNetwork(){
		return sn;
	}
	public ArrayList<VirtualNetwork> getVirtualNetworks()
	{
		return vns;
	}
	public ArrayList<VirtualNetwork> getList()
	{
		return vnEvent;
	}
	
	public ArrayList<VirtualNetwork> getMappedVNs() {
		return mappedVNs;
	}

	public ArrayList<VnEvent> getVnEvents()
	{
		return events;
	}
	public ArrayList<Metric> getMetrics() {
		return metrics;
	}
	
	public Simulation() throws IOException{
		
		sn=new SubstrateNetwork(false,true); //control the directed or undirected
		//sn.alt2network("data/longHaul");
		sn.alt2network("sndlib/ta2");
		sn.addAllResource(false);
		
		vns = new ArrayList<VirtualNetwork>();
		for(int i=0;i<10;i++){
			VirtualNetwork vn = new VirtualNetwork(1,false);
			vn.alt2network("data/vir"+i);
			vn.addAllResource(false);
			vns.add(vn);
		}
		
		events = new ArrayList<VnEvent>();
		for(int i=0;(time <=simulationTime)	&& (i <vns.size());i++){
			events.add(new VnEvent(vns.get(i),time,0)); //arrival event
			double departure = time+vns.get(i).getLifetime();
			if(departure<=simulationTime)
				events.add(new VnEvent(vns.get(i),departure,1)); // departure event
			time+=MiscelFunctions.negExponential(lambda); //generate next vn arrival event
		}
		Collections.sort(events);
		
		//add metric
		//metrics = new ArrayList<Metric>();
		/*metrics.add(new AcceptedRatioL(this));
		metrics.add(new LinkUtilizationL(this));
		metrics.add(new NodeUtilizationL(this));
		metrics.add(new CostL(this));
		metrics.add(new LinkCostPerVnrL(this));
		metrics.add(new CostPerMappedNetworkL(this));
		metrics.add(new CostRevenueL(this,false));*/
		//metrics.add(new MappedRevenueL(this,false));
		/*metrics.add(new RevenueCostL(this,false));
		metrics.add(new tempCostRevenueL(this,false));
		metrics.add(new TotalRevenueL(this,false));*/
		
		mappedVNs = new ArrayList<VirtualNetwork>();
		vnEvent = new ArrayList<VirtualNetwork>();
		
	}
	
	//TODO
	//constructor with parameters
	
	public void runSimulation() throws IOException{
		for(VnEvent currentEvent : events){
			
			if(currentEvent.getFlag()==0){
				AvailableResourcesNodeMapping arnm = new AvailableResourcesNodeMapping(sn,50,true,false);
				
				vnEvent.add(currentEvent.getConcernedVn());
				if(arnm.nodeMapping(currentEvent.getConcernedVn())){
					Map<VirtualNode, SubstrateNode> nodeMapping = arnm.getNodeMapping();
					//System.out.println(nodeMapping);
					//System.out.println("node mapping succes, virtual netwotk "+j);
					
					//link mapping
					//UnsplittingLPCplex ulpc = new UnsplittingLPCplex(sn,0.3,0.7);
					PathSplittingVirtualLinkMapping psvlm = new PathSplittingVirtualLinkMapping(sn,0.3,0.7);
					if(psvlm.linkMapping(currentEvent.getConcernedVn(), nodeMapping)){
						this.accepted++;
						mappedVNs.add(currentEvent.getConcernedVn());
					}
					else{
						this.rejected++;
						System.out.println("link resource error, virtual network"); //TODO print vn id 
					}
				}
				else{
					this.rejected++;
					//System.out.println("node resource error, virtual network "+j);
				}
				//System.out.println("Duree d'execution :"+duree);
			}
			else{
				System.out.println("Liberation Ressources");
				NodeLinkDeletion.freeResource(currentEvent.getConcernedVn(), sn);
			}
			/*
			for(Metric metric : metrics){ //write data to file
				metric.getFout().write(currentEvent.getAoDTime()+" " +metric.calculate()+"\n");
			}*/
			
		}
		/*
		for(Metric metric : metrics){
			metric.getFout().close();
		}*/
		
		
		
	}
}
