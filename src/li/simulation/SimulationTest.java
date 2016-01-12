package li.simulation;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimulationTest {

	public static void main(String[] args) throws IOException {
		
		FileWriter writer = new FileWriter("resultat.txt",true);
		writer.write("/----------------New Simulation--------------/\n");
		writer.write("Simulation time : "+new SimpleDateFormat().format(new Date())+"\n");
		writer.close();
		
		for(int c=0;c<3;c++){
			writer = new FileWriter("resultat.txt",true);
			writer.write("Number:"+c+"\n");
			writer.close();
			
			MultiDomainSimulation simulation = new MultiDomainSimulation();
			
			System.out.println(simulation.getMultiDomain().get(0));
			System.out.println(simulation.getMultiDomain().get(1));
			System.out.println("v:"+simulation.getMultiDomain().get(0).getVertexCount());
			System.out.println("e:"+simulation.getMultiDomain().get(0).getEdgeCount());
			System.out.println("v:"+simulation.getMultiDomain().get(1).getVertexCount());
			System.out.println("e:"+simulation.getMultiDomain().get(1).getEdgeCount());
			System.out.println("inter:"+simulation.getMultiDomain().get(1).getInterLinkCount());
			
	//		PrintStream tmp = new PrintStream(new FileOutputStream("tmp.txt"));
	//		System.setOut(tmp);
		
			for(int i=3;i<4;i++){
				simulation.initialize(i);
				
				PrintStream md = new PrintStream(new FileOutputStream("res/MultiDomainAsOneDomain_l"+i+"_c"+c+".txt"));
				System.setOut(md);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("MultiDomainAsOneDomain");
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.reset();
				
				PrintStream shen = new PrintStream(new FileOutputStream("res/Shen_l"+i+"_c"+c+".txt"));
				System.setOut(shen);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("Shen2014");
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.reset();
				
				PrintStream mdrk = new PrintStream(new FileOutputStream("res/MultiDomainRanking_l"+i+"_c"+c+".txt"));
				System.setOut(mdrk);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("MultiDomainRanking");
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.reset();
				
				PrintStream mdrk2 = new PrintStream(new FileOutputStream("res/MultiDomainRanking2_l"+i+"_c"+c+".txt"));
				System.setOut(mdrk2);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("MultiDomainRanking2");
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
