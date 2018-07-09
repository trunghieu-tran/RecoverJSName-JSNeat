package singleVarResolution;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @author Harry Tran on 7/8/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class StarGraph {
	private static double THRESHOLD = 0.8;
	private HashSet<Edge> edges;
	private List<Long> vectorRepresentation;

	private String varNameFunction;
	private String varName;

	public StarGraph(HashSet<Edge> edges, String varNameFunction) {
		this.edges = edges;
		this.varNameFunction = varNameFunction;

		this.varName = varNameFunction.split("-")[0];
		updateVectorRepresentation();
	}

	private void updateVectorRepresentation() {
		vectorRepresentation = new ArrayList<>();
		for (Edge e : edges) vectorRepresentation.add(e.hashCode);
		Collections.sort(vectorRepresentation);
	}

	public String getVarName() {
		return varName;
	}

	public int getSizeGraph() {
		return vectorRepresentation.size();
	}
	
	public HashSet<Edge> getEdges() {
		return edges;
	}

	// check whether the testing graph is similar to other graph from corpus
	public double getSimilarScoreTo(StarGraph sg) {
		int len = this.getSizeGraph();
		int lenSg = sg.getSizeGraph();

		if (len < lenSg * THRESHOLD) return -1.0;

		int i = 0, j = 0;
		int cnt = 0;
		while (i < len && j < lenSg) {
			long curr = vectorRepresentation.get(i);
			while (j < lenSg && sg.vectorRepresentation.get(j) < curr) ++j;
			if (j > lenSg) break;
			if (sg.vectorRepresentation.get(j) == curr) {
				cnt++;
				++i;
			}
			++j;
		}

		if (cnt >= len * THRESHOLD)
			return (double) cnt / len;
		else
			return -1.0;
	}

}
