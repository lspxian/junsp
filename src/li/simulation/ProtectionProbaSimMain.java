package li.simulation;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProtectionProbaSimMain {

public static int c;
	
	public static void main(String[] args) throws IOException {
		FileWriter writer = new FileWriter("result.txt",true);
		writer.write("/----------------New Simulation--------------/\n");
		writer.close();
		for(c=0;c<1;c++){
			writer = new FileWriter("result.txt",true);
			writer.write("Number:"+c+"\n");
			ProtectionProbaSim simulation = new ProtectionProbaSim();
			System.out.println("Substrate Network : v "+
							simulation.getSubstrateNetwork().getVertexCount()+" e "+
							simulation.getSubstrateNetwork().getEdgeCount()+"\n");
			writer.write("Substrate Network : v "+
					simulation.getSubstrateNetwork().getVertexCount()+" e "+
					simulation.getSubstrateNetwork().getEdgeCount()+"\n");
			writer.close();
			
			for(int i=2;i<4;i++){
				simulation.initialize(i);
				
				PrintStream probaHeuristic4 = new PrintStream(new FileOutputStream("res/probaHeuristic4_l"+i+"_c"+c+".txt"));
				System.setOut(probaHeuristic4);
				writeCurrentTime();
				simulation.runSimulation("ProbaHeuristic4");
				simulation.reset();
				
				PrintStream ShortestPathBW = new PrintStream(new FileOutputStream("res/ShortestPathBW_l"+i+"_c"+c+".txt"));
				System.setOut(ShortestPathBW);
				writeCurrentTime();
				simulation.runSimulation("ShortestPathBW");
				simulation.reset();
				
				PrintStream DisjointShortestPathPT = new PrintStream(new FileOutputStream("res/DisjointShortestPathPT_l"+i+"_c"+c+".txt"));
				System.setOut(DisjointShortestPathPT);
				writeCurrentTime();
				simulation.runSimulation("DisjointShortestPathPT");
				simulation.reset();
				
				PrintStream ShortestPathLocalPT = new PrintStream(new FileOutputStream("res/ShortestPathLocalPT_l"+i+"_c"+c+".txt"));
				System.setOut(ShortestPathLocalPT);
				writeCurrentTime();
				simulation.runSimulation("ShortestPathLocalPT");
				simulation.reset();
				
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
