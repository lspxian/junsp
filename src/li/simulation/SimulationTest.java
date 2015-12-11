package li.simulation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class SimulationTest {

	public static void main(String[] args) throws IOException {
		
		//print to a file instead of console
		PrintStream tmp = new PrintStream(new FileOutputStream("tmp.txt"));
		
		//Simulation simulation = new Simulation();
		MultiDomainSimulation simulation = new MultiDomainSimulation();
//		System.setOut(tmp);
		
		PrintStream mdrk = new PrintStream(new FileOutputStream("MultiDomainRanking"));
		System.setOut(mdrk);
		simulation.runSimulation("MultiDomainRanking");
		simulation.reset();

		PrintStream md = new PrintStream(new FileOutputStream("MultiDomainAsOneDomain.txt"));
		System.setOut(md);
		simulation.runSimulation("MultiDomainAsOneDomain");
		simulation.reset();
		
		PrintStream shen = new PrintStream(new FileOutputStream("shen.txt"));
		System.setOut(shen);
		simulation.runSimulation("Shen2014");
		simulation.reset();
		
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
