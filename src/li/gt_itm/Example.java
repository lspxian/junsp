package li.gt_itm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Example {

	public static void main(String[] args) throws IOException {
/*
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
		}*/
		
		//substrat network
		//Generator.createSubNet();
		
		//virtual networks
		for(int i=0;i<400;i++){
			Generator.createVirNet();
			BufferedReader br = new BufferedReader(new FileReader("gt-itm/sub"));
			String line=null;
			System.out.println("new vn");
			while((line=br.readLine())!=null){
				System.out.println(line);
			}
		br.close();	
		}
		
		
		
		
	}

}
