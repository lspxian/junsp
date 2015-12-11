package li.multiDomain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import li.gt_itm.Generator;
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
	
	public Domain(int x, int y, boolean randomResource) throws IOException{
		this();
		this.coordinateX = x;
		this.coordinateY = y;
		Generator.createSubNet();
		this.alt2network("./gt-itm/sub");
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
			result += l.toString();
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
	
	public Collection<SubstrateLink> getAllLinks(){
		Collection<SubstrateLink> result =  new ArrayList<SubstrateLink>();
		result.addAll(this.getEdges());
		result.addAll(this.getInterLink());
		return result;
	}
	
	public double cumulatedBWCost(SubstrateNode sn1, SubstrateNode sn2){
		Transformer<SubstrateLink, Double> weightTrans = new Transformer<SubstrateLink,Double>(){
			public Double transform(SubstrateLink link){
				for(AbstractResource ares : link){
					if(ares instanceof BandwidthResource){
						BandwidthResource bwres = (BandwidthResource)ares;
						return 1/(bwres.getAvailableBandwidth()+0.001);
					}
				}
				return 0.;
			}
		};
		DijkstraShortestPath<SubstrateNode, SubstrateLink> dijkstra = new DijkstraShortestPath<SubstrateNode, SubstrateLink>(this, weightTrans);
		return (double) dijkstra.getDistance(sn1, sn2);
		 
		
	}
}

