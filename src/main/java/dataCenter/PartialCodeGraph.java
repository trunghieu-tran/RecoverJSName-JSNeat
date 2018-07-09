package dataCenter;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Harry Tran on 6/16/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class PartialCodeGraph {
	private static final double THRESHOLD = 0.5;
	private String center;
	private ArrayList<PartialEdge> edges = new ArrayList<>();
	private ArrayList<Long> vectorRepresent = new ArrayList<>();

	public PartialCodeGraph(ArrayList<PartialEdge> edges, String center) {
		this.center = center;
		this.edges = edges;
		for (PartialEdge e : this.edges) {
			vectorRepresent.add(e.getHashCode());
		}
		Collections.sort(vectorRepresent);
	}

	@Override
	public String toString() {
		String res = "";
		for (PartialEdge pe : edges) {
			res += center + " " + pe.getSecond() + " " + pe.getRel() + "\n";
		}
		return res;
	}

	public void setCenter(String center) {
		this.center = center;
	}

	public int getGraphSize() {
		return vectorRepresent.size();
	}

	public long getHashCodeNodeAt(int i) {
		return vectorRepresent.get(i);
	}

	public String getCenter() {
		return center;
	}

	public boolean contains(PartialCodeGraph g) {
		double needed = (double) g.getGraphSize() * THRESHOLD;
		if (this.getGraphSize() < needed) return false;

		int j = 0, cnt = 0;
		for (int i = 0; i < g.getGraphSize(); ++i) {
			while (j < this.getGraphSize() && this.getHashCodeNodeAt(j) < g.getHashCodeNodeAt(i)) ++j;
			if (j < this.getGraphSize()) {
				if (this.getHashCodeNodeAt(j) == g.getHashCodeNodeAt(i)) cnt++;
			} else break;

			int left = this.getGraphSize() - j;
			if (left < needed - cnt) break;
		}
		return cnt >= needed;
	}

	public static void main(String[] args) {
//		Edge e1 = new Edge("a", "b", "c");
//		Edge e2 = new Edge("c", "b", "c");
//		Edge e3 = new Edge("trantrunghieu1", "function", "assign");
//		Edge e4 = new Edge("trantrunghieu2", "function", "assign");
//		ArrayList<Edge> ee = new ArrayList<>();
//		ee.add(e1); ee.add(e2); ee.add(e3); ee.add(e4);
//
//		Edge ee1 = new Edge("a", "e", "c");
//		Edge ee2 = new Edge("c", "b", "c");
//		Edge ee3 = new Edge("trantrunghieu1", "function", "assign");
//		ArrayList<Edge> eee = new ArrayList<>();
//		eee.add(ee1); eee.add(ee2); eee.add(ee3);

//		PartialCodeGraph g = new PartialCodeGraph(ee, "");
//		PartialCodeGraph g2 = new PartialCodeGraph(eee);
//		System.out.println(g.contains(g2));
	}
}
