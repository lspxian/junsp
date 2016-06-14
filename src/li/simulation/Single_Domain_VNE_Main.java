package li.simulation;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Single_Domain_VNE_Main {

public static int c;
	
	public static void main(String[] args) throws IOException {
		FileWriter writer = new FileWriter("result.txt",true);
		writer.write("/----------------New Simulation--------------/\n");
		writer.close();
		for(c=0;c<1;c++){
			writer = new FileWriter("result.txt",true);
			writer.write("Number:"+c+"\n");
			AbstractSimulation simulation = new Single_Domain_VNE_Simulation();
			System.out.println("Substrate Network : v "+
							simulation.getSubstrateNetwork().getVertexCount()+" e "+
							simulation.getSubstrateNetwork().getEdgeCount()+"\n");
			writer.write("Substrate Network : v "+
					simulation.getSubstrateNetwork().getVertexCount()+" e "+
					simulation.getSubstrateNetwork().getEdgeCount()+"\n");
			writer.close();
			
			for(int i=4;i<5;i++){
				simulation.initialize(i);
				
				PrintStream ConstraintShortestPath = new PrintStream(new FileOutputStream("res/ConstraintShortestPath_l"+i+"_c"+c+".txt"));
				System.setOut(ConstraintShortestPath);
				writeCurrentTime();
				simulation.runSimulation("ConstraintShortestPath");
				simulation.reset();
				/*
				PrintStream KShortestPath = new PrintStream(new FileOutputStream("res/KShortestPath_l"+i+"_c"+c+".txt"));
				System.setOut(KShortestPath);
				writeCurrentTime();
				simulation.runSimulation("KShortestPath");
				simulation.reset();*/
				
				PrintStream UnsplittingLPCplex = new PrintStream(new FileOutputStream("res/UnsplittingLPCplex_l"+i+"_c"+c+".txt"));
				System.setOut(UnsplittingLPCplex);
				writeCurrentTime();
				simulation.runSimulation("UnsplittingLPCplex");
				simulation.reset();
				
				PrintStream MultiCommdityFlow = new PrintStream(new FileOutputStream("res/MultiCommdityFlow_l"+i+"_c"+c+".txt"));
				System.setOut(MultiCommdityFlow);
				writeCurrentTime();
				simulation.runSimulation("MultiCommdityFlow");
				simulation.reset();

				System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
				System.out.println("job done");
			}
		}
		writer = new FileWriter("resultat.txt",true);
		writer.write("/---------------Simulation finished!---------------/\n");
		writer.close();
	}
	
	public static void writeCurrentTime(){
		FileWriter writer;
		try {
			writer = new FileWriter("result.txt",true);
			writer.write("Time : "+new SimpleDateFormat().format(new Date())+"\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
