package li.simulation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class SimulationTest {

	public static void main(String[] args) throws IOException {
		
		//print to a file instead of console
		PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
		System.setOut(out);
		
		//Simulation simulation = new Simulation();
		MultiDomainSimulation simulation = new MultiDomainSimulation();
		
		simulation.runSimulation("Shen2014");
		simulation.reset();
		simulation.runSimulation("MultiDomainAsOneDomain");
		simulation.reset();
		simulation.runSimulation("AS_MCF");
		
		System.out.println("job done");
	}

}
