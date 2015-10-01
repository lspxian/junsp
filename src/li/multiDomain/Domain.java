package li.multiDomain;

import java.util.ArrayList;
import java.util.List;

import edu.uci.ics.jung.graph.util.Pair;
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

	public Domain() {
		super(false);
		interLink = new ArrayList<InterLink>();
	}

	public Domain(boolean autoUnregisterConstraints) {
		super(autoUnregisterConstraints);
	}
	
	public Domain(boolean autoUnregisterConstraints, boolean directed) {
		super(autoUnregisterConstraints, directed);
	}

	
	public ArrayList<InterLink> getInterLink() {
		return interLink;
	}

	public void setInterLink(ArrayList<InterLink> interLink) {
		this.interLink = interLink;
	}
	
	public void addInterLink(SubstrateLink sl, SubstrateNode source, SubstrateNode dest, Domain destDomain){
		this.addVertex(source);
		this.interLink.add(new InterLink(sl, source, dest, destDomain));
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

