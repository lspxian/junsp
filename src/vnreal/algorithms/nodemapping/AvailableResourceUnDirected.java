package vnreal.algorithms.nodemapping;

import java.util.ArrayList;

import vnreal.algorithms.AbstractNodeMapping;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class AvailableResourceUnDirected extends AbstractNodeMapping {
	
	private int distance;

	protected AvailableResourceUnDirected(SubstrateNetwork sNet, int distance) {
		super(sNet);
		this.distance=distance;
	}

	@Override
	public boolean nodeMapping(VirtualNetwork vNet) {
		
		for(VirtualNode vnode:vNet.getVertices()){
			
			ArrayList<SubstrateNode> candidats = new ArrayList<SubstrateNode>();
			for(SubstrateNode snode : sNet.getVertices()){
				double temDis = Math.pow(snode.getCoordinateX()-vnode.getCoordinateX(),2);
				temDis = temDis + Math.pow(snode.getCoordinateY()-vnode.getCoordinateY(),2);
				temDis = Math.sqrt(temDis);
				if(temDis<=distance)
					candidats.add(snode);
			}
			
			if(candidats.isEmpty())
				return false;
			else{
				double maxResource=0;
				SubstrateNode maxNode;
				for(SubstrateNode snode:candidats){
					//
				}
				
			}
			
		}

		
		return false;
	}

}
