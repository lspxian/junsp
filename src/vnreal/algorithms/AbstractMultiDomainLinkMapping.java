package vnreal.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import li.multiDomain.Domain;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public abstract class AbstractMultiDomainLinkMapping {
	protected List<Domain> domains;
	protected String localPath ;
	protected String remotePath ;
	public AbstractMultiDomainLinkMapping(List<Domain> domains){
		this.domains = new ArrayList<Domain>();
		this.domains.addAll(domains);
		this.localPath = "cplex/vne-mcf.lp";
		this.remotePath = "pytest/vne-mcf.lp";
	}
	public AbstractMultiDomainLinkMapping(List<Domain> domains,String localPath, String remotePath){
		this.domains = new ArrayList<Domain>();
		this.domains.addAll(domains);
		this.localPath = localPath;
		this.remotePath = remotePath;
	}
	public abstract boolean linkMapping(VirtualNetwork vNet,
			Map<VirtualNode, SubstrateNode> nodeMapping);
}
