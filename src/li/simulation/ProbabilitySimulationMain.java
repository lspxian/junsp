package li.simulation;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProbabilitySimulationMain {
	
	public static int c;
	
	public static void main(String[] args) throws IOException {
		FileWriter writer = new FileWriter("resultat.txt",true);
		writer.write("/----------------New Simulation--------------/\n");
		writer.write("Simulation time : "+new SimpleDateFormat().format(new Date())+"\n");
		writer.close();
			
		SteinerTreeProbabilitySimulation simulation = new SteinerTreeProbabilitySimulation();
		
		//System.out.println(simulation.getSubstrateNetwork());
			
		for(c=0;c<1;c++){
	//		PrintStream tmp = new PrintStream(new FileOutputStream("tmp.txt"));
	//		System.setOut(tmp);
			writer = new FileWriter("resultat.txt",true);
			writer.write("Number:"+c+"\n");
			writer.close();
		
			for(int i=3;i<7;i++){
				simulation.initialize(i);
				/*
				PrintStream exact = new PrintStream(new FileOutputStream("res/ExactILP_l"+i+"_c"+c+".txt"));
				System.setOut(exact);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("Exact");
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.reset();
				
				PrintStream ta = new PrintStream(new FileOutputStream("res/Takahashi_l"+i+"_c"+c+".txt"));
				System.setOut(ta);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("Takahashi");
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.reset();*/
				
				PrintStream kmb = new PrintStream(new FileOutputStream("res/KMB1981_l"+i+"_c"+c+".txt"));
				System.setOut(kmb);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("KMB1981");
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.reset();
				
				System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
				System.out.println("job done");
			}
		}
		writer = new FileWriter("resultat.txt",true);
		writer.write("/---------------Simulation finished!---------------/\n");
		writer.write("Time : "+new SimpleDateFormat().format(new Date())+"\n\n");
		writer.close();
	}

}
