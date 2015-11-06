package li.multiDomain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
		this.copy(sn);
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
	
	public Domain deepCopy(){
		return new Domain(this.getCopy(),this.coordinateX,this.coordinateY);
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

