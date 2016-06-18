package li.evaluation.metrics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import li.simulation.AbstractSimulation;
import li.simulation.ProbabilitySimulationMain;

public abstract class Metric{
	
	protected AbstractSimulation simulation;
	protected BufferedWriter fout;
	
	public BufferedWriter getFout() {
		return fout;
	}

	public void setSimulation(AbstractSimulation simulation) {
		this.simulation = simulation;
	}

	public Metric(AbstractSimulation simulation) throws IOException{
		this.simulation = simulation;
		this.fout = new BufferedWriter(new FileWriter("evaluationData/"+this.name()+".txt"));
	}
	
	public Metric(AbstractSimulation simulation, String method) throws IOException{
		this.simulation = simulation;
		this.fout = new BufferedWriter(new FileWriter("evaluationData/"+method+this.name()+".txt"));
	}
	//TODO
	public Metric(AbstractSimulation simulation, String method, int lambda) throws IOException{
		this.simulation = simulation;
		this.fout = new BufferedWriter(new FileWriter("evaluationData/"+method+"_"+this.name()+"_l"+lambda+"_c"+ProbabilitySimulationMain.c+".txt"));
//		this.fout = new BufferedWriter(new FileWriter("evaluationData/"+method+"_"+this.name()+"_l"+lambda+".txt"));
	}
	
	public abstract String name();
	public abstract double calculate();
}
