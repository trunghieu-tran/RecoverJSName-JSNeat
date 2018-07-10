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
	private static int TOPK = 10;
	private static final int numberOfThread = 20;
	private static String data = "/home/nmt140230/RecoverJSName/StarGraphData";
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
		int numOfOne = 0;
		for (StarGraph sg : cache.keySet())
			if (sg.getSizeGraph() == 1) ++numOfOne;

		res.append("Number of training = ").append(sgData.sgSet.size()).append("\n");
		res.append("Number of testcase = ").append(numOfTest).append("\n");
		res.append("Number of testcase_1_edge = ").append(numOfOne).append("\n");


		for (int i = 0; i < tops.length; ++i) {
			int cnt = 0;
			int cnt1 = 0;
			int cntNot1 = 0;
			for (StarGraph sg : cache.keySet()) {
				if (isInTopK(cache.get(sg), tops[i], sg.getVarName())) {
					cnt++;
					if (sg.getSizeGraph() == 1) cnt1++;
					else cntNot1++;
				}
			}

			res.append("== TopK = ").append(tops[i]).append("===\n");
			res.append("Accuracy = ").append((double) cnt / numOfTest).append("\n");
			res.append("Accuracy1 = ").append((double) cnt1 / numOfOne).append("\n");
			res.append("AccuracyNOT1 = ").append((double) cntNot1 / (numOfTest - numOfOne)).append("\n");
		}

		FileIO.writeStringToFile(tmpOutputAccuracy, res.toString());
	}

	public void loadData() {
		sgData = new SGData();
		sgData.getData(data, -1);
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

	public void testing() {
		HashSet<StarGraph> testSg = new HashSet<>();
		int cc = 0;
		int c = 0;
		numOfTest = sgData.sgSet.size() / 10;
		for (StarGraph sg : sgData.sgSet) {
			if (++c % 10 == 0) {
				testSg.add(new StarGraph(sg));
				++cc;
				if (cc == numOfTest) break;
			}
		}

		sgData.sgSet.removeAll(testSg);

		StringBuilder resStr = new StringBuilder();
		SimilarGraphFinder sf = new SimilarGraphFinder(sgData.sgSet);
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
