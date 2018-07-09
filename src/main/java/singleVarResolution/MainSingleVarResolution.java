package singleVarResolution;

import javafx.util.Pair;
import utils.FileIO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Harry Tran on 7/9/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class MainSingleVarResolution {
	private static int TOPK = 30;
	private static String data = "/home/nmt140230/RecoverJSName/StarGraphData";
	private static String tmpOutput = "./resources/tmp/tmp.txt";
	private static String tmpOutputAccuracy = "./resources/tmp/tmpAccuracy.txt";

	private SGData sgData;

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
		SGData sgData = new SGData();
		sgData.getData(data, 100000);

	}

	public void testing() {
		ArrayList<StarGraph> testSg = new ArrayList<>();
		int cc = 0;
		int c = 0;
		for (StarGraph sg : sgData.sgSet) {
			if (++c % 10 == 0) {
				testSg.add(new StarGraph(sg));
				++cc;
				if (cc == 100000) break;
			}
		}
		sgData.sgSet.removeAll(testSg);

		StringBuilder resStr = new StringBuilder();
		SimilarGraphFinder sf = new SimilarGraphFinder(sgData.sgSet);
		HashMap<StarGraph, ArrayList<String>> cache = new HashMap<>();

		int csg = 0;
		for (StarGraph sg : testSg) {
			if (++csg % 10000 == 0)
				System.out.println("[" + Integer.toString(csg) + "/" + Integer.toString(testSg.size()) + "] >>> Processing ..." + sg.getVarName());
			resStr.append("----------\n");
			resStr.append(sg.toString());
			resStr.append("---\n");
			ArrayList<Pair<String, Double>> res = sf.getCandidateListForStarGraph(sg);
			ArrayList<String> tmpStr = new ArrayList<>();

			resStr.append(">> Original-varName: ").append(sg.getVarName()).append("\n");
			int cnt = 0;
			for (Pair<String, Double> p : res) {
				tmpStr.add(p.getKey());

				resStr.append(p.getKey()).append(" ").append(Double.toString(p.getValue())).append("\n");
				if (++cnt == TOPK) break;
			}

			cache.put(sg, tmpStr);
		}

		FileIO.writeStringToFile(tmpOutput, resStr.toString());
		analyzing(cache);
	}

	public static void main(String[] args) {
		MainSingleVarResolution mr = new MainSingleVarResolution();
		mr.loadData();
//		mr.testing();
	}
}
