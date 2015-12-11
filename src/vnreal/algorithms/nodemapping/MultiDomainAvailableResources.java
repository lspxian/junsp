package vnreal.algorithms.nodemapping;

import java.util.List;

import li.multiDomain.Domain;
import vnreal.algorithms.AbstractMultiDomainNodeMapping;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class MultiDomainAvailableResources extends AbstractMultiDomainNodeMapping {
	int distance;

	public MultiDomainAvailableResources(List<Domain> multiDomain,int distance) {
		super(multiDomain);
		this.distance = distance;
	}

	@Override
	public boolean nodeMapping(VirtualNetwork vNet) {
		for(Domain domain : multiDomain){
			VirtualNetwork tmpvl = new VirtualNetwork(1);
			//
			for(VirtualNode vnode : vNet.getVertices()){
				if(domain.getCoordinateX()<=vnode.getCoordinateX()/100.0&&
						vnode.getCoordinateX()/100.0<domain.getCoordinateX()+1&&
						domain.getCoordinateY()<=vnode.getCoordinateY()/100.0&&
						vnode.getCoordinateY()/100.0<domain.getCoordinateY()+1){
					vnode.setCoordinateX(vnode.getCoordinateX()%100);
					vnode.setCoordinateY(vnode.getCoordinateY()%100);
					vnode.setDomain(domain); 	//the virtual node belong to a domain
					tmpvl.addVertex(vnode);
				}
			}
			
			//sub virtual network node mapping
			AvailableResourcesNodeMapping arnm = new AvailableResourcesNodeMapping(domain,this.distance,true,false);
			if(arnm.nodeMapping(tmpvl)){
				System.out.println("node mapping succes, domain("+domain.getCoordinateX()+","+domain.getCoordinateY()+")");
				this.nodeMapping.putAll(arnm.getNodeMapping());
			}
			else{
				System.out.println("node resource error, domain("+domain.getCoordinateX()+","+domain.getCoordinateY()+")");
				for(VirtualNode vnode : vNet.getVertices()){
					if((vnode.getDomain()!=null)&&vnode.getDomain().equals(domain)){
						vnode.setCoordinateX(vnode.getCoordinateX()+domain.getCoordinateX()*100);
						vnode.setCoordinateY(vnode.getCoordinateY()+domain.getCoordinateY()*100);
					}
				}
				this.nodeMapping.clear();
				return false;
			}
			
			//reset coordinate of vn
			for(VirtualNode vnode : vNet.getVertices()){
				if((vnode.getDomain()!=null)&&vnode.getDomain().equals(domain)){
					vnode.setCoordinateX(vnode.getCoordinateX()+domain.getCoordinateX()*100);
					vnode.setCoordinateY(vnode.getCoordinateY()+domain.getCoordinateY()*100);
				}
			}
			
			
			
		}
		return true;
	}

}
