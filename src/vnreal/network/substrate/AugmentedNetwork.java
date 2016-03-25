package vnreal.network.substrate;

import java.util.ArrayList;
import java.util.List;

import edu.uci.ics.jung.graph.util.Pair;
import li.multiDomain.Domain;
import vnreal.network.virtual.VirtualNode;

public class AugmentedNetwork extends SubstrateNetwork {
	protected SubstrateNetwork root;

	public AugmentedNetwork() {
		super();
	}
	
	public AugmentedNetwork(SubstrateNetwork sNet){
		super();
		this.copy(sNet);
		this.root = sNet;
	}


	public SubstrateNetwork getRoot() {
		return root;
	}

	public void setRoot(SubstrateNetwork root) {
		this.root = root;
	}

	
	public List<MetaNode> getMetaNodes(){
		List<MetaNode> metaNodes = new ArrayList<MetaNode>();
		for(SubstrateNode snode:this.getVertices()){
			if(snode instanceof MetaNode)
				metaNodes.add((MetaNode)snode);
		}
		return metaNodes;
	}
	
	//in the augmented network, there exists an augmented link from src to dest for the virtual destination vnode
	public boolean existLink(SubstrateNode src, SubstrateNode dest,VirtualNode vnode){
		for(SubstrateLink slink : this.getEdges()){
			if(slink instanceof AugmentedLink){
				AugmentedLink al=(AugmentedLink)slink;
				Pair<SubstrateNode> p = this.getEndpoints(slink);
				if(p.contains(src)&&p.contains(dest)&&(al.getDestNode().equals(vnode))){
					return true;
				}
			}
		}
		return false;
	}
	
	public String toString(){
		return "Augemented network : \n"+super.toString();
	}
	
}
