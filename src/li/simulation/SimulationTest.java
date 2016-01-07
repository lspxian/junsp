package li.simulation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimulationTest {

	public static void main(String[] args) throws IOException {
		
		//print to a file instead of console

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
		
		for(int i=2;i<3;i++){
			simulation.initialize(i);
			
//			PrintStream mdrk = new PrintStream(new FileOutputStream("MultiDomainRanking_l"+i+".txt"));
//			System.setOut(mdrk);
//			System.out.println(new SimpleDateFormat().format(new Date()));
//			simulation.runSimulation("MultiDomainRanking");
//			simulation.reset();
			
			PrintStream shen = new PrintStream(new FileOutputStream("Shen_l"+i+".txt"));
			System.setOut(shen);
			System.out.println(new SimpleDateFormat().format(new Date()));
			simulation.runSimulation("Shen2014");
			System.out.println(new SimpleDateFormat().format(new Date()));
			simulation.reset();
			
			PrintStream md = new PrintStream(new FileOutputStream("MultiDomainAsOneDomain_l"+i+".txt"));
			System.setOut(md);
			System.out.println(new SimpleDateFormat().format(new Date()));
			simulation.runSimulation("MultiDomainAsOneDomain");
			System.out.println(new SimpleDateFormat().format(new Date()));
			simulation.reset();
	//		
	//		PrintStream mo = new PrintStream(new FileOutputStream("MDasOD2.txt"));
	//		System.setOut(mo);
	//		simulation.runSimulation("MDasOD2");
	//		simulation.reset();
	
	//		PrintStream tdmcf = new PrintStream(new FileOutputStream("TwoDomainMCF.txt"));
	//		System.setOut(tdmcf);
	//		simulation.runSimulation("TwoDomainMCF");
	//		simulation.reset();
	//		
	//		PrintStream as = new PrintStream(new FileOutputStream("AS_MCF.txt"));
	//		System.setOut(as);
	//		simulation.runSimulation("AS_MCF");
	//		simulation.reset();
			
			PrintStream original = System.out;
			System.setOut(original);
			System.out.println("job done");
		}
	}

}
