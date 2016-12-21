package protectionProba;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.resources.BandwidthResource;

public class KSPLocalShare extends AbstractBackupMapping {

	public KSPLocalShare(SubstrateNetwork sNet) {
		super(sNet);
		this.sNet.precalculatedBackupPath(5);
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet, Map<BandwidthDemand, SubstrateLink> primary) {
		Map<BandwidthDemand,List<SubstrateLink>> resultB = new HashMap<BandwidthDemand,List<SubstrateLink>>();
		for(Map.Entry<BandwidthDemand, SubstrateLink> e:primary.entrySet()){
			List<SubstrateLink> backup = this.ComputeLocalBackupPath(sNet, e.getValue(), e.getKey(), true);
			System.out.println(e.getValue()+"#"+e.getKey()+" "+backup);
			if(!backup.isEmpty()){
				resultB.put(e.getKey(), backup);
				if(!NodeLinkAssignation.backup(e.getKey(),e.getValue(), backup, true))
					throw new AssertionError("But we checked before!");
				e.getValue().getBandwidthResource().getMapping(e.getKey()).setProtection(true);
			}
			else{
				System.out.println("no backup link");
				System.out.println(e.getValue().getBandwidthResource());
				for(Map.Entry<BandwidthDemand, List<SubstrateLink>> ent: resultB.entrySet()){	//free backup path of other virtual links
					NodeLinkDeletion.linkFreeBackup(ent.getKey(), ent.getValue(),true);
				}
				NodeLinkDeletion.freeResource(vNet, sNet);	//free primary
				return false;
			}
		}
		return true;
	}
	
	private List<SubstrateLink> ComputeLocalBackupPath(SubstrateNetwork sn, SubstrateLink sl, BandwidthDemand bwd, boolean share){
		for(List<SubstrateLink> path:sl.getKsp()){
			boolean pathFlag=true;
			for(SubstrateLink slink:path){
				boolean flag=false;
				BandwidthResource bdsrc = slink.getBandwidthResource();
				Risk risk=bdsrc.findRiskByLink(sl);
				if(risk==null){
					double additional = bdsrc.getAvailableBandwidth()+bdsrc.getReservedBackupBw()-bwd.getDemandedBandwidth();
					if(additional>=0)	flag=true;
				}
				else{
					double origTotal = bdsrc.maxRiskTotal();
					risk.addDemand(bwd);
					double newTotal = bdsrc.maxRiskTotal();
					risk.removeDemand(bwd);
					if((newTotal-origTotal)<=bdsrc.getAvailableBandwidth())
						flag=true;
				}
				if(flag==false){
					pathFlag=false;
					break;
				}
			}
			if(pathFlag==true) return path;
		}
		return null;
	
	}
}
