package li.multiDomain.metrics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import li.simulation.MultiDomainSimulation;

public abstract class MetricMD {
	protected MultiDomainSimulation simulation;
	protected BufferedWriter fout;
	
	public BufferedWriter getFout() {
		return fout;
	}

	public void setSimulation(MultiDomainSimulation simulation) {
		this.simulation = simulation;
	}

	public MetricMD(MultiDomainSimulation simulation) throws IOException{
		this.simulation = simulation;
		this.fout = new BufferedWriter(new FileWriter("evaluationData/"+this.name()+".txt"));
	}
	
	public MetricMD(MultiDomainSimulation simulation, String method) throws IOException{
		this.simulation = simulation;
		this.fout = new BufferedWriter(new FileWriter("evaluationData/"+method+"_"+this.name()+".txt"));
	}
	
	public abstract String name();
	public abstract double calculate();
}
