package li.SteinerTree;

import java.util.Map;
import java.util.TreeMap;

import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class PrimMST {
	private SubstrateNetwork sNet;
	
	public PrimMST(SubstrateNetwork sNet){
		this.sNet=sNet;
	}
	
	public SubstrateNetwork getMST(){
		
		SubstrateNetwork mst=new SubstrateNetwork();
		SubstrateNode startNode = sNet.getVertices().iterator().next();
		mst.addVertex(startNode);
		
		
		while(mst.getVertexCount()<sNet.getVertexCount()){
			Map<SubstrateLink,SubstrateNode> candidats=new TreeMap<SubstrateLink,SubstrateNode>(new LinkProbaComparator());
			for(SubstrateNode sn1 : mst.getVertices()){
				for(SubstrateNode sn2 : this.sNet.getVertices()){
					if((!mst.getVertices().contains(sn2))&&(this.sNet.findEdge(sn1, sn2)!=null)){
						candidats.put(this.sNet.findEdge(sn1, sn2), sn2);
					}
				}
			}
			
			boolean flag=false;
			for(Map.Entry<SubstrateLink, SubstrateNode> entry: candidats.entrySet()){
				SubstrateLink sl = entry.getKey();
				mst.addEdge(sl, sNet.getEndpoints(sl));
				Cycle cycle = new Cycle(mst);
				if(cycle.isCyclic()){
					mst.removeEdge(sl);
					mst.removeVertex(entry.getValue());
				}
				else{
					flag=true;
					break;
				}
			}
		}
		
		return mst;
	}
}
