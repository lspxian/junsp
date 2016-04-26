package li.SteinerTree;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class Cycle {
	
	private SubstrateNetwork sn;
	private Map<SubstrateNode, Boolean> visited;
	
	
	public Cycle(SubstrateNetwork sn){
		this.sn=sn;
		this.visited = new HashMap<SubstrateNode,Boolean>();
		// Mark all the vertices as not visited and not part of
		for(SubstrateNode snode : this.sn.getVertices())
			visited.put(snode, false);
	}
	
	private Boolean isCyclicUtil(SubstrateNode v, SubstrateNode parent)
	{
		// Mark the current node as visited
		visited.replace(v, true);
		SubstrateNode tempo;

		// Recur for all the vertices adjacent to this vertex
		Iterator<SubstrateNode> it = this.sn.getNeighbors(v).iterator();
		while (it.hasNext())
		{
			tempo = it.next();

			// If an adjacent is not visited, then recur for that
			// adjacent
			if (!visited.get(tempo))
			{
				if (isCyclicUtil(tempo, v))
					return true;
			}

			// If an adjacent is visited and not parent of current
			// vertex, then there is a cycle.
			else if (!tempo.equals(parent))
				return true;
		}
		return false;
	}

	// Returns true if the graph contains a cycle, else false.
	public Boolean isCyclic()
	{
		// Call the recursive helper function to detect cycle in
		// different DFS trees
		
		for(SubstrateNode snode : sn.getVertices())
			if(!this.visited.get(snode))	// Don't recur for u if already visited
				if(this.isCyclicUtil(snode, null))
					return true;
		
		return false;
	}
	
}
