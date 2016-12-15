package protectionProba;

import java.util.Map;

import vnreal.algorithms.AbstractLinkMapping;
import vnreal.algorithms.linkmapping.MultiCommodityFlow;
import vnreal.algorithms.nodemapping.AvailableResourcesNodeMapping;
import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.virtual.VirtualNetwork;
/*
 * try bakcup with current primary mapping, if no solution,
 * redo if with protection enabled primary mapping
 */
public class CSP_PE extends AbstractBackupMapping {

	public CSP_PE(SubstrateNetwork sNet) {
		super(sNet);
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<BandwidthDemand, SubstrateLink> primary) {
		AbstractBackupMapping backup= new ConstraintSPLocalShare(sNet);
		if(backup.linkMapping(vNet, primary))
			return true;
		else{
			AvailableResourcesNodeMapping arnm = new AvailableResourcesNodeMapping(sNet,1,true,false);
			if(arnm.nodeMapping(vNet)){
				AbstractLinkMapping primaryMapping= new ProtectionEnabledPrimaryMapping(sNet);
				if(primaryMapping.linkMapping(vNet, arnm.getNodeMapping())){
					backup = new CSP_Proba(sNet); 
					backup.linkMapping(vNet, primaryMapping.getMapping());
					this.probability=backup.getProbability();
					return true;
					/*
					backup=new ConstraintSPLocalShare(sNet);
					if(backup.linkMapping(vNet, primaryMapping.getMapping()))
						return true;
					else{
					/*	arnm = new AvailableResourcesNodeMapping(sNet,1,true,false);
						arnm.nodeMapping(vNet);
						primaryMapping= new MultiCommodityFlow(sNet);
						primaryMapping.linkMapping(vNet, arnm.getNodeMapping());
						backup=new CSP_Proba(sNet);
						return backup.linkMapping(vNet, primaryMapping.getMapping());
						
					}*/
				}
			}
		}
		return false;
	}

}
