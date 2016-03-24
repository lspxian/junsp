package li.gt_itm;

import java.io.IOException;

import li.simulation.Centralized_MD_VNE_Simulation;


public class Example {

	public static void main(String[] args) throws IOException {

		Centralized_MD_VNE_Simulation simulation = new Centralized_MD_VNE_Simulation();
		
		for(int i=0;i<simulation.getMultiDomain().size();i++){
			System.out.println(simulation.getMultiDomain().get(i));
		}
		
		for(int i=0;i<simulation.getMultiDomain().size();i++){
			System.out.println("v:"+simulation.getMultiDomain().get(i).getVertexCount());
			System.out.println("e:"+simulation.getMultiDomain().get(i).getEdgeCount());
		}
		for(int i=0;i<simulation.getMultiDomain().size();i++){
			System.out.println("inter:"+simulation.getMultiDomain().get(i).getInterLinkCount());				
		}
		
		//substrat network
		//Generator.createSubNet();
		
		//virtual networks
		//createVgb();
	}

}
