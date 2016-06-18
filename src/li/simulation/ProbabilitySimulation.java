package li.simulation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import li.SteinerTree.SteinerILPExact;
import li.evaluation.metrics.AcceptedRatioL;
import li.evaluation.metrics.AffectedRevenue;
import li.evaluation.metrics.Affected_VN_Number;
import li.evaluation.metrics.AverageAffectedVNRatio;
import li.evaluation.metrics.AverageProbability;
import li.evaluation.metrics.CostL;
import li.evaluation.metrics.CostRevenueL;
import li.evaluation.metrics.CurrentLinkUtilisationL;
import li.evaluation.metrics.LinkUtilizationL;
import li.evaluation.metrics.MappedRevenueL;
import li.evaluation.metrics.Metric;
import li.evaluation.metrics.ProbabilityL;
import li.evaluation.metrics.RevenueProba;
import li.event.FailureEvent;
import li.event.NetEvent;
import li.event.VnEvent;
import li.gt_itm.DrawGraph;
import li.gt_itm.Generator;
import probabilityBandwidth.AbstractProbaLinkMapping;
import probabilityBandwidth.PBBWExactILP;
import probabilityBandwidth.ProbaHeuristic1;
import probabilityBandwidth.ProbaHeuristic2;
import probabilityBandwidth.ProbaHeuristic3;
import probabilityBandwidth.ShortestPathBW;
import probabilityBandwidth.UnsplittableBandwidth;
import vnreal.algorithms.linkmapping.SteinerTreeHeuristic;
import vnreal.algorithms.nodemapping.AvailableResourcesNodeMapping;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.evaluations.metrics.AcceptedRatio;
import vnreal.mapping.Mapping;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.resources.BandwidthResource;

public class ProbabilitySimulation extends AbstractSimulation{
	protected ArrayList<NetEvent> netEvents;
	protected int affected;
	protected double affectedRevenue;
	protected ArrayList<Double> affectedRatio;
	protected int failures;
	
	public ArrayList<Double> getAffectedRatio() {
		return affectedRatio;
	}
	public int getFailures() {
		return failures;
	}
	public int getAffected() {
		return affected;
	}
	public double getAffectedRevenue() {
		return affectedRevenue;
	}
	
