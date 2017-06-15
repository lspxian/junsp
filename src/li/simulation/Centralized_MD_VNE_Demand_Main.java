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

public class Centralized_MD_VNE_Demand_Main {
	public static int c;
	public static void main(String[] args) throws IOException {
		FileWriter writer = new FileWriter("cenResult.txt",true);
		writer.write("/----------------New Simulation--------------/\n");
		writer.write("Simulation time : "+new SimpleDateFormat().format(new Date())+"\n");
		writer.close();
		
		List<Integer> demandMin=new ArrayList<Integer>();
		List<Integer> demandMax=new ArrayList<Integer>();
		demandMin.add(0);
		demandMin.add(10);
		demandMin.add(20);
		demandMin.add(30);
		demandMin.add(40);
		demandMax.add(20);
		demandMax.add(30);
		demandMax.add(40);
		demandMax.add(50);
		demandMax.add(60);
		
		for(c=0;c<10;c++){
			
			Centralized_MD_VNE_Simulation simulation = new Centralized_MD_VNE_Simulation(true, 0.8,0.12);
			
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
			writer.write("Number:"+c+"\n");
			writer.close();
			
			int i=3;
			for(int j=0;j<demandMin.size();j++){
				simulation.initialize(i,demandMin.get(j),demandMax.get(j));
				
				writer = new FileWriter("cenResult.txt",true);
				writer.write("VN link resource: min "+demandMin.get(j)+" max "+demandMax.get(j)+"\n");
				writer.close();
				
				PrintStream md = new PrintStream(new FileOutputStream("res/MultiDomainAsOneDomain_r"+j+"_c"+c+".txt"));
				System.setOut(md);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("MultiDomainAsOneDomain");
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.reset();
				
				PrintStream shen = new PrintStream(new FileOutputStream("res/Shen_r"+j+"_c"+c+".txt"));
				System.setOut(shen);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("Shen2014");
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.reset();
				/*
				PrintStream mdrk = new PrintStream(new FileOutputStream("res/MultiDomainRanking_r"+j+"_c"+c+".txt"));
				System.setOut(mdrk);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("MultiDomainRanking");
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.reset();*/
				
				PrintStream mdrk2 = new PrintStream(new FileOutputStream("res/MultiDomainRanking2_r"+j+"_c"+c+".txt"));
				System.setOut(mdrk2);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("MultiDomainRanking2");
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.reset();
				
				PrintStream mdrk3 = new PrintStream(new FileOutputStream("res/MultiDomainRanking3_r"+j+"_c"+c+".txt"));
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
