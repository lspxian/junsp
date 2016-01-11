package li.simulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import li.evaluation.metrics.Metric;
import li.gt_itm.Generator;
import li.multiDomain.Domain;
import li.multiDomain.MultiDomainUtil;
import li.multiDomain.metrics.AcceptedRatioMD;
import li.multiDomain.metrics.CurrentLinkUtilisationMD;
import li.multiDomain.metrics.LinkUtilizationMD;
import li.multiDomain.metrics.MappedRevenueMD;
import li.multiDomain.metrics.MetricMD;
import main.MultiDomainAlgoTest;
import vnreal.algorithms.AbstractLinkMapping;
import vnreal.algorithms.AbstractMultiDomainLinkMapping;
import vnreal.algorithms.linkmapping.AS_MCF;
import vnreal.algorithms.linkmapping.MDasOD2;
import vnreal.algorithms.linkmapping.MultiDomainAsOneDomain;
import vnreal.algorithms.linkmapping.MultiDomainRanking;
import vnreal.algorithms.linkmapping.MultiDomainRanking2;
import vnreal.algorithms.linkmapping.PathSplittingVirtualLinkMapping;
import vnreal.algorithms.linkmapping.Shen2014;
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
	private ArrayList<VnEvent> events;
	private ArrayList<MetricMD> metrics;
	private double simulationTime = 7000.0;
	private double time = 0.0;
	private int accepted = 0;
	private int rejected = 0;
	private int lambda = 3;
	
	public List<Domain> getMultiDomain() {
		return multiDomain;
	}

	public int setLambda() {
		return lambda;
	}
	public int getLambda() {
		return lambda;
	}

	public int getAccepted() {
		return accepted;
	}

	public int getRejected() {
		return rejected;
	}

	public ArrayList<VirtualNetwork> getVns() {
		return vns;
	}

	public ArrayList<VirtualNetwork> getMappedVNs() {
		return mappedVNs;
	}

	public MultiDomainSimulation() throws IOException{
		
		multiDomain = new ArrayList<Domain>();
		//int x,int y, file path, resource
		multiDomain.add(new Domain(0,0,"sndlib/india35", true));
		multiDomain.add(new Domain(1,0,"sndlib/pioro40", true));
		//use gt-itm to create net
//		multiDomain.add(new Domain(0,0, true));
//		multiDomain.add(new Domain(1,0, true));

		MultiDomainUtil.staticInterLinks(multiDomain.get(0),multiDomain.get(1));
//		MultiDomainUtil.randomInterLinks(multiDomain);
		
		
	}
	
	public void initialize(int lambda) throws IOException{
		this.time=0.0;
		this.accepted=0;
		this.rejected=0;
		this.lambda=lambda;
/*		
		vns = new ArrayList<VirtualNetwork>();
		for(int i=100;i<400;i++){
			VirtualNetwork vn = new VirtualNetwork();
//			vn.alt2network("data/vir"+i);
			vn.alt2network("data/vir"+new Random().nextInt(500));
//			vn.alt2network("data/vhr2");
			vn.addAllResource(true);
//			vn.scale(2, 1);
			vn.myExtend();
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
		
		events = new ArrayList<VnEvent>();
		while(time<simulationTime){
			VirtualNetwork vn = new VirtualNetwork();
			Generator.createVirNet();
			vn.alt2network("./gt-itm/sub");
			vn.addAllResource(true);
//			vn.scale(2, 1);
			vn.myExtend();
			
			double departureTime = time+vn.getLifetime();
			events.add(new VnEvent(vn,time,0)); //arrival event
			if(departureTime<=simulationTime)
				events.add(new VnEvent(vn,departureTime,1)); // departure event
			time+=MiscelFunctions.negExponential(lambda/100.0); //generate next vn arrival event
		}
		Collections.sort(events);
		
		//add metric
		metrics = new ArrayList<MetricMD>();
		mappedVNs = new ArrayList<VirtualNetwork>();
	}
	
	public void runSimulation(String methodStr) throws IOException{
		//add metrics
		metrics.add(new AcceptedRatioMD(this, methodStr,lambda));
		metrics.add(new LinkUtilizationMD(this, methodStr,lambda));
		metrics.add(new CurrentLinkUtilisationMD(this, methodStr,lambda));
		metrics.add(new MappedRevenueMD(this, methodStr,lambda));
		
		for(VnEvent currentEvent : events){
			
			if(currentEvent.getFlag()==0){
				MultiDomainAvailableResources arnm = new MultiDomainAvailableResources(multiDomain,30);
				
				System.out.println("/------------------------------------/");
				System.out.println("New event at time :	"+currentEvent.getAoDTime()+" for vn:"+currentEvent.getConcernedVn().getId());
				System.out.println("At this moment, accepted:"+this.accepted+" rejected:"+this.rejected);
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
					case "MDasOD2" : 
						method = new MDasOD2(multiDomain);
						break;
					case "MultiDomainRanking2" : 
						method = new MultiDomainRanking2(multiDomain);
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
//						System.out.println(multiDomain.get(0));
//						System.out.println(multiDomain.get(1));
					}
					else{
						this.rejected++;
						System.out.println("link resource error, virtual network"); 
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
//				System.out.println(currentEvent.getConcernedVn());
				NodeLinkDeletion.multiDomainFreeResource(currentEvent.getConcernedVn(), multiDomain);
			}
//			System.out.println(multiDomain.get(0));
//			System.out.println(multiDomain.get(1));
			
			for(MetricMD metric : metrics){ //write data to file TODO
				double value = metric.calculate();
				System.out.println(metric.name()+" "+value);
				metric.getFout().write(currentEvent.getAoDTime()+" " +value+"\n");
			}
			
		}
		
//		System.out.println(multiDomain.get(0));
//		System.out.println(multiDomain.get(1));
		
		System.out.println("*-----"+methodStr+" resume------------*");
		System.out.println("accepted : "+this.accepted);
		System.out.println("rejected : "+this.rejected);
//		for(MetricMD metric : metrics){ 
//			System.out.println(metric.name()+" "+metric.calculate());
//		}
		
		for(MetricMD metric : metrics){
			metric.getFout().close();
		}
	}
	
	public void reset(){
		MultiDomainUtil.reset(this.multiDomain);
		this.accepted = 0;
		this.rejected = 0;
		mappedVNs = new ArrayList<VirtualNetwork>();
		metrics = new ArrayList<MetricMD>();
	}
}
