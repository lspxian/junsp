package li.multiDomain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import li.event.VnEvent;
import li.multiDomain.metrics.MetricMD;
import vnreal.network.virtual.VirtualNetwork;

public abstract class AbstractMultiDomain {
	protected List<Domain> multiDomain;
	protected ArrayList<VirtualNetwork> vns;
	protected ArrayList<VirtualNetwork> mappedVNs;
	protected double totalCost;
	protected double linkCost=0;
	protected ArrayList<VnEvent> events;
	protected ArrayList<MetricMD> metrics;
	protected double simulationTime;
	protected double time = 0.0;
	protected int accepted = 0;
	protected int rejected = 0;
	protected int lambda = 3;
	
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
	public double getTotalCost() {
		return totalCost;
	}
	public double getLinkCost() {
		return linkCost;
	}
	public abstract void initialize(int lambda) throws IOException;
	public abstract void runSimulation(String methodStr) throws IOException;
	public abstract void reset();
}
