package li.simulation;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
			
			for(int i=3;i<4;i=i+1){
				simulation.initialize(i);
				simulation.getSubstrateNetwork().configPercentage(1);
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
				
				PrintStream sp_csp = new PrintStream(new FileOutputStream("res/sp_csp_l"+i+"_c"+c+".txt"));
				System.setOut(sp_csp);
				writeCurrentTime();
				simulation.runSimulation("ShortestPathBW","ConstraintSP");
				simulation.reset();
				
				PrintStream sp_pb = new PrintStream(new FileOutputStream("res/sp_pb_l"+i+"_c"+c+".txt"));
				System.setOut(sp_pb);
				writeCurrentTime();
				simulation.runSimulation("ShortestPathBW","CSP_Proba");
				simulation.reset();*/
				/*
				PrintStream mcf_be = new PrintStream(new FileOutputStream("res/mcf_be_l"+i+"_c"+c+".txt"));
				System.setOut(mcf_be);
				writeCurrentTime();
				simulation.runSimulation("SPBWConfig","CSP_Proba");
				simulation.reset();
				
				PrintStream mcf_csp = new PrintStream(new FileOutputStream("res/mcf_csp_l"+i+"_c"+c+".txt"));
				System.setOut(mcf_csp);
				writeCurrentTime();
				simulation.runSimulation("MCF","ConstraintSP");
				simulation.reset();
				
				PrintStream mcf_pb = new PrintStream(new FileOutputStream("res/mcf_pb_l"+i+"_c"+c+".txt"));
				System.setOut(mcf_pb);
				writeCurrentTime();
				simulation.runSimulation("MCF","CSP_Proba");
				simulation.reset();*/

				/*
				PrintStream mcf = new PrintStream(new FileOutputStream("res/mcf_l"+i+"_c"+c+".txt"));
				System.setOut(mcf);
				writeCurrentTime();
				simulation.runSimulation("MCF","");
				simulation.reset();*/
				
				List<Double> percentList=new ArrayList<Double>();
//				percentList.add(0.3);
//				percentList.add(0.5);
//				percentList.add(0.55);
//				percentList.add(0.6);
//				percentList.add(0.65);
				percentList.add(0.7);
//				percentList.add(0.75);
//				percentList.add(0.8);
//				percentList.add(0.9);
				for(double percent:percentList){
					simulation.getSubstrateNetwork().configPercentage(percent);
					writePercent(percent);
					/*
					PrintStream SPWithoutBackupVF = new PrintStream(new FileOutputStream("res/SPWithoutBackupVF_l"+i+"_c"+c+".txt"));
					System.setOut(SPWithoutBackupVF);
					writeCurrentTime();
					simulation.runSimulation("SPWithoutBackupVF","included");
					simulation.reset();
					
					PrintStream ShortestPathBackupVF = new PrintStream(new FileOutputStream("res/ShortestPathBackupVF_l"+i+"_c"+c+".txt"));
					System.setOut(ShortestPathBackupVF);
					writeCurrentTime();
					simulation.runSimulation("ShortestPathBackupVF","included");
					simulation.reset();*/
					/*
					PrintStream SPWithoutBackupVF2 = new PrintStream(new FileOutputStream("res/SPWithoutBackupVF2_l"+i+".txt"));
					System.setOut(SPWithoutBackupVF2);
					writeCurrentTime();
					simulation.runSimulation("SPWithoutBackupVF2","included");
					simulation.reset();*/
					
					PrintStream ShortestPathBackupVF2 = new PrintStream(new FileOutputStream("res/ShortestPathBackupVF2_l"+i+".txt"));
					System.setOut(ShortestPathBackupVF2);
					writeCurrentTime();
					simulation.runSimulation("ShortestPathBackupVF2","included");
					simulation.reset();
					/*
					PrintStream MaxFlowBackupVF = new PrintStream(new FileOutputStream("res/MaxFlowBackupVF_l"+i+".txt"));
					System.setOut(MaxFlowBackupVF);
					writeCurrentTime();
					simulation.runSimulation("MaxFlowBackupVF","included");
					simulation.reset();*/
					
					PrintStream MaxFlowBackupVF2 = new PrintStream(new FileOutputStream("res/MaxFlowBackupVF2_l"+i+".txt"));
					System.setOut(MaxFlowBackupVF2);
					writeCurrentTime();
					simulation.runSimulation("MaxFlowBackupVF2","included");
					simulation.reset();
					
				}
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
	
	public static void writePercent(double percent){
		FileWriter writer;
		try {
			writer = new FileWriter("result.txt",true);
			writer.write("Primary percent : "+percent+"\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
