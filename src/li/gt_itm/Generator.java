package li.gt_itm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Random;


public class Generator {
	public void runShellCmd(String cmd) throws IOException{
		Runtime rt= Runtime.getRuntime();
		Process p = rt.exec(cmd);
		String s;
		BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }
	
	}
	
	//create substrate network
	public void createSubgb() throws IOException{
		File dir = new File("data");
		if(!dir.exists())
			dir.mkdir();
		/*
		File file = new File("sub");
		file.createNewFile();
		*/
		PrintWriter pw = new PrintWriter("data/subCmd2");
		pw.println("geo 1");
		pw.println("10 25 3 0.5");
		pw.close();
		this.runShellCmd("./itm data/subCmd2");
		
		this.runShellCmd("./sgb2alt data/subCmd2-0.gb data/sub2");
		
	}
	
	//create virtual networks
	public void createVgb() throws IOException{
		File dir = new File("data");
		if(!dir.exists())
			dir.mkdir();
		
		for(int i=0;i<500;i++){ //VN quantity
			Random random = new Random();
			Random random2 = new Random();
			PrintWriter pw = new PrintWriter("data/vgb"+i+"Cmd");	//create file
			pw.println("geo 1 "+random.nextInt(100));	//write in file
			int node=random2.nextInt(8)+2;
			pw.println(node+" 100 3 0.5"); //100*100 plane, connectivity probability 0.5
			pw.close();
			this.runShellCmd("./gt-itm/itm gt-itm/data/vgb"+i+"Cmd");	//create graph, gb format, middle step
			
			this.runShellCmd("./gt-itm/sgb2alt gt-itm/data/vgb"+i+"Cmd-0.gb"+" data/vir"+i); //result in ./data, name vir0, vir1...
		}

	}
	
}
