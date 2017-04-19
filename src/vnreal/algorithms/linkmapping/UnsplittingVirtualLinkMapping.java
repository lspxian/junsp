package vnreal.algorithms.linkmapping;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import com.ampl.AMPL;
import com.ampl.DataFrame;
import vnreal.algorithms.AbstractLinkMapping;
import vnreal.algorithms.utils.Consts;
import vnreal.algorithms.utils.MiscelFunctions;
import vnreal.algorithms.utils.NodeLinkAssignation;
import vnreal.algorithms.utils.dataSolverFile;
import vnreal.demands.AbstractDemand;
import vnreal.demands.BandwidthDemand;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;
/**
 * 
 * @author LI
 * This class implements the unsplitting Multi-commodity flow problem with AMPL java api locally.
 * Given the data and the model file, ampl solves the problem with integrated Cplex.
 * But this version of cplex is a student version, which limit the number of variables and constraints.
 * So it can't solve all the VNE problem.
 * The AMPL in the lib directory of this project is a windows version, so this class will not work in Linux
 */
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
		SubstrateLink tSLink;
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
					
					//the element of dataframe is a type of java.lang.Double, but represented as Object
					
					for(int i=0;i<dataframe.getNumRows();i++){
						Object[] line = dataframe.getRowByIndex(i);
						if((int)(double)line[4]!=0&&srcSnode.getId()==(int)(double)line[0]&&dstSnode.getId()==(int)(double)line[1]){
							
							for(SubstrateNode n : sNet.getVertices()){
								if(n.getId()==(int)(double)line[2])
									tSNode = n;
								if(n.getId()==(int)(double)line[3])
									tDNode = n;
							}
							tSLink = sNet.findEdge(tSNode, tDNode);
							newBwDem = new BandwidthDemand(tmpl);
							newBwDem.setDemandedBandwidth(MiscelFunctions
									.roundThreeDecimals(originalBwDem.getDemandedBandwidth()));

							if(!NodeLinkAssignation.vlmSingleLinkSimple(newBwDem, tSLink)){
								ampl.close();
								throw new AssertionError("But we checked before!");
							}		
							
						}
						
					}
					
				}
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		ampl.close();

		return true;
	}
}
