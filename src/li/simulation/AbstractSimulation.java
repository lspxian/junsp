package li.simulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import li.evaluation.metrics.Metric;
import li.event.NetEvent;
import li.event.VnEvent;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualNetwork;

public abstract class AbstractSimulation {
	protected SubstrateNetwork sn;
	protected ArrayList<VirtualNetwork> vns;
	protected ArrayList<VirtualNetwork> mappedVNs;
	protected double totalCost;
	protected ArrayList<VnEvent> events;
	protected ArrayList<Metric> metrics;
	protected ArrayList<Metric> metricsProba;
	protected double simulationTime;
	protected double time = 0.0;
	protected int accepted = 0;
	protected int rejected = 0;
	protected int protectedVNs=0;
	protected int lambda;
	protected ArrayList<VirtualNetwork> currentVNs ;
	protected Map<VirtualNetwork, Double> probability; 
	protected ArrayList<NetEvent> netEvents;
	protected ArrayList<NetEvent> failureEvents;
	protected int affected;
	protected double affectedRevenue;
	protected ArrayList<Double> affectedRatio;
	protected int failures;
	protected long timeDifference;
	
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
	public Map<VirtualNetwork, Double> getProbability() {
		return probability;
	}
	public int getAccepted() {
		return accepted;
	}

	public int getProtectedVNs() {
		return protectedVNs;
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
	public double getTotalCost() {
		return totalCost;
	}

	public double getSimulationTime() {
		return simulationTime;
	}
	
	public int getLambda() {
		return lambda;
	}
	
	public long getTimeDifference() {
		return timeDifference;
	}
	public abstract void initialize(int lambda) throws IOException;
	public abstract void runSimulation(String methodStr) throws IOException;
	public abstract void reset();
	
	
}
