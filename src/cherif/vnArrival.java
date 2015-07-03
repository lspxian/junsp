package cherif;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mulavito.algorithms.shortestpath.disjoint.SuurballeTarjan;
import mulavito.algorithms.shortestpath.ksp.LocalBypass;
import mulavito.algorithms.shortestpath.ksp.Yen;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import vnreal.algorithms.linkmapping.KShortestPath;
import vnreal.algorithms.linkmapping.KShortestPathLinkMapping;
import vnreal.algorithms.linkmapping.PathSplittingVirtualLinkMapping;
import vnreal.algorithms.linkmapping.SOD_BK;
import vnreal.algorithms.linkmapping.UnsplittingLPCplex;
import vnreal.algorithms.linkmapping.UnsplittingVirtualLinkMapping;
import vnreal.algorithms.nodemapping.AvailableResourcesNodeMapping;
import vnreal.algorithms.utils.Consts;
import vnreal.algorithms.utils.dataSolverFile;
import vnreal.demands.BandwidthDemand;
import vnreal.demands.CpuDemand;
import vnreal.evaluations.metrics.AcceptedVnrRatio;
import vnreal.evaluations.metrics.MappedRevenue;
import vnreal.evaluations.metrics.TotalRevenue;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
import vnreal.resources.BandwidthResource;
public class vnArrival {
	public int i=0;
	public double time=0.0;
	public double poisson(double average) {
		Random r = new Random();
		if (average<= 0)
		System.out.println("negexp: First parameter is lower" +" than zero");
		return -Math.log(r.nextDouble())/average;
		}
	public void Map(List<VirtualNetwork> v,SubstrateNetwork sn,double simulationTime, double a)
	{
		
		while((time<simulationTime))
		{
			System.out.println("virtual network "+i+": \n"+v.get(i));
		//node mapping
			AvailableResourcesNodeMapping arnm = new AvailableResourcesNodeMapping(sn,50,true,false);
		
			if(arnm.nodeMapping(v.get(i)))
			{
				System.out.println("node mapping succes, virtual netwotk "+i);
			}
			else{
				System.out.println("node resource error, virtual network "+i);
				
				}
			this.time = this.time+this.poisson(a);
			i++;
			if(i==v.size())
				break;
	   }
    }
 }