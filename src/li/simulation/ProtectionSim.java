package li.simulation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import li.evaluation.metrics.AcceptedRatioL;
import li.evaluation.metrics.AffectedRevenue;
import li.evaluation.metrics.Affected_VN_Number;
import li.evaluation.metrics.AverageAffectedVNRatio;
import li.evaluation.metrics.AverageProbability;
import li.evaluation.metrics.CurrentLinkUtilisationL;
import li.evaluation.metrics.LinkUtilizationL;
import li.evaluation.metrics.MappedRevenueL;
import li.evaluation.metrics.MaxProbability;
import li.evaluation.metrics.Metric;
import li.evaluation.metrics.PrimaryPercentage;
import li.event.FailureEvent;
import li.event.NetEvent;
import li.event.VnEvent;
import li.gt_itm.DrawGraph;
import li.gt_itm.Generator;
import probabilityBandwidth.ProbaHeuristic4;
import probabilityBandwidth.ShortestPathBW;
import protectionProba.AbstractBackupMapping;
import protectionProba.BestEffortBackup;
import protectionProba.CSP_PE;
import protectionProba.CSP_Proba;
import protectionProba.ConstraintSPLocalShare;
import protectionProba.MaxFlowBackupVF;
import protectionProba.MaxFlowBackupVF2;
import protectionProba.ProtectionEnabledPrimaryBW;
import protectionProba.ProtectionEnabledPrimaryMapping;
import protectionProba.ProtectionEnabledPrimaryMapping2;
import protectionProba.SPWithoutBackupVF;
import protectionProba.SPWithoutBackupVF2;
import protectionProba.ShortestPathBackupVF;
import protectionProba.ShortestPathBackupVF2;
import vnreal.algorithms.AbstractLinkMapping;
import vnreal.algorithms.linkmapping.MultiCommodityFlow;
import vnreal.algorithms.nodemapping.AvailableResourcesNodeMapping;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.mapping.Mapping;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.resources.BandwidthResource;

public class ProtectionSim extends AbstractSimulation {
	
