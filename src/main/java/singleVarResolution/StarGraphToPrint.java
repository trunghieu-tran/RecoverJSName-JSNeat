package singleVarResolution;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @author Mike Tran on 7/8/18.
 * @project RecoverJSName
 * For parser and printing
 */
public class StarGraphToPrint {
	private HashSet<Edge> edges;
	private List<Integer> vectorRepresentation;

	private String varName;
	private int functionCode;

	public StarGraphToPrint(StarGraphToPrint sg) {
//		this.edges = sg.edges;
		this.vectorRepresentation = sg.vectorRepresentation;
		this.functionCode = sg.functionCode;
		this.varName = sg.varName;
	}

	public StarGraphToPrint(HashSet<Edge> edges, String varNameFunction) {
		this.edges = edges;
		String[] tmp = varNameFunction.split("-");
		this.varName = tmp[0];

		if (tmp[1].isEmpty()) {
			tmp[1] = "0";
		}
		this.functionCode = Integer.parseInt(tmp[1]);
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


	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append("StarGraphInFo : ").append(this.varName).append("-").append(this.functionCode).append(" (").append(getSizeGraph()).append(")\n");
//		for (Edge e : edges)
//			res.append(e.toString()).append("\n");
		return res.toString();
	}
}
