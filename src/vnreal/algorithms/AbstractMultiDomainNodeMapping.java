package vnreal.algorithms;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import li.multiDomain.Domain;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public abstract class AbstractMultiDomainNodeMapping {
	protected Map<VirtualNode, SubstrateNode> nodeMapping;
	protected List<Domain> multiDomain;
	
	public AbstractMultiDomainNodeMapping(List<Domain> multiDomain){
		this.multiDomain = multiDomain;
		this.nodeMapping = new LinkedHashMap<VirtualNode, SubstrateNode>();
	}
	
	public Map<VirtualNode, SubstrateNode> getNodeMapping() {
		return nodeMapping;
	}

	public abstract boolean nodeMapping(VirtualNetwork vNet);
}
