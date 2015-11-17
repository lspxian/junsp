package li.multiDomain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import vnreal.network.substrate.InterLink;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNode;

public class MultiDomainUtil {
	
	/*in one domain, the inter link contains the node of other domain, so we can't get 
	 * copy of a domain with its local information
	 * 
	 */
	public static List<Domain> getCopy(boolean deepCopy, List<Domain> multiDomain){
		List<Domain> result = new ArrayList<Domain>();
		
		HashMap<String, SubstrateNode> map = new HashMap<String, SubstrateNode>();
		
		SubstrateNode tmpSNode, tmpDNode;
		SubstrateLink tmpSLink;
		InterLink tmpILink;
		Domain d, tmpResult;
		
		//node copy
		for(int i = 0 ; i<multiDomain.size();i++){
			result.add(new Domain());
			d = multiDomain.get(i);
			tmpResult = result.get(i);
			for (Iterator<SubstrateNode> tempSubsNode = d.getVertices().iterator(); tempSubsNode
					.hasNext();) {
				tmpSNode = tempSubsNode.next();
				if (deepCopy) {
					SubstrateNode clone = tmpSNode.getCopy();
					tmpResult.addVertex(clone);
					map.put(tmpSNode.getName(), clone);
				} else {
					tmpResult.addVertex(tmpSNode);
				}
			}
		}

		//link copy
		for(int i = 0 ; i<multiDomain.size();i++){
			d = multiDomain.get(i);
			tmpResult = result.get(i);
			
			LinkedList<SubstrateLink> originalLinks = new LinkedList<SubstrateLink>(d.getEdges());
			LinkedList<InterLink> originalInterLinks = new LinkedList<InterLink>(d.getInterLink());
			
			for (Iterator<SubstrateLink> tempItSubLink = originalLinks.iterator(); tempItSubLink
					.hasNext();) {
				tmpSLink = tempItSubLink.next();
				
				tmpSNode = d.getEndpoints(tmpSLink).getFirst();
				tmpDNode = d.getEndpoints(tmpSLink).getSecond();
				
				if (deepCopy) {
					tmpResult.addEdge(tmpSLink.getCopy(),
							map.get(tmpSNode.getName()),
							map.get(tmpDNode.getName()));
				} else {
					tmpResult.addEdge(tmpSLink, tmpSNode, tmpDNode);
				}
			}
			
			for(Iterator<InterLink> tempItInterLink = originalInterLinks.iterator();tempItInterLink.hasNext();){
				tmpILink = tempItInterLink.next();
				
				tmpSNode = tmpILink.getNode1();
				tmpDNode = tmpILink.getNode2();
				
				if(deepCopy){
					tmpResult.addInterLink(tmpILink.getCopy(map.get(tmpSNode.getName()),map.get(tmpDNode.getName())));
				}else{
					tmpResult.addInterLink(tmpILink);
				}
			}
			
		}
		
		return result;
	}
	
	public static void randomInterLinks(List<Domain> multiDomain){
		//max distance for all the domains
		double maxDistance=0, distance=0, ax,ay,bx,by;
		for(int i=0;i<multiDomain.size();i++){
			Domain startDomain = multiDomain.get(i);
			for(int j=i;j<multiDomain.size();j++){
				Domain endDomain = multiDomain.get(j);
				for(SubstrateNode start : startDomain.getVertices()){
					for(SubstrateNode end : endDomain.getVertices()){
						ax = start.getCoordinateX()+startDomain.getCoordinateX()*100;
						ay = start.getCoordinateY()+startDomain.getCoordinateY()*100;
						bx = end.getCoordinateX()+endDomain.getCoordinateX()*100;
						by = end.getCoordinateY()+endDomain.getCoordinateY()*100;
						distance = Math.sqrt(Math.pow(ax-ay, 2) + Math.pow(bx-by, 2));
						if(distance>maxDistance)	maxDistance = distance;
						
					}
				}
				
			}
		}
		
		//generate inter links
		double alpha = 0.5;	//alpha increases the probability of edges between any nodes in the graph
		double beta = 0.2;	//beta yields a larger ratio of long edges to short edges.
		for(int i=0;i<multiDomain.size();i++){
			Domain startDomain = multiDomain.get(i);
			for(int j=i+1;j<multiDomain.size();j++){
				Domain endDomain = multiDomain.get(j);
				//for each pair of domain, generate inter links by Waxman
				for(SubstrateNode start : startDomain.getVertices()){
					for(SubstrateNode end : endDomain.getVertices()){
						ax = start.getCoordinateX()+startDomain.getCoordinateX()*100;
						ay = start.getCoordinateY()+startDomain.getCoordinateY()*100;
						bx = end.getCoordinateX()+endDomain.getCoordinateX()*100;
						by = end.getCoordinateY()+endDomain.getCoordinateY()*100;
						distance = Math.sqrt(Math.pow(ax-ay, 2) + Math.pow(bx-by, 2));
						double proba = alpha * Math.exp(-distance/beta/maxDistance);
						if(proba>new Random().nextDouble()){
							//source, destination, destination domain, random resource 
							InterLink il = new InterLink(start, end, false);
							startDomain.addInterLink(il);
							endDomain.addInterLink(il);
						}
					}
				}
			}
		}
	}
	
	
	public static void staticInterLinks(Domain d1, Domain d2){
		InterLink il = new InterLink(d1.getNodeFromID(0),d2.getNodeFromID(40),false);
		d1.addInterLink(il);
		d2.addInterLink(il);
		il = new InterLink(d1.getNodeFromID(2),d2.getNodeFromID(47),false);
		d1.addInterLink(il);
		d2.addInterLink(il);
		il = new InterLink(d1.getNodeFromID(3),d2.getNodeFromID(45),false);
		d1.addInterLink(il);
		d2.addInterLink(il);
		il = new InterLink(d1.getNodeFromID(4),d2.getNodeFromID(47),false);
		d1.addInterLink(il);
		d2.addInterLink(il);
		il = new InterLink(d1.getNodeFromID(5),d2.getNodeFromID(47),false);
		d1.addInterLink(il);
		d2.addInterLink(il);
		il = new InterLink(d1.getNodeFromID(8),d2.getNodeFromID(39),false);
		d1.addInterLink(il);
		d2.addInterLink(il);
		il = new InterLink(d1.getNodeFromID(9),d2.getNodeFromID(46),false);
		d1.addInterLink(il);
		d2.addInterLink(il);
	}
	
	//delete all resource
	public static boolean reset(List<Domain> multiDomain){
		for(Domain d : multiDomain)
			d.reset();
		return true;
	}
}