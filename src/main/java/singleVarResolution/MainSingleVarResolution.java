package singleVarResolution;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Harry Tran on 7/9/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class MainSingleVarResolution {

	public static void main(String[] args) {
		HashSet<Edge> edges = new HashSet<>();
		edges.add(new Edge("pe1", "rel1", 1));
		edges.add(new Edge("pe2", "rel2", 2));

		StarGraph testGraph = new StarGraph(edges, "UNKNOWN");
		SGData sgData = new SGData();
		sgData.getData("F:\\Study\\Research\\RecoverJsName\\StarGraphData");

		SimilarGraphFinder sf = new SimilarGraphFinder(sgData.sgSet);

		ArrayList<Pair<String, Double>> res = sf.getCandidateListForStarGraph(testGraph);
		for (Pair<String, Double> p : res) {
			System.out.println(p.getKey() + " "  + Double.toString(p.getValue()));
		}
	}
}
