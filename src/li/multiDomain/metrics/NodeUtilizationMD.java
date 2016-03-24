package li.multiDomain.metrics;

import java.io.IOException;
import java.util.ArrayList;

import li.multiDomain.AbstractMultiDomain;
import li.multiDomain.Domain;
import vnreal.demands.AbstractDemand;
import vnreal.demands.CpuDemand;
import vnreal.mapping.Mapping;
import vnreal.network.substrate.SubstrateNode;
import vnreal.resources.AbstractResource;
import vnreal.resources.CpuResource;

public class NodeUtilizationMD extends MetricMD {
	public static double capacity = 0.0,sum=0.0;

	public NodeUtilizationMD(AbstractMultiDomain simulation, String method) throws IOException {
		super(simulation, method);
	}

	@Override
	public String name() {
		return "NodeUtilization";
	}

	@Override
	public double calculate() {
		ArrayList<SubstrateNode> allNodes = new ArrayList<SubstrateNode>();
		for(Domain d : this.simulation.getMultiDomain()){
			allNodes.addAll(d.getVertices());
		}
		
		for (SubstrateNode sn : allNodes) {
			for (AbstractResource res : sn.get()) {
				capacity += ((CpuResource) res).getCycles();
			}
		}
		for (SubstrateNode sn : allNodes) {
			for (AbstractResource res : sn.get()) {
				for (Mapping m : res.getMappings()) {
					AbstractDemand dem = m.getDemand();

					if (dem instanceof CpuDemand) {
						sum += ((CpuDemand) dem).getDemandedCycles();
						
					}
				}
			}
		}

		return sum/capacity;
	}

}
