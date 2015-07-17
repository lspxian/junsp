package li.evaluation.metrics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import cherif.Simulation;

public abstract class Metric{
	
	protected Simulation simulation;
	protected BufferedWriter fout;
	
	public BufferedWriter getFout() {
		return fout;
	}

	public void setSimulation(Simulation simulation) {
		this.simulation = simulation;
	}

	public Metric(Simulation simulation) throws IOException{
		this.simulation = simulation;
		this.fout = new BufferedWriter(new FileWriter("evaluationData/"+this.name()+".txt"));
	}
	
	public abstract String name();
	public abstract double calculate();
}
