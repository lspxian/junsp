package li.multiDomain;

import java.util.ArrayList;
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
	
	public Domain(SubstrateNetwork sn, int x, int y){
		super(false);
		sn.getCopy(false, this);
		interLink = new ArrayList<InterLink>();
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

	public void addInterLink(SubstrateLink sl, SubstrateNode source, SubstrateNode dest, Domain destDomain){
		this.addVertex(source);
		this.interLink.add(new InterLink(sl, source, dest, destDomain));
	}
	
	public void getCopy(Domain result){
		super.getCopy(false, result);
		
	}
	
	public String toString(){
		String result= super.toString();
		result += "\nINTER LINK:\n";
		for (InterLink l : interLink) {
			result += l.getId() + "  (" + l.getSource().getId() + "<->"
					+ l.getDestination().getId() + ") \n";
			for (AbstractResource r : l.get()) {
				result += "  " + r.toString() + "\n";
			}
		}
		
		return result;
	}
	
}

