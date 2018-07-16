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
	private List<Integer> vectorRepresentation;

	private String varName;
	private int functionCode;

	public StarGraph(StarGraph sg) {
		this.edges = sg.edges;
		this.vectorRepresentation = sg.vectorRepresentation;
		this.functionCode = sg.functionCode;
		this.varName = sg.varName;
	}

	public StarGraph(StarGraph sg, String rel) {
		this.functionCode = sg.functionCode;
		this.varName = sg.varName;
		this.edges = new HashSet<>();
		for (Edge e : sg.edges)
			if (e.getRel().equals(rel)) {
				this.edges.add(e);
			}
		this.updateVectorRepresentation(this.edges);
	}

	private boolean isNotInList(ArrayList<String> rels, String rel) {
		for (String r : rels)
			if (r.equals(rel)) return false;
		return true;
	}
	public StarGraph(StarGraph sg, ArrayList<String> rels, boolean isRemoved) {
		this.functionCode = sg.functionCode;
		this.varName = sg.varName;
		this.edges = new HashSet<>();
		// isRemoved : removed all rels edges
		if (isRemoved) {
			for (Edge e : sg.edges)
				if (isNotInList(rels, e.getRel())) {
					this.edges.add(e);
				}
		} else {
			for (Edge e : sg.edges)
				if (!isNotInList(rels, e.getRel())) {
					this.edges.add(e);
				}
		}
		this.updateVectorRepresentation(this.edges);
	}

	public StarGraph(HashSet<Edge> edges, String varNameFunction) {
		this.edges = edges;
		String[] tmp = varNameFunction.split("-");
		this.varName = tmp[0];

		if (tmp.length == 2) functionCode = Integer.parseInt(tmp[1]);
		else
			functionCode = Integer.parseInt(tmp[2]);

		updateVectorRepresentation(edges);
	}

	private void updateVectorRepresentation(HashSet<Edge> edges) {
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
			if (j >= lenSg) break;
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


	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append("StarGraphInFo : ").append(this.varName).append("-").append(this.functionCode).append(" (").append(getSizeGraph()).append(")\n");
		for (Edge e : edges)
			res.append(e.toString()).append("\n");
		return res.toString();
	}
}