	public ProtectionSim(){
		
		simulationTime = 50000.0;
		try {
			while(true){
				this.sn=new SubstrateNetwork(); //undirected by default 
				boolean connect=true;
				Generator.createSubNet();
				sn.alt2network("./gt-itm/sub");
//				sn.alt2network("data/cost239");
//				sn.alt2network("sndlib/germany50");
				for(SubstrateNode snode:sn.getVertices()){
					if(sn.getNeighborCount(snode)<=1){
						connect=false;
						break;
					}
				}
				if(connect==true)	break;
			}
			
//			DrawGraph dg = new DrawGraph(sn);
//			dg.draw();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sn.addAllResource(true);
		sn.addInfiniteResource();
		
		//random failure event
		failureEvents=new ArrayList<NetEvent>();
		for(SubstrateLink sl : sn.getEdges()){
			time=MiscelFunctions.negExponential(sl.getProbability());
			while(time<simulationTime){
				failureEvents.add(new FailureEvent(time,sl));
				time+=MiscelFunctions.negExponential(sl.getProbability());
			}
		}
		
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
		events = new ArrayList<VnEvent>();
		vns = new ArrayList<VirtualNetwork>();
		for(int i=0;i<1000;i++){
			VirtualNetwork vn = new VirtualNetwork();
			vn.alt2network("data/vir"+i);
			vn.addAllResource(true);
			vn.reconfigPositon(sn);
			//System.out.println(vn);		//print vn
			vns.add(vn);
		}
		for(int i=0;i<20;i++){
			events.add(new VnEvent(vns.get(i),i,0));
			events.add(new VnEvent(vns.get(i),100+i,1));
		}
		
		Collections.sort(events);*/
		/*
		for(int i=0;(time <=simulationTime)	&& (i <vns.size());i++){
			events.add(new VnEvent(vns.get(i),time,0)); //arrival event
			double departure = time+vns.get(i).getLifetime();
			if(departure<=simulationTime)
				events.add(new VnEvent(vns.get(i),departure,1)); // departure event
			time+=MiscelFunctions.negExponential(lambda/100.0); //generate next vn arrival event
		}*/
		
		/*---------random virtual network-----------*/
		
		events = new ArrayList<VnEvent>();
		while(time<simulationTime){
			VirtualNetwork vn = new VirtualNetwork();
			Generator.createVirNet();
			vn.alt2network("./gt-itm/vir");
			vn.addAllResource(true);
			vn.reconfigPositon(sn);
			
			double departureTime = time+vn.getLifetime();
			events.add(new VnEvent(vn,time,0)); //arrival event
			if(departureTime<=simulationTime)
				events.add(new VnEvent(vn,departureTime,1)); // departure event
			time+=MiscelFunctions.negExponential(lambda/200.0); //generate next vn arrival event
		}
		Collections.sort(events);
		
		this.netEvents = new ArrayList<NetEvent>();
		this.netEvents.addAll(events);
		
		//deterministic failure event
	/*	for(int t=200;t<=simulationTime;t=t+200){
			SubstrateLink fsl=randomFailure(this.sn);
			netEvents.add(new FailureEvent(t,fsl));
		}
		Collections.sort(this.netEvents);*/
		
		this.netEvents.addAll(failureEvents);
		Collections.sort(this.netEvents);
		
		//add metric
		metrics = new ArrayList<Metric>();
		metricsProba = new ArrayList<Metric>();
		mappedVNs = new ArrayList<VirtualNetwork>();
		this.currentVNs = new ArrayList<VirtualNetwork>();
		this.probability = new LinkedHashMap<VirtualNetwork,Double>();
		this.affectedRatio = new ArrayList<Double>();
		
	}
	public void runSimulation(String methodStr,String backupStr) throws IOException{
		System.out.println(this.sn.probaToString());
		//add metrics
		metrics.add(new AcceptedRatioL(this));
		metrics.add(new LinkUtilizationL(this));
		metrics.add(new CurrentLinkUtilisationL(this));
		metrics.add(new PrimaryPercentage(this));
		metrics.add(new MappedRevenueL(this));
		metrics.add(new AverageProbability(this));
		metrics.add(new MaxProbability(this));
		metricsProba.add(new AverageAffectedVNRatio(this));
		metricsProba.add(new Affected_VN_Number(this));
		metricsProba.add(new AffectedRevenue(this));
		
		//Primary link mapping
		AbstractLinkMapping method;
		switch (methodStr)
		{
		case "MCF" :
			method = new MultiCommodityFlow(sn);
			break;
		case "ProtectionEnabledMCF" :
			method = new ProtectionEnabledPrimaryMapping(sn);
			break;
		case "ProtectionEnabledMCF2" :
			method = new ProtectionEnabledPrimaryMapping2(sn);
			break;
		case "ProtectionEnabledBW" :
			method = new ProtectionEnabledPrimaryBW(sn);
			break;
		case "ShortestPathBW" :
			method = new ShortestPathBW(sn);
			break;
		case "SPBWConfig" :
			method = new ShortestPathBW(sn);
			configBW(0.99);
			break;
		case "ProbaHeuristic" :
			method = new ProbaHeuristic4(sn);	//use 4
			break;
		case "SPWithoutBackupVF" :
			method = new SPWithoutBackupVF(sn);
			break;
		case "ShortestPathBackupVF" :
			method = new ShortestPathBackupVF(sn);
			break;
		case "SPWithoutBackupVF2" :
			method = new SPWithoutBackupVF2(sn);
			break;
		case "ShortestPathBackupVF2" :
			method = new ShortestPathBackupVF2(sn);
			break;
		case "MaxFlowBackupVF" :
			method = new MaxFlowBackupVF(sn);
			break;
		case "MaxFlowBackupVF2" :
			method = new MaxFlowBackupVF2(sn);
			break;
		default : 
			System.out.println("The methode doesn't exist");
			method = null;
		}
			
		//backup here
		AbstractBackupMapping backupMethod;
		switch(backupStr)
		{
		case "BestEffort":
			backupMethod=new BestEffortBackup(sn);
			break;
		case "ConstraintSP":
			backupMethod=new ConstraintSPLocalShare(sn);
			break;
		case "CSP_PE":
			backupMethod=new CSP_PE(sn);
			break;
		case "CSP_Proba":
			backupMethod=new CSP_Proba(sn);
			break;
		case "included":
			backupMethod = null;
			break;
		default:
			System.out.println("The methode doesn't exist");
			backupMethod = null;
		}
		
		for(NetEvent currentEvent : this.netEvents){
			if(currentEvent instanceof VnEvent){
				VnEvent cEvent = (VnEvent) currentEvent;
				System.out.println("/------------------------------------/");
				System.out.println("New event at time :	"+currentEvent.getAoDTime()+" for vn:"+cEvent.getConcernedVn().getId());
				System.out.println("At this moment, accepted:"+this.accepted+" rejected:"+this.rejected);
				System.out.print("Current vn : \n"+cEvent.getConcernedVn()+"\n");
				
				if(cEvent.getFlag()==0){
					AvailableResourcesNodeMapping arnm = new AvailableResourcesNodeMapping(sn,1,true,false);
//					CordinatedNodeLinkMapping arnm = new CordinatedNodeLinkMapping(sn);
					System.out.println("Operation : Mapping");
					method.resetMapping();
					
					if(arnm.nodeMapping(cEvent.getConcernedVn())){
						Map<VirtualNode, SubstrateNode> nodeMapping = arnm.getNodeMapping();
						System.out.println("node mapping succes : "+nodeMapping);
						
						if(method.linkMapping(cEvent.getConcernedVn(), nodeMapping)){
							System.out.println("Primary link mapping done");
//							System.out.println(this.sn.probaToString());
							
							if((backupStr=="")||(backupStr=="included")){
								if(backupStr=="")
									System.out.println("no backup stage, primary only done.");
								this.currentVNs.add(cEvent.getConcernedVn());
								this.accepted++;
								mappedVNs.add(cEvent.getConcernedVn());
								this.totalCost=this.totalCost+cEvent.getConcernedVn().getTotalCost(sn);
								System.out.println("current probability : "+method.getProbability());
								this.probability.put(cEvent.getConcernedVn(), method.getProbability());
							}
							else if(backupMethod.linkMapping(cEvent.getConcernedVn(), method.getMapping())){
								System.out.println("Backup link mapping done");
								this.currentVNs.add(cEvent.getConcernedVn());
								this.accepted++;
								mappedVNs.add(cEvent.getConcernedVn());
								this.totalCost=this.totalCost+cEvent.getConcernedVn().getTotalCost(sn);
								System.out.println("current probability : "+backupMethod.getProbability());
								this.probability.put(cEvent.getConcernedVn(), backupMethod.getProbability());
								//TODO maybe backup metrics' code
//								System.out.println(sn.probaToString());
							}
							else{
								this.rejected++;
//								System.out.println(sn.probaToString());
								System.out.println("Backup link mapping resource error");
							}
						}
						else{
							this.rejected++;
//							System.out.println(sn.probaToString());
							System.out.println("Primary link mapping resource error");
						}
						
					}
					else{
						if(currentEvent.getAoDTime()>=0)
							this.rejected++;
//						System.out.println("node resource error, virtual network "+j);
					}
					
//					System.out.println(sn.probaToString());
					for(Metric metric : metrics){ //write data to file
						double value = metric.calculate();
						System.out.println(metric.name()+" "+value);
						metric.getFout().write(currentEvent.getAoDTime()+" " +value+"\n");
					}			
					
				}
				else{
					System.out.println("Operation : Liberation Ressources");
					if(this.currentVNs.contains(cEvent.getConcernedVn())){
						if(methodStr=="MaxFlowBackupVF")
							((MaxFlowBackupVF) method).freeMaxFlow(cEvent.getConcernedVn(), sn);
						if(methodStr=="MaxFlowBackupVF2")
							((MaxFlowBackupVF2) method).freeMaxFlow(cEvent.getConcernedVn(), sn);
						if(backupStr!="")
							NodeLinkDeletion.FreeResourceBackup(cEvent.getConcernedVn(), sn, true);
						NodeLinkDeletion.freeResource(cEvent.getConcernedVn(), sn);
						this.currentVNs.remove(cEvent.getConcernedVn());					
					}
				}
			}
			else if(currentEvent instanceof FailureEvent){
				failures++;
				Set<VirtualNetwork> affectedNet = new HashSet<VirtualNetwork>();
				FailureEvent fEvent = (FailureEvent) currentEvent;
				BandwidthResource bw = (BandwidthResource)fEvent.getFailureLink().getBandwidthResource();
				for(Mapping m :bw.getMappings()){
					if(!m.isProtection()){
						VirtualLink vl=(VirtualLink)m.getDemand().getOwner();
						for(VirtualNetwork vn : this.currentVNs){
							if(vn.containsEdge(vl))
								affectedNet.add(vn);
						}						
					}
				}
				
				this.affected+=affectedNet.size();
				for(VirtualNetwork vn : affectedNet){
					this.affectedRevenue+=vn.calculateRevenue();					
				}
				double ratio;
				if(this.currentVNs.isEmpty())
					ratio=0.0;
				else ratio = (double)affectedNet.size()/this.currentVNs.size();
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
		System.out.println(this.sn.probaToString());
		if(methodStr=="MaxFlowBackupVF")
			System.out.println(((MaxFlowBackupVF) method).getMaxflow());
		
		FileWriter writer = new FileWriter("result.txt",true);
		writer.write("*----lambda="+this.lambda+"--"+methodStr+"-"+backupStr+"----*\n");
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
	@Override
	public void runSimulation(String methodStr) throws IOException {
		// TODO Auto-generated method stub
	}
	
	public void configBW(double percent){
		for(SubstrateNode snode:sn.getVertices()){
			if(sn.getNeighborCount(snode)==2){
				for(SubstrateNode dnode:sn.getNeighbors(snode)){
					SubstrateLink sl=sn.findEdge(snode, dnode);
					BandwidthResource bwr=sl.getBandwidthResource();
					double primaryCap=MiscelFunctions.roundThreeDecimals(bwr.getBandwidth()*percent);
					bwr.setPrimaryCap(primaryCap);
//					bwr.setPrimaryCap(1000);
					double backupCap=MiscelFunctions.roundThreeDecimals(bwr.getBandwidth()*(1-percent));
					bwr.setBackupCap(backupCap);
				}
			}
		}
	}
}
