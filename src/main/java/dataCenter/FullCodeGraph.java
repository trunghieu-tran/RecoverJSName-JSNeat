package dataCenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Harry Tran on 6/17/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class FullCodeGraph {
	private ArrayList<Edge> edges = new ArrayList<>();
	private HashMap<String, PartialCodeGraph> partialGraphMap = new HashMap<>();
	private HashSet<String> varNameSet = new HashSet<>();

	public FullCodeGraph(ArrayList<Edge> edges) {
		this.edges = edges;
		for (Edge e : this.edges) {
			varNameSet.add(e.getVarName());
		}
		buildVectorRepresent();
//		Collections.sort(vectorRepresent);
	}

	private void buildVectorRepresent() {
		HashMap<String, ArrayList<PartialEdge> > tmp = new HashMap<>();
		for (Edge e: edges) {
			PartialEdge pe = new PartialEdge(e.getObserved(), e.getRel());
			if (tmp.containsKey(e.getVarName())) {
				tmp.get(e.getVarName()).add(pe);
			} else {
				ArrayList<PartialEdge> peList = new ArrayList<>();
				peList.add(pe);
				tmp.put(e.getVarName(), peList);
			}
		}
		for (String str: varNameSet) {
			PartialCodeGraph pg = new PartialCodeGraph(tmp.get(str), str);
			partialGraphMap.put(str, pg);
		}
	}

	public boolean contains(PartialCodeGraph g, boolean isExact) {
		boolean res = false;
		if (isExact) {
			String center = g.getCenter();
			if (partialGraphMap.containsKey(center)) {
				res = partialGraphMap.get(center).contains(g);
			}
		} else {
			for (String center: varNameSet) {
				res = partialGraphMap.get(center).contains(g);
				if (res) break;
			}
		}
		return res;

	}

	public boolean isEmptyGraph() {
		return edges.isEmpty();
	}
	public static void main(String[] args) {

		Edge e1 = new Edge("v1", "p1", "r1");
		Edge e2 = new Edge("v1", "p3", "r1");
		Edge e3 = new Edge("v1", "p2", "r2");
		Edge e4 = new Edge("v1", "v2", "r0");
		Edge e5 = new Edge("v2", "v1", "r0");
		Edge e6 = new Edge("v2", "p2", "r2");
		Edge e7 = new Edge("v2", "p4", "r1");
		ArrayList<Edge> es = new ArrayList<>();
		es.add(e1); es.add(e2); es.add(e3); es.add(e4); es.add(e5); es.add(e6); es.add(e7);
		FullCodeGraph fg = new FullCodeGraph(es);

		PartialEdge p1 = new PartialEdge("p2", "r2");
		PartialEdge p2 = new PartialEdge("p3", "r1");
		ArrayList<PartialEdge> pe = new ArrayList<>();
		pe.add(p1); pe.add(p2);
		PartialCodeGraph pg = new PartialCodeGraph(pe, "v22");

		System.out.println(fg.contains(pg, false));
	}
}
