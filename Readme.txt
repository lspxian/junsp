/*---------Pre-installation-------------*/
Cplex for linux

/*-------------------files---------------------*/
/cplex/mysolver.py	--cplex solver.
/data	 --pre-defined networks data
/evaluationData	--data for each metric
/gt-itm	--gt itm version linux to generate random networks
/lib	--library
/sndlib	--sndlib network topologies

/*-----------------class------------------*/
li.gt_itm.Generator generate substrate and virtual networks.
More details of gt-itm can be found in  gt-itm documents.
The number of substrate and virtual nodes can be configured by input parameters.

vnreal.network.substrate.SubstrateLink are resources related to substrate link.
probability can be constant or random.
bw.setBandwidth configures the range of bandwidth.


multi domain simulation:
In li.simulation.Centralized_MD_VNE, we can control the following parameters:
-simulationTime;
-multiDomain.add() can add different substrate networks presented in sndlib;
-MultiDomainUtil.randomInterLinks generates inter-domain links;
-Generator.createVirNet() creates virtual network by GT-ITM;
-vn.alt2network() add the virtual network to the queue;
-metrics.add can add the metrics to the simulation.

To simulate the impact of different variables,
1 lambda:  li.simulation.Centralized_MD_VNE_SimulationMain
2 virtual demand: li.simulation.Centralized_MD_VNE_Demand_Main
3 peering links: li.simulation.Centralized_MD_VNE_InterLink_Main

In each of the mentioned main class (for example Centralized_MD_VNE_SimulationMain)
variable c is the repetition times of simulation. the typical value is 10.
The initialization of Centralized_MD_VNE_Simulation has 3 parameters.
The first one determines whether the substrate networks are random generated.
The second and third one are alpha and beta, which aim to generate the inter-domain links.
Variable i is lambda.

The class "MultiDomainAsOneDomain" is the "ideal" method in the articles/thesis.
The class "shen2014" is the method "shen" in the articles/thesis.
The class "MultiDomainRanking2" is "ciplm".
The class "MultiDomainRanking3" is "ciplm_up".

To show the performance of reinforced method, use li.simulation.Distribute3DVNEMain
---------------------------

The simulations for failure avoidance are li.simulation.ProbabilitySimulationMain and ProbabilitySimulation_sn_Main
li.simulation.ProbabilitySimulationMain simulates lambda.
li.simulation.ProbabilitySimulation_sn_Main simulates number of substrate nodes.

In each of the main class,
The class "ShortestPathBW" is "bw" in the articles/thesis.
The class "PBBWExact" is "Exact".
The class "ProbaHeuristic3" is "Basic".
The class "ProbaHeuristic4" is "reinforced".


---------------------------------
failure protection:
li.simulation.ProtectionSimMain for lambda
li.simulation.ProtectionPercentMain for primary percent.
use percentList.add() to configure the percentage of primary bw in the whole capacity.
In each of the main class,
simulation.runSimulation() indicates the primary and backup method.
("SPWithoutBackupVF", "included") is Basic_FP
("ShortestPathBackupVF","included") is SOL_FP
("SPWithoutBackupVF2", "included") is Basic_BEP
("ShortestPathBackupVF2","included") is SOL_BEP
("MaxFlowBackupVF2", "included") is MFP_BEP


/*------------result--------------*/
The simulation will produce files named "result.txt", "cenResult.txt", "disResult.txt".
Use the plot files in the thesis directory to draw the figures of the article/thesis.


