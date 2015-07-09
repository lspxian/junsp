package cherif;

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
public class VnEvent  implements java.lang.Comparable
{
private VirtualNetwork concernedVn;
private double AoDTime=0.0;	 //Arrival or Departure Time 
private int flag=0;			//if flag=0 then arrival else departure
public VnEvent (VirtualNetwork concernedVn,double AoDTime,int flag)
	{
		this.concernedVn=concernedVn;
		this.AoDTime=AoDTime;
		this.flag=flag;
	}
public VirtualNetwork getConcernedVn()
	{
		return concernedVn;
	}
public double getAoDTime()
	{
		return AoDTime;
	}
public int getFlag()
	{
		return flag;
	}
public void setConcernedVn(VirtualNetwork vn)
	{
		concernedVn = vn;
	}
public void setAoDTime (double AoDTime)
	{
		this.AoDTime = AoDTime;
	}
public void setFlag(int flag)
	{
		this.flag = flag;
	}
public int compareTo(Object obj)
{
	double time1 = ((VnEvent) obj).getAoDTime();
	double time2 = this.getAoDTime();
	if(time1>time2) return -1;
	else if (time1 == time2) return 0;
	else  return 1;
}

/*public void Mapping(SubstrateNetwork sn,int i)
{
		System.out.println("virtual network "+i+": \n"+this);
	//node mapping
		AvailableResourcesNodeMapping arnm = new AvailableResourcesNodeMapping(sn,50,true,false);
	
		if(arnm.nodeMapping(this))
		{
			System.out.println("node mapping succes, virtual netwotk "+i);
		}
		else{
			System.out.println("node resource error, virtual network "+i);
   }*/


}
