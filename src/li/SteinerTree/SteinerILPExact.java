package li.SteinerTree;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import vnreal.algorithms.AbstractLinkMapping;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class SteinerILPExact extends AbstractLinkMapping {
	private String localPath ;
	private String remotePath ;
	public SteinerILPExact(SubstrateNetwork sNet) {
		super(sNet);
		this.localPath = "ILP-LP-Models/vne-mcf.lp";
		this.remotePath = "pytest/vne-mcf.lp";
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		//Map<String, String> solution = linkMappingWithoutUpdate(vNet, nodeMapping);
				Map<String, String> solution = linkMappingWithoutUpdateLocal(vNet, nodeMapping);	
				if(solution.size()==0){
					System.out.println("link no solution");
					for(Map.Entry<VirtualNode, SubstrateNode> entry : nodeMapping.entrySet()){
						NodeLinkDeletion.nodeFree(entry.getKey(), entry.getValue());
					}
					return false;
				}
				//update
				//updateResource(vNet, nodeMapping, solution);
				
				return true;
	}
	
	public Map<String,String> linkMappingWithoutUpdateLocal(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		Map<String, String> solution = new HashMap<String, String>();
		//generate .lp file
		try {
			this.generateFile(vNet, nodeMapping);
			Process p = Runtime.getRuntime().exec("python cplex/mysolver.py "+localPath+" o");
			InputStream in = p.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String readLine;
			boolean solBegin=false;
			while (((readLine = br.readLine()) != null)) {
				if(solBegin==true){
					System.out.println(readLine);
					StringTokenizer st = new StringTokenizer(readLine, " ");
					solution.put(st.nextToken(), st.nextToken());
				}
				if(solBegin==false&&readLine.equals("The solutions begin here : "))
					solBegin=true;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return solution;
	}
	
	public void generateFile(VirtualNetwork vNet,Map<VirtualNode, SubstrateNode> nodeMapping) throws IOException{
		SubstrateNode ssnode=null, dsnode=null;
		VirtualNode srcVnode = null, dstVnode = null;
		Collection<SubstrateNode> nextHop;

		String preambule = "\\Problem : vne\n";
		String obj = "Maximize\n"+"obj : ";
		String constraint = "Subject To\n";
		String bounds = "Bounds\n";
		String general = "General\n";
		
		//flow constraint
		for(VirtualLink vlink : vNet.getEdges()){
			// Find their mapped SubstrateNodes
			srcVnode = vNet.getEndpoints(vlink).getFirst();
			dstVnode = vNet.getEndpoints(vlink).getSecond();
			
			for(SubstrateNode snode : sNet.getVertices()){
				nextHop = sNet.getNeighbors(snode);
				for(Iterator<SubstrateNode> it=nextHop.iterator();it.hasNext();){
					SubstrateNode tmmpsn = it.next();
					constraint=constraint+" + vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+snode.getId()+"sd"+tmmpsn.getId();
					constraint=constraint+" - vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+tmmpsn.getId()+"sd"+snode.getId();
				}
				
				if(snode.equals(nodeMapping.get(srcVnode)))	constraint =constraint+" = 1\n";
				else if(snode.equals(nodeMapping.get(dstVnode))) constraint =constraint+" = -1\n";
				else	constraint =constraint+" = 0\n";
				
			}
			
		}
		
		for (SubstrateLink slink : sNet.getEdges()){
			
			//objective
			ssnode = sNet.getEndpoints(slink).getFirst();
			dsnode = sNet.getEndpoints(slink).getSecond();
			obj = obj + " "+Math.log(slink.getProbability());
			obj = obj + " Xs"+ssnode.getId()+"d"+dsnode.getId();
			
			//general
			general = general + "Xs"+ssnode.getId()+"d"+dsnode.getId()+"\n";
			
			for(VirtualLink vlink : vNet.getEdges()){
				// Find their mapped SubstrateNodes
				srcVnode = vNet.getEndpoints(vlink).getFirst();
				dstVnode = vNet.getEndpoints(vlink).getSecond();
				
				//f and x constraint
				constraint=constraint+" + vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId();
				constraint=constraint+" + vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId();
				
				//bounds
				bounds = bounds + "0 <= vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+ssnode.getId()+"sd"+dsnode.getId()+" <= 1\n";
				bounds = bounds + "0 <= vs"+srcVnode.getId()+"vd"+dstVnode.getId()+"ss"+dsnode.getId()+"sd"+ssnode.getId()+" <= 1\n";
				
			}
				constraint = constraint + " - 20 Xs"+ssnode.getId()+"d"+dsnode.getId()+"<=0\n";
			
		}
		
		
		obj = obj+ "\n";
		BufferedWriter writer = new BufferedWriter(new FileWriter(localPath));
		writer.write(preambule+obj+constraint+bounds+general+"END");
		writer.close();
		
	}
	
}
