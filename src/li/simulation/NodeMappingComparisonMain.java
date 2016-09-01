package li.simulation;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NodeMappingComparisonMain {
public static int c;
	
	public static void main(String[] args) throws IOException {
		FileWriter writer = new FileWriter("nodecomparison.txt",true);
		writer.write("/----------------New Simulation--------------/\n");
		writer.write("Simulation time : "+new SimpleDateFormat().format(new Date())+"\n");
			
		NodeMappingComparisonSimulation simulation= new NodeMappingComparisonSimulation();
		
		writer.write("Substrate Network : v "+
		simulation.getSubstrateNetwork().getVertexCount()+" e "+simulation.getSubstrateNetwork().getEdgeCount()+"\n");
		writer.close();
			
		for(c=0;c<1;c++){
	//		PrintStream tmp = new PrintStream(new FileOutputStream("tmp.txt"));
	//		System.setOut(tmp);
			writer = new FileWriter("nodecomparison.txt",true);
			writer.write("Number:"+c+"\n");
			writer.close();
		
			for(int i=5;i<6;i++){
				simulation.initialize(i);
				
				PrintStream AvailableResourcesNodeMapping = new PrintStream(new FileOutputStream("res/ARMN"+i+"_c"+c+".txt"));
				System.setOut(AvailableResourcesNodeMapping);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("ARNM");
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.reset();
				/*
				PrintStream CordinatedNodeLinkMapping = new PrintStream(new FileOutputStream("res/CNLM"+i+"_c"+c+".txt"));
				System.setOut(CordinatedNodeLinkMapping);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("CNLM");
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.reset();*/
				
				System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
				System.out.println("job done");
			}
		}
		writer = new FileWriter("nodecomparison.txt",true);
		writer.write("/---------------Simulation finished!---------------/\n");
		writer.write("Time : "+new SimpleDateFormat().format(new Date())+"\n\n");
		writer.close();
	}
}
