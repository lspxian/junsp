package li.multiDomain.metrics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import li.multiDomain.AbstractMultiDomain;
import li.simulation.SimulationTest;

public abstract class MetricMD {
	protected AbstractMultiDomain simulation;
	protected BufferedWriter fout;
	
	public BufferedWriter getFout() {
		return fout;
	}

	public void setSimulation(AbstractMultiDomain simulation) {
		this.simulation = simulation;
	}

	public MetricMD(AbstractMultiDomain simulation) throws IOException{
		this.simulation = simulation;
		this.fout = new BufferedWriter(new FileWriter("evaluationData/"+this.name()+".txt"));
	}
	
	public MetricMD(AbstractMultiDomain simulation, String method) throws IOException{
		this.simulation = simulation;
		this.fout = new BufferedWriter(new FileWriter("evaluationData/"+method+"_"+this.name()+".txt"));
	}
	
	public MetricMD(AbstractMultiDomain simulation, String method, int lambda) throws IOException{
		this.simulation = simulation;
		this.fout = new BufferedWriter(new FileWriter("evaluationData/"+method+"_"+this.name()+"_l"+lambda+"_c"+SimulationTest.c+".txt"));
	}
	
	public abstract String name();
	public abstract double calculate();
}
