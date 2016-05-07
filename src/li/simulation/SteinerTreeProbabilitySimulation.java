package li.simulation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import li.SteinerTree.SteinerILPExact;
import li.evaluation.metrics.AcceptedRatioL;
import li.evaluation.metrics.CostL;
import li.evaluation.metrics.CostRevenueL;
import li.evaluation.metrics.CurrentLinkUtilisationL;
import li.evaluation.metrics.LinkUtilizationL;
import li.evaluation.metrics.MappedRevenueL;
import li.evaluation.metrics.Metric;
import li.evaluation.metrics.ProbabilityL;
import li.evaluation.metrics.RevenueProba;
import li.gt_itm.Generator;
import vnreal.algorithms.AbstractLinkMapping;
import vnreal.algorithms.linkmapping.SteinerTreeHeuristic;
import vnreal.algorithms.nodemapping.AvailableResourcesNodeMapping;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.evaluations.metrics.AcceptedRatio;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class SteinerTreeProbabilitySimulation extends AbstractSimulation{
	
	protected Map<VirtualNetwork, Double> probability; 
	
	public Map<VirtualNetwork, Double> getProbability() {
		return probability;
	}

	public SteinerTreeProbabilitySimulation(){
		
		simulationTime = 10000.0;
		this.sn=new SubstrateNetwork(); //undirected by default 
		try {
			sn.alt2network("sndlib/germany50");
		} catch (IOException e) {
			e.printStackTrace();
		}
		//sn.addAllResource(true);
		sn.addInfiniteResource();
	}
	
	public void initialize(int lambda) throws IOException{
		this.time=0.0;
		this.accepted=0;
		this.rejected=0;
		this.lambda=lambda;
		this.totalCost=0.0;
		/*-----------use pre-generated virtual network---------*/
		/*
		vns = new ArrayList<VirtualNetwork>();
		for(int i=0;i<40;i++){
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
		
		//add metric
		metrics = new ArrayList<Metric>();
		mappedVNs = new ArrayList<VirtualNetwork>();
		this.probability = new LinkedHashMap<VirtualNetwork,Double>();
	}
	public void runSimulation(String methodStr) throws IOException{
		//add metrics
		metrics.add(new AcceptedRatioL(this, methodStr,lambda));
	//	metrics.add(new LinkUtilizationL(this, methodStr,lambda));
//		metrics.add(new CurrentLinkUtilisationL(this, methodStr,lambda));
		metrics.add(new MappedRevenueL(this, methodStr,lambda));
		//metrics.add(new CostL(this, methodStr,lambda));
		//metrics.add(new CostRevenueL(this,methodStr,lambda));
		metrics.add(new ProbabilityL(this,methodStr,lambda));
		metrics.add(new RevenueProba(this,methodStr,lambda));
		
		for(VnEvent currentEvent : events){
			
			System.out.println("/------------------------------------/");
			System.out.println("New event at time :	"+currentEvent.getAoDTime()+" for vn:"+currentEvent.getConcernedVn().getId());
			System.out.println("At this moment, accepted:"+this.accepted+" rejected:"+this.rejected);
			System.out.print("Current vn : \n"+currentEvent.getConcernedVn()+"\n");

			if(currentEvent.getFlag()==0){
				AvailableResourcesNodeMapping arnm = new AvailableResourcesNodeMapping(sn,40,true,false);
				System.out.println("Operation : Mapping");
				
				if(arnm.nodeMapping(currentEvent.getConcernedVn())){
					Map<VirtualNode, SubstrateNode> nodeMapping = arnm.getNodeMapping();
					System.out.println("node mapping succes : "+nodeMapping);
					
					//link mapping method
					AbstractLinkMapping method;
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
					default : 
						System.out.println("The methode doesn't exist");
						method = null;
					}
					
					if(method.linkMapping(currentEvent.getConcernedVn(), nodeMapping)){
						this.accepted++;
						mappedVNs.add(currentEvent.getConcernedVn());
						this.totalCost=this.totalCost+currentEvent.getConcernedVn().getTotalCost(sn);
						
						if(method instanceof SteinerTreeHeuristic){
							this.probability.put(currentEvent.getConcernedVn(), 
									((SteinerTreeHeuristic) method).getProbability());
						}
						else if(method instanceof SteinerILPExact){
							this.probability.put(currentEvent.getConcernedVn(), 
									((SteinerILPExact) method).getProbability());
						}
						
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
				//System.out.println("Duree d'execution :"+duree);
			}
			else{
				System.out.println("Operation : Liberation Ressources");
				NodeLinkDeletion.freeResource(currentEvent.getConcernedVn(), sn);
			}
			
			for(Metric metric : metrics){ //write data to file TODO
				double value = metric.calculate();
				System.out.println(metric.name()+" "+value);
				metric.getFout().write(currentEvent.getAoDTime()+" " +value+"\n");
			}
			
		}
		
		System.out.println("*-----"+methodStr+" resume------------*");
		System.out.println("accepted : "+this.accepted);
		System.out.println("rejected : "+this.rejected);
		
		FileWriter writer = new FileWriter("resultat.txt",true);
		writer.write("*----lambda="+this.lambda+"--"+methodStr+"----*\n");
		writer.write("accepted : "+this.accepted+"\n");
		writer.write("rejected : "+this.rejected+"\n");
		for(Metric metric : metrics){ 
			writer.write(metric.name()+" "+metric.calculate()+"\n");
		}
		writer.write("\n");
		writer.close();
		
		for(Metric metric : metrics){
			metric.getFout().close();
		}
		
		
	}
	@Override
	public void reset() {
		NodeLinkDeletion.resetNet(this.sn);
		this.accepted = 0;
		this.rejected = 0;
		this.totalCost=0.0;
		mappedVNs = new ArrayList<VirtualNetwork>();
		metrics = new ArrayList<Metric>();
		this.probability = new LinkedHashMap<VirtualNetwork,Double>();
	}
	
}
