package mainRecover;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javafx.util.Pair;
import singleVarResolution.SGData;
import singleVarResolution.SimilarGraphFinder;
import singleVarResolution.StarGraph;
import utils.FileIO;

/**
 * @author Harry Tran on 7/9/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class MainRecover {
	private static final int numberOfThread = 20;
	private static int TOPK = 10;

	private static String InputData = "/home/nmt140230/RecoverJSName/StarGraphTestData/";
	private static String TrainingData = "/home/nmt140230/RecoverJSName/StarGraphData";
	private static String tmpOutput = "./resources/tmp/tmp.txt";
	private static String tmpOutputAccuracy = "./resources/tmp/tmpAccuracy.txt";

	private static SGData sgData = new SGData();
	private Set<FunctionInfo> functionList;
	private SimilarGraphFinder sf;
	int cnt = 0;

	public void loadInput() {
		try {
			sgData.getTestData(InputData);
			functionList = sgData.testFunctionSet;
			System.out.println(">>> The number of loaded function for testing = " + Integer.toString(functionList.size()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadTrainingData() {
		sgData.getData(TrainingData, 1000000);
	}

	public class ProcessingOneFunction implements Runnable {
		FunctionInfo fi;
		HashMap<StarGraph, ArrayList<Pair<String, Double>>> resolvedVarName = new HashMap<>();
		int num;
		public ProcessingOneFunction(FunctionInfo fi, int num) {
			this.fi = fi;
			this.num = num;
		}
		public void run() {
			for (StarGraph sg : fi.getStarGraphsList()) {
				ArrayList<Pair<String, Double>> res = sf.getCandidateListForStarGraph(sg);
				resolvedVarName.put(sg, res);
			}
			if (num % 5000 == 0) {
				System.out.println("[" + Integer.toString(num) + "/" + Integer.toString(functionList.size()) + "] >>> Processing function " + fi.getDir());
			}
		}
	}

	public void process() {
		sf = new SimilarGraphFinder(sgData.sgSet, sgData.mapEdgeToGraphs);
		ExecutorService executor = Executors.newFixedThreadPool(numberOfThread);
		ArrayList<ProcessingOneFunction> pfs = new ArrayList<>();
		HashMap<StarGraph, ArrayList<String>> cache = new HashMap<>();
		StringBuilder resStr = new StringBuilder();

		for (FunctionInfo fi : functionList) {
			ProcessingOneFunction pf = new ProcessingOneFunction(fi, ++cnt);
			executor.execute(pf);
			pfs.add(pf);
//			if (cnt == 10000) break;
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


		for (ProcessingOneFunction pf : pfs) {
			for (StarGraph sg : pf.fi.getStarGraphsList()) {
				ArrayList<Pair<String, Double>> varNames = pf.resolvedVarName.getOrDefault(sg, null);
				ArrayList<String> names = new ArrayList<>();
//				cache.put(sg, varNames);

				resStr.append("----------\n");
				resStr.append(sg.toString());
				resStr.append("---\n");

				int ccc = 0;
				for (Pair<String, Double> p : varNames) {
					names.add(p.getKey());
					resStr.append(p.getKey()).append(" ");
					if (++ccc == TOPK) break;
				}
				resStr.append("\n");
				cache.put(sg, names);
			}
		}
		FileIO.writeStringToFile(tmpOutput, resStr.toString());
		analyzing(cache);
	}

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

	public static void main(String[] args) {
		MainRecover mr = new MainRecover();
		mr.loadTrainingData();
		mr.loadInput();
		mr.process();
	}
}
