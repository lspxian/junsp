package li.simulation;

import java.io.IOException;
import java.util.ArrayList;
import li.evaluation.metrics.Metric;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualNetwork;

public abstract class AbstractSimulation {
	protected SubstrateNetwork sn;
	protected ArrayList<VirtualNetwork> vns;
	protected ArrayList<VirtualNetwork> mappedVNs;
	protected double totalCost;
	protected ArrayList<VnEvent> events;
	protected ArrayList<Metric> metrics;
	protected double simulationTime = 20000.0;
	protected double time = 0.0;
	protected int accepted = 0;
	protected int rejected = 0;
	protected int lambda = 4;
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

	public abstract void initialize(int lambda) throws IOException;
	public abstract void runSimulation(String methodStr) throws IOException;
	public abstract void reset();
	
	
}
