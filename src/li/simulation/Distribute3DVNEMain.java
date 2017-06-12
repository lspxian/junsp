package li.simulation;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Distribute3DVNEMain {
	public static int c;

	public static void main(String[] args) throws IOException {
		
		
		FileWriter writer = new FileWriter("disResult.txt",true);
		writer.write("/----------------New Simulation--------------/\n");
		writer.write("Simulation time : "+new SimpleDateFormat().format(new Date())+"\n");
		writer.close();
		
		for(c=0;c<1;c++){
			
			Distribute3DVNE simulation = new Distribute3DVNE();
			/*
			for(int i=0;i<simulation.getMultiDomain().size();i++){
				System.out.println(simulation.getMultiDomain().get(i));
			}*/
			
			for(int i=0;i<simulation.getMultiDomain().size();i++){
				System.out.println("v:"+simulation.getMultiDomain().get(i).getVertexCount());
				System.out.println("e:"+simulation.getMultiDomain().get(i).getEdgeCount());
			}
			for(int i=0;i<simulation.getMultiDomain().size();i++){
				System.out.println("inter:"+simulation.getMultiDomain().get(i).getInterLinkCount());				
			}
			
			writer = new FileWriter("disResult.txt",true);
			for(int i=0;i<simulation.getMultiDomain().size();i++){
				writer.write(" v:"+simulation.getMultiDomain().get(i).getVertexCount());
				writer.write(" e:"+simulation.getMultiDomain().get(i).getEdgeCount());
			}
			writer.write("\n");
	//			writer.write("Number:"+c);
			for(int i=0;i<simulation.getMultiDomain().size();i++){
				writer.write(" inter:"+simulation.getMultiDomain().get(i).getInterLinkCount());				
			}
			writer.write("\n");
			writer.close();
			
	//		PrintStream tmp = new PrintStream(new FileOutputStream("tmp.txt"));
	//		System.setOut(tmp);
			writer = new FileWriter("disResult.txt",true);
			writer.write("Number:"+c+"\n");
			writer.close();
		
			for(int i=6;i<7;i++){
				simulation.initialize(i);
				
				PrintStream mdrk3 = new PrintStream(new FileOutputStream("res/AllPossibleMDRanking_l"+i+"_c"+c+".txt"));
				System.setOut(mdrk3);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("AllPossible");
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.reset();
				/*
				PrintStream mdrk = new PrintStream(new FileOutputStream("res/MultiDomainRanking_l"+i+"_c"+c+".txt"));
				System.setOut(mdrk);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("MultiDomainRanking");
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.reset();*/
				/*
				PrintStream shen = new PrintStream(new FileOutputStream("res/Shen_l"+i+"_c"+c+".txt"));
				System.setOut(shen);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("Shen2014");
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.reset();*/
				
				PrintStream mdrk2 = new PrintStream(new FileOutputStream("res/MultiDomainRanking2_l"+i+"_c"+c+".txt"));
				System.setOut(mdrk2);
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.runSimulation("MultiDomainRanking2");
				System.out.println(new SimpleDateFormat().format(new Date()));
				simulation.reset();


				
				
				System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
				System.out.println("job done");
			}
		}
		writer = new FileWriter("disResult.txt",true);
		writer.write("/---------------Simulation finished!---------------/\n");
		writer.write("Time : "+new SimpleDateFormat().format(new Date())+"\n\n");
		writer.close();

	}


}