	public ProbabilitySimulation(){
		
		simulationTime = 30001.0;
		this.sn=new SubstrateNetwork(); //undirected by default 
		try {
			Generator.createSubNet();
			sn.alt2network("./gt-itm/sub");
//			sn.alt2network("data/cost239");
//			sn.alt2network("sndlib/germany50");
			
//			DrawGraph dg = new DrawGraph(sn);
//			dg.draw();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sn.addAllResource(true);
//		sn.addInfiniteResource();

	}
	
	public void initialize(int lambda) throws IOException{
		this.time=0.0;
		this.accepted=0;
		this.rejected=0;
		this.lambda=lambda;
		this.totalCost=0.0;
		this.affected=0;
		this.affectedRevenue=0.0;
		this.failures = 0;
		/*-----------use pre-generated virtual network---------*/
		/*
		vns = new ArrayList<VirtualNetwork>();
		for(int i=100;i<140;i++){
			VirtualNetwork vn = new VirtualNetwork();
			vn.alt2network("data/vir"+i);
//			vn.alt2network("data/vir"+new Random().nextInt(500));
//			vn.alt2network("data/vhr2");
			vn.addAllResource(true);
			//System.out.println(vn);		//print vn
			vns.add(vn);
		}
		events = new ArrayList<VnEvent>();
		for(int i=0;(time <=simulationTime)	&& (i <vns.size());i++){
			events.add(new VnEvent(vns.get(i),time,0)); //arrival event
			double departure = time+vns.get(i).getLifetime();
			if(departure<=simulationTime)
				events.add(new VnEvent(vns.get(i),departure,1)); // departure event
			time+=MiscelFunctions.negExponential(lambda/100.0); //generate next vn arrival event
		}
		Collections.sort(events);*/
		
		/*---------random virtual network-----------*/
		
		events = new ArrayList<VnEvent>();
		while(time<simulationTime){
			VirtualNetwork vn = new VirtualNetwork();
			Generator.createVirNet();
			vn.alt2network("./gt-itm/sub");
			vn.addAllResource(true);
			
			double departureTime = time+vn.getLifetime();
			events.add(new VnEvent(vn,time,0)); //arrival event
			if(departureTime<=simulationTime)
				events.add(new VnEvent(vn,departureTime,1)); // departure event
			time+=MiscelFunctions.negExponential(lambda/100.0); //generate next vn arrival event
		}
		Collections.sort(events);
		
		this.netEvents = new ArrayList<NetEvent>();
		this.netEvents.addAll(events);
		
		//deterministic failure event
		for(int t=300;t<simulationTime;t=t+300){
			int slindex = new Random().nextInt(this.sn.getEdgeCount());
			SubstrateLink fsl= (SubstrateLink)this.sn.getEdges().toArray()[slindex];
			netEvents.add(new FailureEvent(t,fsl));
		}
		
		//random failure event
		/*
		for(SubstrateLink sl : sn.getEdges()){
			time=MiscelFunctions.negExponential(sl.getProbability());
			while(time<simulationTime){
				netEvents.add(new FailureEvent(time,sl));
				time+=MiscelFunctions.negExponential(sl.getProbability());
			}
		}*/
		Collections.sort(this.netEvents);
		
		//add metric
		metrics = new ArrayList<Metric>();
		metricsProba = new ArrayList<Metric>();
		mappedVNs = new ArrayList<VirtualNetwork>();
		this.currentVNs = new ArrayList<VirtualNetwork>();
		this.probability = new LinkedHashMap<VirtualNetwork,Double>();
		this.affectedRatio = new ArrayList<Double>();
	}
	public void runSimulation(String methodStr) throws IOException{
		//add metrics
		metrics.add(new AcceptedRatioL(this, methodStr,lambda));
		metrics.add(new LinkUtilizationL(this, methodStr,lambda));
//		metrics.add(new CurrentLinkUtilisationL(this, methodStr,lambda));
		metrics.add(new MappedRevenueL(this, methodStr,lambda));
		//metrics.add(new CostL(this, methodStr,lambda));
		//metrics.add(new CostRevenueL(this,methodStr,lambda));
		metricsProba.add(new ProbabilityL(this,methodStr,lambda));
		metricsProba.add(new AverageProbability(this,methodStr,lambda));
		metricsProba.add(new AverageAffectedVNRatio(this,methodStr,lambda));
		metricsProba.add(new Affected_VN_Number(this,methodStr,lambda));
		metricsProba.add(new AffectedRevenue(this,methodStr,lambda));
		
		for(NetEvent currentEvent : this.netEvents){
			
			if(currentEvent instanceof VnEvent){
				VnEvent cEvent = (VnEvent) currentEvent;
				System.out.println("/------------------------------------/");
				System.out.println("New event at time :	"+currentEvent.getAoDTime()+" for vn:"+cEvent.getConcernedVn().getId());
				System.out.println("At this moment, accepted:"+this.accepted+" rejected:"+this.rejected);
				System.out.print("Current vn : \n"+cEvent.getConcernedVn()+"\n");
				
				if(cEvent.getFlag()==0){
					AvailableResourcesNodeMapping arnm = new AvailableResourcesNodeMapping(sn,40,true,false);
					System.out.println("Operation : Mapping");
					
					if(arnm.nodeMapping(cEvent.getConcernedVn())){
						Map<VirtualNode, SubstrateNode> nodeMapping = arnm.getNodeMapping();
						System.out.println("node mapping succes : "+nodeMapping);
						
						//link mapping method
						AbstractProbaLinkMapping method;
						switch (methodStr)
						{
						case "Takahashi" : 
							method = new SteinerTreeHeuristic(sn,"Takahashi");
							break;
						case "KMB1981" : 
							method = new SteinerTreeHeuristic(sn,"KMB1981");
							break;
						case "KMB1981V2" : 
							method = new SteinerTreeHeuristic(sn,"KMB1981V2");
							break;
						case "Exact" : 
							method = new SteinerILPExact(sn);
							break;
							// with bandwidth
						case "PBBWExact" :
							method = new PBBWExactILP(sn);
							break;
						case "ProbaHeuristic1" :
							method = new ProbaHeuristic1(sn);
							break;
						case "ProbaHeuristic2" :
							method = new ProbaHeuristic2(sn);
							break;
						case "ProbaHeuristic3" :
							method = new ProbaHeuristic3(sn);
							break;
						case "UnsplittableBandwidth" :
							method = new UnsplittableBandwidth(sn);
							break;
						case "ShortestPathBW" :
							method = new ShortestPathBW(sn);
							break;
						default : 
							System.out.println("The methode doesn't exist");
							method = null;
						}
//					System.out.println(this.sn.probaToString());
						
						if(method.linkMapping(cEvent.getConcernedVn(), nodeMapping)){
							this.accepted++;
							this.currentVNs.add(cEvent.getConcernedVn());
							mappedVNs.add(cEvent.getConcernedVn());
							this.totalCost=this.totalCost+cEvent.getConcernedVn().getTotalCost(sn);
							this.probability.put(cEvent.getConcernedVn(), method.getProbability());
							
							System.out.println("link mapping done");
						}
						else{
							this.rejected++;
							System.out.println("link mapping resource error"); 
						}
						
					}
					else{
						this.rejected++;
						//System.out.println("node resource error, virtual network "+j);
					}
					
					
					for(Metric metric : metrics){ //write data to file
						double value = metric.calculate();
						System.out.println(metric.name()+" "+value);
						metric.getFout().write(currentEvent.getAoDTime()+" " +value+"\n");
					}
					//System.out.println("Duree d'execution :"+duree);
				}
				else{
					System.out.println("Operation : Liberation Ressources");
					if(this.currentVNs.contains(cEvent.getConcernedVn())){
						NodeLinkDeletion.freeResource(cEvent.getConcernedVn(), sn);
						this.currentVNs.remove(cEvent.getConcernedVn());						
					}
				}
			}
			else if(currentEvent instanceof FailureEvent){
				failures++;
				Set<VirtualNetwork> affectedNet = new HashSet<VirtualNetwork>();
				FailureEvent fEvent = (FailureEvent) currentEvent;
				BandwidthResource bw = (BandwidthResource)fEvent.getFailureLink().get().get(0);
				for(Mapping m :bw.getMappings()){
					VirtualLink vl=(VirtualLink)m.getDemand().getOwner();
					for(VirtualNetwork vn : this.currentVNs){
						if(vn.containsEdge(vl))
							affectedNet.add(vn);
					}
				}
				
				this.affected+=affectedNet.size();
				for(VirtualNetwork vn : affectedNet){
					this.affectedRevenue+=vn.calculateRevenue();					
				}
				double ratio = (double)affectedNet.size()/this.currentVNs.size();
				this.affectedRatio.add(ratio);
				
				System.out.println("Failure Event: "+fEvent.getFailureLink());
				for(Metric metric : metricsProba){ //write data to file
					double value = metric.calculate();
					System.out.println(metric.name()+" "+value);
					metric.getFout().write(currentEvent.getAoDTime()+" " +value+"\n");
				}
				
			}
			
		}
		//TODO
//		System.out.println(this.sn.probaToString());
		
		System.out.println("*-----"+methodStr+" resume------------*");
		System.out.println("accepted : "+this.accepted);
		System.out.println("rejected : "+this.rejected);
		
		FileWriter writer = new FileWriter("result.txt",true);
		writer.write("*----lambda="+this.lambda+"--"+methodStr+"----*\n");
		writer.write("accepted : "+this.accepted+"\n");
		writer.write("rejected : "+this.rejected+"\n");
		for(Metric metric : metrics){ 
			writer.write(metric.name()+" "+metric.calculate()+"\n");
		}
		
		writer.write("affected vn : "+this.affected+"\n");
		writer.write("affected vn revenue : "+this.affectedRevenue+"\n");
		for(Metric metric : metricsProba){ 
			writer.write(metric.name()+" "+metric.calculate()+"\n");
		}
		
		writer.write("\n");
		writer.close();
		
		for(Metric metric : metrics){
			metric.getFout().close();
		}
		for(Metric metric : metricsProba){
			metric.getFout().close();
		}
		
	}
	@Override
	public void reset() {
		NodeLinkDeletion.resetNet(this.sn);
		this.accepted = 0;
		this.rejected = 0;
		this.totalCost=0.0;
		this.affected=0;
		this.affectedRevenue=0.0;
		this.failures=0;
		this.currentVNs = new ArrayList<VirtualNetwork>();
		this.affectedRatio=new ArrayList<Double>();
		mappedVNs = new ArrayList<VirtualNetwork>();
		metrics = new ArrayList<Metric>();
		metricsProba = new ArrayList<Metric>();
		this.probability = new LinkedHashMap<VirtualNetwork,Double>();
	}
	
}
