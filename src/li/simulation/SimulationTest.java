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
		simulation.runSimulation();
	}

}
