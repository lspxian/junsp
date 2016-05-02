package vnreal.algorithms.linkmapping;

import java.util.List;
import java.util.Map;

import li.multiDomain.Domain;
import vnreal.algorithms.AbstractMultiDomainLinkMapping;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class AllPossibleMDRanking extends AbstractMultiDomainLinkMapping{
	
	
	
	public AllPossibleMDRanking(List<Domain> domains) {
		super(domains);
	}
	public AllPossibleMDRanking(List<Domain> domains, String localPath, String remotePath) {
		super(domains, localPath, remotePath);
	}
	
	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<VirtualNode, SubstrateNode> nodeMapping) {
		

		return false;
	}

}
