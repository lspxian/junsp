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
		FileWriter writer = new FileWriter("result.txt",true);
		writer.write("/----------------New Simulation--------------/\n");
		writer.close();
		for(c=0;c<1;c++){
			writer = new FileWriter("result.txt",true);
			writer.write("Number:"+c+"\n");
			ProbabilitySimulation simulation = new ProbabilitySimulation();
			System.out.println("Substrate Network : v "+
							simulation.getSubstrateNetwork().getVertexCount()+" e "+
							simulation.getSubstrateNetwork().getEdgeCount()+"\n");
			writer.write("Substrate Network : v "+
					simulation.getSubstrateNetwork().getVertexCount()+" e "+
					simulation.getSubstrateNetwork().getEdgeCount()+"\n");
			writer.close();
			
//			int i= Integer.valueOf(args[0]);
			for(int i=2;i<7;i++){
				simulation.initialize(i);
				/*
				PrintStream exact = new PrintStream(new FileOutputStream("res/E xactILP_l"+i+"_c"+c+".txt"));
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
				simulation.reset();
				
				PrintStream kmb = new PrintStream(new FileOutputStream("res/KMB1981_l"+i+"_c"+c+".txt"));
				System.setOut(kmb);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("KMB1981");
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.reset();
				
				PrintStream kmb2 = new PrintStream(new FileOutputStream("res/KMB1981V2_l"+i+"_c"+c+".txt"));
				System.setOut(kmb2);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("KMB1981V2");
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.reset();*/
			
				/*
				PrintStream probaHeuristic2 = new PrintStream(new FileOutputStream("res/probaHeuristic2_l"+i+"_c"+c+".txt"));
				System.setOut(probaHeuristic2);
				writeCurrentTime();
				simulation.runSimulation("ProbaHeuristic2");
//				writeCurrentTime();
				simulation.reset();*/
/*
				PrintStream probaHeuristic1 = new PrintStream(new FileOutputStream("res/probaHeuristic1_l"+i+"_c"+c+".txt"));
				System.setOut(probaHeuristic1);
				writeCurrentTime();
				simulation.runSimulation("ProbaHeuristic1");
				simulation.reset();*/
				
				PrintStream probaHeuristic3 = new PrintStream(new FileOutputStream("res/probaHeuristic3_l"+i+"_c"+c+".txt"));
				System.setOut(probaHeuristic3);
				writeCurrentTime();
				simulation.runSimulation("ProbaHeuristic3");
				simulation.reset();
				
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
			/*
				PrintStream pbbwExact = new PrintStream(new FileOutputStream("res/PBBWExactILP_l"+i+"_c"+c+".txt"));
				System.setOut(pbbwExact);
				writeCurrentTime();
				simulation.runSimulation("PBBWExact");
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
