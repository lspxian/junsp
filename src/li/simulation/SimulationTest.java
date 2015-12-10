package li.simulation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class SimulationTest {

	public static void main(String[] args) throws IOException {
		
		//print to a file instead of console
		PrintStream tmp = new PrintStream(new FileOutputStream("tmp.txt"));
		PrintStream shen = new PrintStream(new FileOutputStream("shen.txt"));
		PrintStream as = new PrintStream(new FileOutputStream("AS_MCF.txt"));
		PrintStream md = new PrintStream(new FileOutputStream("MultiDomainAsOneDomain.txt"));
		PrintStream mo = new PrintStream(new FileOutputStream("MDasOD2.txt"));
		PrintStream tdmcf = new PrintStream(new FileOutputStream("TwoDomainMCF.txt"));
		PrintStream mdrk = new PrintStream(new FileOutputStream("MultiDomainRanking"));
		
		
		//Simulation simulation = new Simulation();
		MultiDomainSimulation simulation = new MultiDomainSimulation();
//		System.setOut(tmp);
		
		System.setOut(mdrk);
		simulation.runSimulation("MultiDomainRanking");
		simulation.reset();

//		System.setOut(shen);
//		simulation.runSimulation("Shen2014");
//		simulation.reset();
//				
//		System.setOut(md);
//		simulation.runSimulation("MultiDomainAsOneDomain");
//		simulation.reset();
		
		System.setOut(mo);
		simulation.runSimulation("MDasOD2");
		simulation.reset();

//		System.setOut(tdmcf);
//		simulation.runSimulation("TwoDomainMCF");
//		simulation.reset();
//		
//		System.setOut(as);
//		simulation.runSimulation("AS_MCF");
//		simulation.reset();
		
		PrintStream original = System.out;
		System.setOut(original);
		System.out.println("job done");
	}

}
