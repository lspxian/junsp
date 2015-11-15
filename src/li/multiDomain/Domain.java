package li.multiDomain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import vnreal.network.substrate.InterLink;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.resources.AbstractResource;

/*
 * 
 * 
 */
public class Domain extends SubstrateNetwork{
	
	protected ArrayList<InterLink> interLink;
	protected int coordinateX;
	protected int coordinateY;

	
	public Domain() {
		super(false);
		interLink = new ArrayList<InterLink>();
	}
	
	public Domain(int x, int y, String filePath, boolean randomResource) throws IOException{
		this();
		this.coordinateX = x;
		this.coordinateY = y;
		this.alt2network(filePath);
		this.addAllResource(randomResource);
	}
	
	public Domain(SubstrateNetwork sn, int x, int y){
		this();
		sn.getCopy(this);
		this.coordinateX = x;
		this.coordinateY = y;
	}
	
	public ArrayList<InterLink> getInterLink() {
		return interLink;
	}

	public void setInterLink(ArrayList<InterLink> interLink) {
		this.interLink = interLink;
	}

	public int getCoordinateX() {
		return coordinateX;
	}

	public void setCoordinateX(int coordinateX) {
		this.coordinateX = coordinateX;
	}

	public int getCoordinateY() {
		return coordinateY;
	}

	public void setCoordinateY(int coordinateY) {
		this.coordinateY = coordinateY;
	}
	
	public void addInterLink(InterLink il){
		this.interLink.add(il);
	}
	
	public Domain getDeepCopy(){
		Domain result = new Domain();
		getCopy(true, result);
		return result;
	}
	
	/*in one domain, the inter link contains the node of other domain, so we can't get 
	 * copy of a domain with its local information
	 * so this is not correct
	 */
	public void getCopy(boolean deepCopy, Domain result){
		HashMap<String, SubstrateNode> map = new HashMap<String, SubstrateNode>();

		LinkedList<SubstrateLink> originalLinks = new LinkedList<SubstrateLink>(getEdges());
		LinkedList<InterLink> originalInterLinks = new LinkedList<InterLink>(getInterLink());
		
		SubstrateNode tmpSNode, tmpDNode;
		SubstrateLink tmpSLink;
		InterLink tmpILink;
		for (Iterator<SubstrateNode> tempSubsNode = getVertices().iterator(); tempSubsNode
				.hasNext();) {
			tmpSNode = tempSubsNode.next();
			if (deepCopy) {
				SubstrateNode clone = tmpSNode.getCopy();
				result.addVertex(clone);
				map.put(tmpSNode.getName(), clone);
			} else {
				result.addVertex(tmpSNode);
			}
		}
		
		for (Iterator<SubstrateLink> tempItSubLink = originalLinks.iterator(); tempItSubLink
				.hasNext();) {
			tmpSLink = tempItSubLink.next();
			
			tmpSNode = this.getEndpoints(tmpSLink).getFirst();
			tmpDNode = this.getEndpoints(tmpSLink).getSecond();

			if (deepCopy) {
				result.addEdge(tmpSLink.getCopy(),
						map.get(tmpSNode.getName()),
						map.get(tmpDNode.getName()));
			} else {
				result.addEdge(tmpSLink, tmpSNode, tmpDNode);
			}
		}
		
		for(Iterator<InterLink> tempItInterLink = originalInterLinks.iterator();tempItInterLink.hasNext();){
			tmpILink = tempItInterLink.next();
			
			tmpSNode = tmpILink.getNode1();
			tmpDNode = tmpILink.getNode2();
			
			if(deepCopy){
				result.addInterLink(tmpILink.getCopy(map.get(tmpSNode.getName()),map.get(tmpDNode.getName())));
			}else{
				result.addInterLink(tmpILink);
			}
		}
		
	}

	
	public String toString(){
		String result= "Domain("+this.getCoordinateX()+","+this.getCoordinateY()+") : \n";
		result+=super.toString();
		for (InterLink l : interLink) {			
			result += l + "  (" + l.getNode1().getId() + "<->"
					+ l.getNode2().getId() + ") \n";
			for (AbstractResource r : l.get()) {
				result += "  " + r.toString() + "\n";
			}
		}
		
		return result;
	}
	
}

