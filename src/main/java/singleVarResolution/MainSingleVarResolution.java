package singleVarResolution;

import javafx.util.Pair;
import utils.FileIO;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Harry Tran on 7/9/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class MainSingleVarResolution {
	private static String data = "/home/nmt140230/RecoverJSName/StarGraphData";
	private static String tmpOutput = "./resources/tmp/tmp.txt";

	public static void main(String[] args) {
//		HashSet<Edge> edges = new HashSet<>();
//		edges.add(new Edge("pe1", "rel1", 1));
//		edges.add(new Edge("pe2", "rel2", 2));
//
//		StarGraph testGraph = new StarGraph(edges, "UNKNOWN");
		SGData sgData = new SGData();
//		sgData.getData("F:\\Study\\Research\\RecoverJsName\\StarGraphData");
		sgData.getData(data);

		ArrayList<StarGraph> testSg = new ArrayList<>();
		int cc = 0;
		for (StarGraph sg : sgData.sgSet) {
			testSg.add(sg);
			sgData.sgSet.remove(sg);
			++cc;
			if (cc == 10) break;
		}

		StringBuilder resStr = new StringBuilder();
		SimilarGraphFinder sf = new SimilarGraphFinder(sgData.sgSet);

		for (StarGraph sg : testSg) {
			resStr.append("----------\n");
			ArrayList<Pair<String, Double>> res = sf.getCandidateListForStarGraph(sg);
			resStr.append("Original varName: ").append(sg.getVarName()).append("\n");
			for (Pair<String, Double> p : res) {
				resStr.append(p.getKey() + " " + Double.toString(p.getValue()) + "\n");
			}
		}
		FileIO.writeStringToFile(tmpOutput, resStr.toString());
	}
}
