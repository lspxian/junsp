package li.simulation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import li.event.VnEvent;
import li.gt_itm.Generator;
import li.multiDomain.AbstractMultiDomain;
import li.multiDomain.Domain;
import li.multiDomain.MultiDomainUtil;
import li.multiDomain.metrics.AcceptedRatioMD;
import li.multiDomain.metrics.CostMD;
import li.multiDomain.metrics.CostRevenueMD;
import li.multiDomain.metrics.CurrentLinkUtilisationMD;
import li.multiDomain.metrics.LinkUtilizationMD;
import li.multiDomain.metrics.MappedRevenueMD;
import li.multiDomain.metrics.MetricMD;
import li.multiDomain.metrics.LinkCostRevenueMD;
import vnreal.algorithms.AbstractMultiDomainLinkMapping;
import vnreal.algorithms.linkmapping.AS_MCF;
import vnreal.algorithms.linkmapping.AllPossibleMDRanking;
import vnreal.algorithms.linkmapping.MDasOD2;
import vnreal.algorithms.linkmapping.MultiDomainAsOneDomain;
import vnreal.algorithms.linkmapping.MultiDomainRanking;
import vnreal.algorithms.linkmapping.MultiDomainRanking2;
import vnreal.algorithms.linkmapping.MultiDomainRanking3;
import vnreal.algorithms.linkmapping.Shen2014;
import vnreal.algorithms.nodemapping.MultiDomainAvailableResources;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class Distribute3DVNE extends AbstractMultiDomain {

public Distribute3DVNE() throws IOException{
		this.simulationTime = 20000.0;
		multiDomain = new ArrayList<Domain>();
		//int x,int y, file path, resource
		/*-------3 domains example--------*/
		
	/*	multiDomain.add(new Domain(0,0,"sndlib/india35", true));
		multiDomain.add(new Domain(1,0,"sndlib/pioro40", true));
		multiDomain.add(new Domain(2,0,"sndlib/germany50", true));*/
		
		/*------use gt-itm to create random substrate network-----*/
		multiDomain.add(new Domain(0,0, true, 40, 0.11));
		multiDomain.add(new Domain(1,0, true, 40, 0.11));
		multiDomain.add(new Domain(2,0, true, 40, 0.11));


		/*--------static or random peering links--------*/
//		MultiDomainUtil.random2DInterLinks(multiDomain.get(0),multiDomain.get(1),0.9,0.07);
		MultiDomainUtil.random3DInterLinks(multiDomain,0.9,0.09);
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
		for(int i=100;i<400;i++){
			VirtualNetwork vn = new VirtualNetwork();
			vn.alt2network("data/vir"+i);
//			vn.alt2network("data/vir"+new Random().nextInt(500));
//			vn.alt2network("data/vhr2");
			vn.addAllResource(true);
			vn.scale(3, 1);
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
			vn.alt2network("./gt-itm/vir");
			vn.addAllResource(true);
			vn.reconfigPositionMD(multiDomain);
//			vn.scale(3, 1);		//scale a [100,100] vn to [300,100]
//			vn.reconfigResource(multiDomain);
			
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
		metrics.add(new CostMD(this, methodStr,lambda));
		metrics.add(new CostRevenueMD(this,methodStr,lambda));
		metrics.add(new LinkCostRevenueMD(this,methodStr,lambda));
		
		for(int i=0;i<multiDomain.size();i++){
			System.out.println(multiDomain.get(i));
		}
		
		for(VnEvent currentEvent : events){
			
			System.out.println("/------------------------------------/");
			System.out.println("New event at time :	"+currentEvent.getAoDTime()+" for vn:"+currentEvent.getConcernedVn().getId());
			System.out.println("At this moment, accepted:"+this.accepted+" rejected:"+this.rejected);

			if(currentEvent.getFlag()==0){
				MultiDomainAvailableResources arnm = new MultiDomainAvailableResources(multiDomain,1);
				
				if(arnm.nodeMapping(currentEvent.getConcernedVn())){
					System.out.println(currentEvent.getConcernedVn());
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
					case "MultiDomainRanking2" : 
						method = new MultiDomainRanking2(multiDomain);
						break;
					case "MultiDomainRanking3" : 
						method = new MultiDomainRanking3(multiDomain);
						break;
					case "AllPossible" : 
						method = new AllPossibleMDRanking(multiDomain);
						break;
					default : 
						System.out.println("The methode doesn't exist");
						method = null;
					}
					
					if(method.linkMapping(currentEvent.getConcernedVn(), nodeMapping)){
						this.accepted++;
						mappedVNs.add(currentEvent.getConcernedVn());
						this.totalCost=this.totalCost+currentEvent.getConcernedVn().getTotalCost(multiDomain);
						this.linkCost=this.linkCost+currentEvent.getConcernedVn().getLinkCost(multiDomain);
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
				System.out.println(currentEvent.getConcernedVn());
				NodeLinkDeletion.multiDomainFreeResource(currentEvent.getConcernedVn(), multiDomain);
			}
		/*	
			for(int i=0;i<multiDomain.size();i++){
				System.out.println(multiDomain.get(i));
			}*/
			
			for(MetricMD metric : metrics){ //write data to file TODO
				double value = metric.calculate();
				System.out.println(metric.name()+" "+value);
				metric.getFout().write(currentEvent.getAoDTime()+" " +value+"\n");
			}
			
		}
		
		System.out.println("*-----"+methodStr+" resume------------*");
		System.out.println("accepted : "+this.accepted);
		System.out.println("rejected : "+this.rejected);
		
		FileWriter writer = new FileWriter("disResult.txt",true);
		writer.write("*----lambda="+this.lambda+"--"+methodStr+"----*\n");
		writer.write("accepted : "+this.accepted+"\n");
		writer.write("rejected : "+this.rejected+"\n");
		for(MetricMD metric : metrics){ 
			writer.write(metric.name()+" "+metric.calculate()+"\n");
		}
		writer.write("\n");
		writer.close();
		
		for(MetricMD metric : metrics){
			metric.getFout().close();
		}
	}
	
	public void reset(){
		MultiDomainUtil.reset(this.multiDomain);
		this.accepted = 0;
		this.rejected = 0;
		this.totalCost=0.0;
		this.linkCost=0.0;
		mappedVNs = new ArrayList<VirtualNetwork>();
		metrics = new ArrayList<MetricMD>();
	}
}
