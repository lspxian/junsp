package li.SteinerTree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;

public class KruskalMST {
	private SubstrateNetwork sNet;
	
	public KruskalMST(SubstrateNetwork sNet){
		this.sNet=sNet;
	}
	
	public SubstrateNetwork getMST(){
		SubstrateNetwork mst=new SubstrateNetwork();
		List<SubstrateLink> tempo = new ArrayList<SubstrateLink>();
		tempo.addAll(this.sNet.getEdges());
		tempo.sort(new LinkProbaComparator());
		int count = 0;
		Iterator<SubstrateLink> it = tempo.iterator();
		
		while(it.hasNext()&&(count<sNet.getEdgeCount()-1)){
			SubstrateLink sl = it.next();
			mst.addEdge(sl, sNet.getEndpoints(sl));
			Cycle cycle = new Cycle(mst);
			if(cycle.isCyclic()){
				mst.removeEdge(sl);
			}
			else{
				count++;
			}
		}
		
		return mst;
	}
}
