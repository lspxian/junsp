package li.multiDomain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import vnreal.algorithms.utils.NodeLinkDeletion;
import vnreal.network.substrate.InterLink;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.resources.AbstractResource;
import vnreal.resources.BandwidthResource;

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
	
	public boolean reset(){
		NodeLinkDeletion.resetNet(this);
		for(InterLink slink : this.getInterLink()){
			for(AbstractResource res : slink){
				if(res instanceof BandwidthResource){
					((BandwidthResource) res).reset();
				}
			}
		}
		return true;
	}
}

