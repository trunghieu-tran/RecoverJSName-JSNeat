package mainRecover;

import association.AssociationCalculator;
import javafx.util.Pair;
import singleVarResolution.SGData;
import singleVarResolution.SimilarGraphFinder;
import singleVarResolution.StarGraph;
import utils.FileIO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Harry Tran on 7/9/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class MainRecover_backup {
	private static final int numberOfThread = 20;
	private static int TOPK = 10;
	private static int TOPK_BEAMSEARCH = 30;

	private static String InputData = "/home/nmt140230/RecoverJSName/StarGraphTestData/"; //  138K stargraph
	private static String TrainingData = "/home/nmt140230/RecoverJSName/StarGraphData"; // 7.9 M
	private static String tmpOutput = "./resources/tmp/tmp.txt";
	private static String tmpOutputAccuracy = "./resources/tmp/tmpAccuracy.txt";
	private static String tmpOutputNoBS = "./resources/tmp/tmp_noBS.txt";
	private static String tmpOutputAccuracyNoBS = "./resources/tmp/tmpAccuracy_noBS.txt";
	private static String asscociationData = "/home/nmt140230/RecoverJSName/HashAssocData";

	private static SGData sgData = new SGData();
	private static AssociationCalculator ac;
	private Set<FunctionInfo> functionList;
	private SimilarGraphFinder sf;
	int cnt = 0;

	public void loadInput() {
		try {
			sgData.getTestData(TrainingData, -1);
			functionList = sgData.testFunctionSet;
			System.out.println(">>> The number of loaded function for testing = " + Integer.toString(functionList.size()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadTrainingData() {
		sgData.getData(TrainingData, -1);
		sgData.IndexingGraphByEdges();
		try {
//			ac = new AssociationCalculator("indirect", asscociationData, -1);
		} catch (Exception e) {
			System.out.println("ERROR Association constructor");
		}
	}

	public class ProcessingOneFunction implements Runnable {
		FunctionInfo fi;
		HashMap<StarGraph, ArrayList<Pair<String, Double>>> resolvedVarName = new HashMap<>();
//		HashMap<StarGraph, ArrayList<Pair<String, Double>>> resolvedVarName_withoutBS = new HashMap<>();
//		HashMap<Integer, StarGraph> idToSG = new HashMap<>();
//		ArrayList<ArrayList<String>> resolvedWithBs;

		int num;
		int nVar;
		public ProcessingOneFunction(FunctionInfo fi, int num) {
			this.fi = fi;
			this.num = num;
			this.nVar = fi.getStarGraphsList().size();
		}

		private void beamSearchInvocation(ArrayList<ArrayList<Pair<String, Double>>> tmp) {
//			BeamSearch bs = new BeamSearch(tmp, ac);
//			resolvedWithBs = bs.getTopKRecoveringResult(TOPK_BEAMSEARCH);
//
//			resolvedVarName.clear();
//			for (int i = 0; i < nVar; ++i) {
//				StarGraph sg = idToSG.get(i);
//				ArrayList<Pair<String, Double>> tmp2 = new ArrayList<>();
//				for (ArrayList<String> arr : resolvedWithBs)
//					tmp2.add(new Pair<>(arr.get(i), 1.0));
//				resolvedVarName.put(sg, tmp2);
//			}
		}
		public void run() {
//			ArrayList<ArrayList<Pair<String, Double>>> tmp = new ArrayList<>();

			int cc = 0;
			for (StarGraph sg : fi.getStarGraphsList()) {
				ArrayList<Pair<String, Double>> res = sf.getCandidateListForStarGraph(sg);
				resolvedVarName.put(sg, res);
//				tmp.add(res);
//				idToSG.put(cc++, sg);
			}

//			resolvedVarName_withoutBS = new HashMap<>(resolvedVarName);

//			beamSearchInvocation(tmp);

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
//		HashMap<StarGraph, ArrayList<String>> cacheNoBs = new HashMap<>();
		StringBuilder resStr = new StringBuilder();
		StringBuilder resStrNoBs = new StringBuilder();

		for (FunctionInfo fi : functionList) {
			ProcessingOneFunction pf = new ProcessingOneFunction(fi, ++cnt);
			executor.execute(pf);
			pfs.add(pf);
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
			resStr.append(">>>>> Function ").append(pf.fi.getDir()).append(" <<<<<").append("\n");
			resStrNoBs.append(">>>>> Function ").append(pf.fi.getDir()).append(" <<<<<").append("\n");

			for (StarGraph sg : pf.fi.getStarGraphsList()) {
				ArrayList<Pair<String, Double>> varNames = pf.resolvedVarName.getOrDefault(sg, null);
//				ArrayList<Pair<String, Double>> varNamesNoBs = pf.resolvedVarName_withoutBS.getOrDefault(sg, null);

				ArrayList<String> names = new ArrayList<>();
//				ArrayList<String> namesNoBs = new ArrayList<>();

				resStr.append("----------\n");
				resStr.append(sg.toString());
				resStr.append("---\n");

//				resStrNoBs.append("----------\n");
//				resStrNoBs.append(sg.toString());
//				resStrNoBs.append("---\n");

				int ccc = 0;
				for (Pair<String, Double> p : varNames) {
					names.add(p.getKey());
					resStr.append(p.getKey()).append(" ");
					if (++ccc == TOPK) break;
				}

//				ccc = 0;
//				for (Pair<String, Double> p : varNamesNoBs) {
//					namesNoBs.add(p.getKey());
//					resStrNoBs.append(p.getKey()).append(" ");
//					if (++ccc == TOPK) break;
//				}

				resStr.append("\n");
//				resStrNoBs.append("\n");

				cache.put(sg, names);
//				cacheNoBs.put(sg, namesNoBs);
			}
		}
		FileIO.writeStringToFile(tmpOutput, resStr.toString());
//		FileIO.writeStringToFile(tmpOutputNoBS, resStrNoBs.toString());
		analyzing(cache, tmpOutputAccuracy);
//		analyzing(cacheNoBs, tmpOutputAccuracyNoBS);
	}

	private  boolean isInTopK(ArrayList<String> list, int k, String oracle) {
		for (int i = 0; i < Math.min(k, list.size()); ++i)
			if (oracle.equals(list.get(i))) return true;
		return false;
	}

	private  void analyzing(HashMap<StarGraph, ArrayList<String>> cache, String fileout) {
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

		FileIO.writeStringToFile(fileout, res.toString());
	}

	public static void main(String[] args) {
		MainRecover_backup mr = new MainRecover_backup();
		mr.loadTrainingData();
		mr.loadInput();
		mr.process();
	}
}
