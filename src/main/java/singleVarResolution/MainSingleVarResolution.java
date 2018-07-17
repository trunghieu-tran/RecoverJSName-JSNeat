package singleVarResolution;

import javafx.util.Pair;
import utils.FileIO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Harry Tran on 7/9/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class MainSingleVarResolution {
	private static String RELATION_TYPE = "CoArgument";
	private static int TOPK = 10;
	private static final int numberOfThread = 20    ;
	private static String data = "/home/nmt140230/RecoverJSName/StarGraphData"; // 7.9M
	private static String tmpOutput = "./resources/tmp/tmp.txt";
	private static String tmpOutputAccuracy = "./resources/tmp/tmpAccuracy.txt";

	private SGData sgData;
	private int countDone = 0;
	private int numOfTest = 0;

	private  boolean isInTopK(ArrayList<String> list, int k, String oracle) {
		for (int i = 0; i < Math.min(k, list.size()); ++i)
			if (oracle.equals(list.get(i))) return true;
		return false;
	}

	private  void analyzing(HashMap<StarGraph, ArrayList<String>> cache) {
		StringBuilder res = new StringBuilder();

		int[] tops = {1, 5, 10};
		int numOfTest = cache.size();
		int[] numOfEdge = new int[11];
		for (StarGraph sg : cache.keySet())
			if (sg.getSizeGraph() <= 10) numOfEdge[sg.getSizeGraph()]++;

		res.append("Number of training = ").append(sgData.sgSet.size()).append("\n");
		res.append("Number of testcase = ").append(numOfTest).append("\n");

		for (int i = 1; i <= 10; ++i)
			res.append("Number of testcase ").append(i).append("_edges = ").append(numOfEdge[i]).append("\n");



		for (int i = 0; i < tops.length; ++i) {
			int[] cntE = new int[11];
			int cnt = 0;
			for (StarGraph sg : cache.keySet()) {
				if (isInTopK(cache.get(sg), tops[i], sg.getVarName())) {
					cnt++;
					if (sg.getSizeGraph() <= 10) cntE[sg.getSizeGraph()]++;
				}
			}

			res.append("== TopK = ").append(tops[i]).append("===\n");
			res.append("Accuracy = ").append((double) cnt / numOfTest).append("\n");
			for (int ii = 1; ii <= 10; ++ii)
				res.append("Accuracy ").append(ii).append("_edges = ").append((double) cntE[ii] / numOfEdge[ii]).append("\n");
		}

		FileIO.writeStringToFile(tmpOutputAccuracy, res.toString());
	}

	public void loadData() {
		System.out.println("Loading corpus...");
		sgData = new SGData();
		sgData.getData(data, 1000000);
		sgData.IndexingGraphByEdges();
	}

	public class ProcessingOneGraph implements Runnable {
		StarGraph sg;
		SimilarGraphFinder sf;
		ArrayList<String> nameResolved = new ArrayList<>();
		public ProcessingOneGraph(StarGraph sg, SimilarGraphFinder sf) {
			this.sg = sg;
			this.sf = sf;
		}
		public void run() {
			ArrayList<Pair<String, Double>> res = sf.getCandidateListForStarGraph(sg);
			int cnt = 0;
			for (Pair<String, Double> p : res) {
				nameResolved.add(p.getKey());
				if (++cnt == TOPK) break;
			}
			if (++countDone % 10000 == 0) {
				System.out.println("[ DONE testing " + Integer.toString(countDone) + "/" + Integer.toString(numOfTest) + "]");
			}
		}
	}

	private ArrayList<String> getRels() {
		ArrayList<String> rels = new ArrayList<>();
		rels.add("CoArgument");
//		rels.add("Assignment");
//		rels.add("Argument");
//		rels.add("FieldAccess");
//		rels.add("FunctionCall");
		rels.add("Boolean");
		return rels;
	}

	public void testing() {
		HashSet<StarGraph> testSg = new HashSet<>();
		int cc = 0;
		int c = 5;
		numOfTest = sgData.sgSet.size() / 10;
		ArrayList<String> rels = getRels();
		for (StarGraph sg : sgData.sgSet) {
			if (++c % 10 == 0) {
				// This is for a normal graph
				StarGraph sgg = new StarGraph(sg);

				// This is for choose 1 relation type
//				StarGraph sgg = new StarGraph(sg, RELATION_TYPE);

				// This is for a graph without RELATION_TYPE


//				StarGraph sgg = new StarGraph(sg, rels, true);
				if (sgg.getSizeGraph() == 0) continue;

				testSg.add(sgg);
				++cc;
				if (cc == numOfTest) break;
			}
		}

		sgData.sgSet.removeAll(testSg);

		StringBuilder resStr = new StringBuilder();
		SimilarGraphFinder sf = new SimilarGraphFinder(sgData.sgSet, sgData.mapEdgeToGraphs);
		HashMap<StarGraph, ArrayList<String>> cache = new HashMap<>();

		ExecutorService executor = Executors.newFixedThreadPool(numberOfThread);
		ArrayList<ProcessingOneGraph> pgs = new ArrayList<>();

		for (StarGraph sg : testSg) {
			ProcessingOneGraph pg = new ProcessingOneGraph(sg, sf);
			executor.execute(pg);
			pgs.add(pg);
		}

		// Wait until all threads are finish
		executor.shutdown();
		try {
			if (!executor.awaitTermination(7, TimeUnit.DAYS))
				executor.shutdownNow();
		} catch (Exception e) {
			System.out.println("Waiting error");
			executor.shutdownNow();
		}
		System.out.println("FINISHED all threads for testing");

		for (ProcessingOneGraph pg : pgs) {
			cache.put(pg.sg, pg.nameResolved);

			resStr.append("----------\n");
			resStr.append(pg.sg.toString());
			resStr.append("---\n");
			for (String str : pg.nameResolved) {
				resStr.append(str).append(" ");
			}
			resStr.append("\n");
		}

		FileIO.writeStringToFile(tmpOutput, resStr.toString());
		analyzing(cache);
	}

	public static void main(String[] args) {
		MainSingleVarResolution mr = new MainSingleVarResolution();
		mr.loadData();
		mr.testing();
	}
}
