package li.simulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import li.evaluation.metrics.Metric;
import li.multiDomain.Domain;
import li.multiDomain.MultiDomainUtil;
import main.MultiDomainAlgoTest;
import vnreal.algorithms.AbstractLinkMapping;
import vnreal.algorithms.AbstractMultiDomainLinkMapping;
import vnreal.algorithms.linkmapping.AS_MCF;
import vnreal.algorithms.linkmapping.MultiDomainAsOneDomain;
import vnreal.algorithms.linkmapping.MultiDomainRanking;
import vnreal.algorithms.linkmapping.PathSplittingVirtualLinkMapping;
import vnreal.algorithms.linkmapping.Shen2014;
import vnreal.algorithms.linkmapping.TwoDomainMCF;
import vnreal.algorithms.nodemapping.AvailableResourcesNodeMapping;
import vnreal.algorithms.nodemapping.MultiDomainAvailableResources;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class MultiDomainSimulation {
	private List<Domain> multiDomain;
	private ArrayList<VirtualNetwork> vns;
	private ArrayList<VirtualNetwork> mappedVNs;
	private ArrayList<VirtualNetwork> vnEvent;
	private ArrayList<VnEvent> events;
	private ArrayList<Metric> metrics;
	private double simulationTime = 50000.0;
	private double time = 0.0;
	private int accepted = 0;
	private int rejected = 0;
	private double lambda = 4.0/100.0;
	
	public MultiDomainSimulation() throws IOException{
		
		multiDomain = new ArrayList<Domain>();
		//int x,int y, file path, resource
		multiDomain.add(new Domain(0,0,"data/cost239", false));
		multiDomain.add(new Domain(1,0,"sndlib/abilene", false));

		MultiDomainUtil.staticInterLinks(multiDomain.get(0),multiDomain.get(1));
		
		vns = new ArrayList<VirtualNetwork>();
		for(int i=0;i<5;i++){
			VirtualNetwork vn = new VirtualNetwork(1,false);
			vn.alt2network("data/vir"+i);
			vn.addAllResource(true);
			vn.scale(2, 1);
			//System.out.println(vn);		//print vn
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
	
	public void runSimulation(String methodStr) throws IOException{
		for(VnEvent currentEvent : events){
			
			if(currentEvent.getFlag()==0){
				MultiDomainAvailableResources arnm = new MultiDomainAvailableResources(multiDomain,80);
				
				vnEvent.add(currentEvent.getConcernedVn());
				
				System.out.println("accepted : "+this.accepted+"\n");
				System.out.println("rejected : "+this.rejected+"\n");
				System.out.println(currentEvent.getConcernedVn());
				
				if(arnm.nodeMapping(currentEvent.getConcernedVn())){
					Map<VirtualNode, SubstrateNode> nodeMapping = arnm.getNodeMapping();
					System.out.println(nodeMapping);
					//System.out.println("node mapping succes, virtual netwotk "+j);
					
					//link mapping method
					AbstractMultiDomainLinkMapping method;
					switch (methodStr)
					{
					case "MultiDomainRanking" : 
						method = new MultiDomainRanking(multiDomain);
						break;
					case "Shen2014" : 
						method = new Shen2014(multiDomain);
						break;
					case "MultiDomainAsOneDomain" : 
						method = new MultiDomainAsOneDomain(multiDomain);
						break;
					case "TwoDomainMCF" :
						method = new TwoDomainMCF(multiDomain);
						break;
					case "AS_MCF" : 
						method = new AS_MCF(multiDomain);
						break;
					default : 
						System.out.println("The methode doesn't exist");
						method = null;
					}
					
					if(method.linkMapping(currentEvent.getConcernedVn(), nodeMapping)){
						this.accepted++;
						mappedVNs.add(currentEvent.getConcernedVn());
						
					}
					else{
						this.rejected++;
						System.out.println("link resource error, virtual network"); //TODO print vn id 
					}
					
					//reset virtual node domain value
					for(VirtualNode viNode : currentEvent.getConcernedVn().getVertices())
						viNode.setDomain(null);
					
				}
				else{
					this.rejected++;
					//System.out.println("node resource error, virtual network "+j);
				}
				//System.out.println("Duree d'execution :"+duree);
			}
			else{
				System.out.println("Liberation Ressources");
				NodeLinkDeletion.multiDomainFreeResource(currentEvent.getConcernedVn(), multiDomain);
			}
			System.out.println(multiDomain.get(0));
			System.out.println(multiDomain.get(1));
			/*
			for(Metric metric : metrics){ //write data to file
				metric.getFout().write(currentEvent.getAoDTime()+" " +metric.calculate()+"\n");
			}*/
			
		}
		/*
		for(Metric metric : metrics){
			metric.getFout().close();
		}*/	
		System.out.println(methodStr);
		System.out.println("accepted : "+this.accepted);
		System.out.println("rejected : "+this.rejected);
		
	}
	
	public void reset(){
		MultiDomainUtil.reset(this.multiDomain);
		this.accepted = 0;
		this.rejected = 0;
		mappedVNs = new ArrayList<VirtualNetwork>();
		vnEvent = new ArrayList<VirtualNetwork>();
	}
}
