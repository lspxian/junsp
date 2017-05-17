package li.simulation;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Centralized_MD_VNE_InterLink_Main {

	public static int c;
	public static void main(String[] args) throws IOException {
		FileWriter writer = new FileWriter("cenResult.txt",true);
		writer.write("/----------------New Simulation--------------/\n");
		writer.write("Simulation time : "+new SimpleDateFormat().format(new Date())+"\n");
		writer.close();
		
		List<Double> betas=new ArrayList<Double>();
		betas.add(0.01);
		betas.add(0.05);
		betas.add(0.09);
		betas.add(0.14);
		betas.add(0.17);
		betas.add(0.21);
		
		for(c=0;c<10;c++){
			for(double beta:betas){
				Centralized_MD_VNE_Simulation simulation = new Centralized_MD_VNE_Simulation(0.8,beta);
				
				for(int i=0;i<simulation.getMultiDomain().size();i++){
					System.out.println("v:"+simulation.getMultiDomain().get(i).getVertexCount());
					System.out.println("e:"+simulation.getMultiDomain().get(i).getEdgeCount());
				}
				for(int i=0;i<simulation.getMultiDomain().size();i++){
					System.out.println("inter:"+simulation.getMultiDomain().get(i).getInterLinkCount());				
				}
				
				writer = new FileWriter("cenResult.txt",true);
				for(int i=0;i<simulation.getMultiDomain().size();i++){
					writer.write(" v:"+simulation.getMultiDomain().get(i).getVertexCount());
					writer.write(" e:"+simulation.getMultiDomain().get(i).getEdgeCount());
				}
				writer.write("\n");
				for(int i=0;i<simulation.getMultiDomain().size();i++){
					writer.write(" inter:"+simulation.getMultiDomain().get(i).getInterLinkCount());				
				}
				writer.write("\n");
				writer.close();
				
				writer = new FileWriter("cenResult.txt",true);
				writer.write("Number:"+c+"\n");
				writer.write("*----beta="+beta+"------*\n");
				writer.close();
				
				int i=4;
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
				/*
				PrintStream mdrk = new PrintStream(new FileOutputStream("res/MultiDomainRanking_l"+i+"_c"+c+".txt"));
				System.setOut(mdrk);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("MultiDomainRanking");
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.reset();*/
				
				PrintStream mdrk2 = new PrintStream(new FileOutputStream("res/MultiDomainRanking2_l"+i+"_c"+c+".txt"));
				System.setOut(mdrk2);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("MultiDomainRanking2");
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.reset();
				
				PrintStream mdrk3 = new PrintStream(new FileOutputStream("res/MultiDomainRanking3_l"+i+"_c"+c+".txt"));
				System.setOut(mdrk3);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("MultiDomainRanking3");
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.reset();
				
				System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
				System.out.println("job done");
					
		//		DrawGraphMD graph=new DrawGraphMD(simulation.getMultiDomain());
		//		graph.draw();
			}

		}
		writer = new FileWriter("cenResult.txt",true);
		writer.write("/---------------Simulation finished!---------------/\n");
		writer.write("Time : "+new SimpleDateFormat().format(new Date())+"\n\n");
		writer.close();		
	}
}
