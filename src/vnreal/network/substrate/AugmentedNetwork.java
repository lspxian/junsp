package vnreal.network.substrate;

import edu.uci.ics.jung.graph.util.Pair;
import li.multiDomain.Domain;

public class AugmentedNetwork extends SubstrateNetwork {
	protected SubstrateNetwork root;

	public AugmentedNetwork() {
		super();
	}
	
	public AugmentedNetwork(Domain domain){
		super();
		this.copy(domain);
		this.root = domain;
	}


	public SubstrateNetwork getRoot() {
		return root;
	}

	public void setRoot(SubstrateNetwork root) {
		this.root = root;
	}
	
	public boolean existLink(SubstrateNode src, SubstrateNode dest){
		for(SubstrateLink slink : this.getEdges()){
			if(slink instanceof AugmentedLink){
				Pair<SubstrateNode> p = this.getEndpoints(slink);
				if(p.contains(src)&&p.contains(dest)){
					return true;
				}
			}
		}
		return false;
	}
	
	public int getInterLinkCount(){
		int nomber = 0;
		for(SubstrateLink slink : this.getEdges()){
			if(slink instanceof InterLink)
				nomber++;
		}
		return nomber;
	}
	
	public String toString(){
		return "Augemented network : \n"+super.toString();
	}
	
}
