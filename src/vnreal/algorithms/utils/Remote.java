package vnreal.algorithms.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class Remote {
	
	private Session session;
	private Channel channel;
	private ChannelSftp sftp;
	
	public Session getSession() {
		return session;
	}

	public Channel getChannel() {
		return channel;
	}

	public ChannelSftp getSftp() {
		return sftp;
	}

	public Remote(){
		JSch jsch = new JSch();
		String user = "li.shuopeng";
		String host = "magi.univ-paris13.fr";
		int port = 2822;
		try {
			session=jsch.getSession(user, host, port);
			session.setPassword("lsp891025");
			java.util.Properties config = new java.util.Properties(); 
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			sftp = (ChannelSftp) session.openChannel("sftp");

		} catch (JSchException e) {
			e.printStackTrace();
		}
	}
	
	// upload and download, use put and get method of sftp
	
	public Map<String, String> executeCmd(String cmd) throws JSchException, IOException{
		ChannelExec exec = (ChannelExec) session.openChannel("exec");
		exec.setCommand(cmd);
		InputStream in = exec.getInputStream();
		exec.connect();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String readLine, variable=null,value=null;
		boolean solBegin=false;
		Map<String, String> result = new HashMap<String, String>();
		while (((readLine = br.readLine()) != null)) {
			//System.out.println(readLine);
			if(solBegin==true){
				variable = readLine.substring(0, readLine.indexOf(" "));
				value = readLine.substring(readLine.indexOf(" ")+1);
				result.put(variable, value);
				
			}
			if(readLine.equals("The solutions begin here : "))
				solBegin=true;
		}
		
		exec.disconnect();
		return result;
	}
	
	public void disconnect(){
		sftp.disconnect();
		session.disconnect();
	}
}
