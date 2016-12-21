package li.simulation;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProtectionSimMain {

public static int c;
	
	public static void main(String[] args) throws IOException {
		FileWriter writer = new FileWriter("result.txt",true);
		writer.write("/----------------New Simulation--------------/\n");
		writer.close();
		for(c=0;c<1;c++){
			writer = new FileWriter("result.txt",true);
			writer.write("Number:"+c+"\n");
			ProtectionSim simulation = new ProtectionSim();
			System.out.println("Substrate Network : v "+
							simulation.getSubstrateNetwork().getVertexCount()+" e "+
							simulation.getSubstrateNetwork().getEdgeCount()+"\n");
			writer.write("Substrate Network : v "+
					simulation.getSubstrateNetwork().getVertexCount()+" e "+
					simulation.getSubstrateNetwork().getEdgeCount()+"\n");
			writer.close();
			
			for(int i=3;i<5;i++){
				simulation.initialize(i);
				/*
				PrintStream ShortestPathBW = new PrintStream(new FileOutputStream("res/ShortestPathBW_l"+i+"_c"+c+".txt"));
				System.setOut(ShortestPathBW);
				writeCurrentTime();
				simulation.runSimulation("ShortestPathBW","");
				simulation.reset();
				
				PrintStream probaHeuristic = new PrintStream(new FileOutputStream("res/probaHeuristic_l"+i+"_c"+c+".txt"));
				System.setOut(probaHeuristic);
				writeCurrentTime();
				simulation.runSimulation("ProbaHeuristic","");
				simulation.reset();
				*/
				PrintStream mcf_csp = new PrintStream(new FileOutputStream("res/mcf_csp_l"+i+"_c"+c+".txt"));
				System.setOut(mcf_csp);
				writeCurrentTime();
				simulation.runSimulation("MCF","ConstraintSP");
				simulation.reset();
				/*
				PrintStream pe2_csp = new PrintStream(new FileOutputStream("res/pe2_csp_l"+i+"_c"+c+".txt"));
				System.setOut(pe2_csp);
				writeCurrentTime();
				simulation.runSimulation("ProtectionEnabledMCF2","ConstraintSP");
				simulation.reset();
				
				PrintStream mcf_pb = new PrintStream(new FileOutputStream("res/mcf_pb_l"+i+"_c"+c+".txt"));
				System.setOut(mcf_pb);
				writeCurrentTime();
				simulation.runSimulation("MCF","CSP_Proba");
				simulation.reset();*/
				/*
				PrintStream pe_pb = new PrintStream(new FileOutputStream("res/pe_pb_l"+i+"_c"+c+".txt"));
				System.setOut(pe_pb);
				writeCurrentTime();
				simulation.runSimulation("ProtectionEnabledMCF2","CSP_Proba");
				simulation.reset();*/
				/*
				PrintStream mcf_be = new PrintStream(new FileOutputStream("res/mcf_be_l"+i+"_c"+c+".txt"));
				System.setOut(mcf_be);
				writeCurrentTime();
				simulation.runSimulation("MCF","BestEffort");
				simulation.reset();*/

				/*
				PrintStream pepbw_csp = new PrintStream(new FileOutputStream("res/pepbw_csp_l"+i+"_c"+c+".txt"));
				System.setOut(pepbw_csp);
				writeCurrentTime();
				simulation.runSimulation("ProtectionEnabledBW","ConstraintSP");
				simulation.reset();*/

				writeCurrentTime();
				System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
				System.out.println("job done");
			}
		}
		writer = new FileWriter("result.txt",true);
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
