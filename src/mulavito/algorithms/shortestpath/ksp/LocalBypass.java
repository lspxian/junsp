package mulavito.algorithms.shortestpath.ksp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.filters.EdgePredicateFilter;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;

public class LocalBypass<V,E> extends Yen<V,E>{
	public LocalBypass(Graph<V, E> graph, Transformer<E, Number> nev) {
		super(graph, nev);
	}

	@Override
	protected List<List<E>> getShortestPathsIntern(V source, V target, int k) {
		return super.getShortestPathsIntern(source, target, k);
	}
	
	public List<List<E>> getShortestPaths(E link,int k){
		V source = graph.getSource(link);
		V dest = graph.getDest(link);
		//Set<E> blocked = new HashSet<E>();
		//blocked.add(link);
		EdgePredicateFilter<V, E> filter = new EdgePredicateFilter<V, E>(
				new Predicate<E>() {
					@Override
					public boolean evaluate(E e) {
						return !e.equals(link);
						//return !blocked.contains(e);
					}
				});
		this.graph =  filter.transform(graph);
		this.dijkstra = new DijkstraShortestPath<V, E>(graph, nev);
		return super.getShortestPaths(source, dest, k);
	}
	
}
