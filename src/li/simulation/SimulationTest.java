package li.simulation;

import java.io.IOException;

public class SimulationTest {

	public static void main(String[] args) throws IOException {
		//Simulation simulation = new Simulation();
		MultiDomainSimulation simulation = new MultiDomainSimulation();
		simulation.runSimulation();
	}

}
