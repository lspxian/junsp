package li.gt_itm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Random;

public class Generator {
	
	//create substrate network
	public static void createSubNet() throws IOException{
		File dir = new File("gt-itm");
		if(!dir.exists())
			dir.mkdir();
		
		PrintWriter pw = new PrintWriter("gt-itm/subCmd");
		pw.println("geo 1 "+new Random().nextInt(100));
		//node number, scale, method, proba connect
		pw.println("50 100 3 0.1");
		//0.1 for multi domain
//		pw.println("40 100 4 0.25 0.01 1");
		pw.close();
		runShellCmd("./gt-itm/itm gt-itm/subCmd");
		runShellCmd("./gt-itm/sgb2alt gt-itm/subCmd-0.gb gt-itm/sub");
//		cutFile();
	}
	
	//create virtual networks
	public static void createVirNet() throws IOException{
		File dir = new File("gt-itm");
		if(!dir.exists())
			dir.mkdir();
		
		PrintWriter pw = new PrintWriter("gt-itm/subCmd");
		pw.println("geo 1 "+new Random().nextInt(100));
		//node number, scale, method, proba connect
		int number = new Random().nextInt(5)+3;
//		int number = 2;
		pw.println(number+" 100 3 0.5");
		pw.close();
		runShellCmd("./gt-itm/itm gt-itm/subCmd");
		runShellCmd("./gt-itm/sgb2alt gt-itm/subCmd-0.gb gt-itm/vir");
	}
	
	//
	public static void createVgb() throws IOException{
		File dir = new File("data200");
		if(!dir.exists())
			dir.mkdir();
		
		for(int i=0;i<15;i++){ //VN quantity
			Random random = new Random();
			Random random2 = new Random();
			PrintWriter pw = new PrintWriter("gt-itm/data/vgb"+i+"Cmd");	//create file
			pw.println("geo 1 "+random.nextInt(100));	//write in file, initial random number seed
			int node=random2.nextInt(5)+10;
			pw.println(node+" 200 3 0.5"); //100*100 plane, connectivity probability 0.5
			pw.close();
			//this.runShellCmd("ls ./gt-itm");
			
			runShellCmd("./gt-itm/itm gt-itm/data/vgb"+i+"Cmd");	//create graph, gb format, middle step
			runShellCmd("./gt-itm/sgb2alt gt-itm/data/vgb"+i+"Cmd-0.gb"+" data200/vir"+i); //result in ./data, name vir0, vir1...
			
		}

	}
	private static void runShellCmd(String cmd) throws IOException{
		Runtime rt= Runtime.getRuntime();
		Process p = rt.exec(cmd);
		String s;
		BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }
	
	}
	
}
