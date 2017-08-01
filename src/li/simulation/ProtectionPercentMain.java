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

public class ProtectionPercentMain {
	public static int c;
	public static void main(String[] args) throws IOException {
		int i=10;
		FileWriter writer = new FileWriter("result.txt",true);
		writer.write("/----------------New Simulation--------------/\n");
		writer.close();
		for(c=0;c<10;c++){
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
			
			simulation.initialize(i);
			simulation.getSubstrateNetwork().configPercentage(1);
			
			List<Double> percentList=new ArrayList<Double>();
//			percentList.add(0.3);
			percentList.add(0.5);
			percentList.add(0.55);
			percentList.add(0.6);
			percentList.add(0.65);
			percentList.add(0.7);
//			percentList.add(0.75);
//			percentList.add(0.8);
//			percentList.add(0.85);
//			percentList.add(0.9);
			
			for(double percent:percentList){
				simulation.getSubstrateNetwork().configPercentage(percent);
				writePercent(percent);
				
				PrintStream SPWithoutBackupVF = new PrintStream(new FileOutputStream("res/SPWithoutBackupVF_l"+i+"_c"+c+".txt"));
				System.setOut(SPWithoutBackupVF);
				writeCurrentTime();
				simulation.runSimulation("SPWithoutBackupVF","included");
				simulation.reset();
				
				PrintStream ShortestPathBackupVF = new PrintStream(new FileOutputStream("res/ShortestPathBackupVF_l"+i+"_c"+c+".txt"));
				System.setOut(ShortestPathBackupVF);
				writeCurrentTime();
				simulation.runSimulation("ShortestPathBackupVF","included");
				simulation.reset();
				
				PrintStream SPWithoutBackupVF2 = new PrintStream(new FileOutputStream("res/SPWithoutBackupVF2_l"+i+".txt"));
				System.setOut(SPWithoutBackupVF2);
				writeCurrentTime();
				simulation.runSimulation("SPWithoutBackupVF2","included");
				simulation.reset();
				
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
		writer = new FileWriter("result.txt",true);
		writer.write("/---------------Simulation finished!---------------/\n");
		writer.close();
	}
	
	public static void writeCurrentTime(){
		FileWriter writer;
		try {
			writer = new FileWriter("result.txt",true);
			writer.write("Time : "+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())+"\n");
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
