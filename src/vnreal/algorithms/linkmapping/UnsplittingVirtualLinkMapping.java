package vnreal.algorithms.linkmapping;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ampl.AMPL;
import com.ampl.DataFrame;
import com.ampl.Variable;

import vnreal.algorithms.AbstractLinkMapping;
import vnreal.algorithms.utils.Consts;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.algorithms.utils.dataSolverFile;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public class UnsplittingVirtualLinkMapping extends AbstractLinkMapping{
	private double wBw, wCpu;

	public UnsplittingVirtualLinkMapping(SubstrateNetwork sNet,
			double cpuWeight, double bwWeight) {
		super(sNet);
		this.wBw = bwWeight;
		this.wCpu = cpuWeight;
	}

	@Override
	public boolean linkMapping(VirtualNetwork vNet,Map<VirtualNode, SubstrateNode> nodeMapping) {
		
		BandwidthDemand originalBwDem = null, newBwDem;
		SubstrateNode srcSnode = null;
		SubstrateNode dstSnode = null;
		SubstrateNode tSNode = new SubstrateNode();
		SubstrateNode tDNode = new SubstrateNode();
		
		String dataFileName = "datafile.dat";
		dataSolverFile lpLinkMappingData = new dataSolverFile(Consts.LP_SOLVER_FOLDER + dataFileName);
		lpLinkMappingData.createDataSolverFile(sNet, null, vNet, nodeMapping,wBw, wCpu, false, 0);
		
		AMPL ampl = new AMPL();
		try {
			ampl.read(Consts.LP_SOLVER_FOLDER+Consts.LP_LINKMAPPING_MODEL);
			ampl.readData(Consts.LP_SOLVER_FOLDER + dataFileName);
			ampl.setOption("solver", "cplex");
			ampl.solve();
			DataFrame dataframe = ampl.getVariable("lambda").getValues();
			
			for (Iterator<VirtualLink> links = vNet.getEdges().iterator(); links
					.hasNext();) {
				VirtualLink tmpl = links.next();
				mappedLinks++; // increase number of processed.

				// Find the source and destiny of the current VirtualLink (tmpl)
				VirtualNode srcVnode = vNet.getSource(tmpl);
				VirtualNode dstVnode = vNet.getDest(tmpl);

				// Find their mapped SubstrateNodes
				srcSnode = nodeMapping.get(srcVnode);
				dstSnode = nodeMapping.get(dstVnode);
				
				if (!srcSnode.equals(dstSnode)) {
					// Get current VirtualLink demand
					for (AbstractDemand dem : tmpl) {
						if (dem instanceof BandwidthDemand) {
							originalBwDem = (BandwidthDemand) dem;
							break;
						}
					}
					
					for (Iterator<List<String>> cad = solverResult.keySet()
							.iterator(); cad.hasNext();) {
						List<String> tmpValues = cad.next();
						Double vtmp = MiscelFunctions
								.roundTwelveDecimals(solverResult
										.get(tmpValues));

						if (srcSnode.getId() == Integer.parseInt(tmpValues
								.get(0))
								&& dstSnode.getId() == Integer
										.parseInt(tmpValues.get(1))
								&& vtmp != 0) {
							for (SubstrateNode n : sNet.getVertices()) {
								if (Integer.parseInt(tmpValues.get(2)) == n
										.getId()) {
									tSNode = n;
								} else {
									if (Integer.parseInt(tmpValues.get(3)) == n
											.getId()) {
										tDNode = n;
									}
								}
							}
						}
					
				}
			}
			
			
			System.out.println(dataframe);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ampl.close();

		return false;
	}
}
