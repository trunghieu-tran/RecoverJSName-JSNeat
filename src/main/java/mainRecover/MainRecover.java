package mainRecover;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.util.Pair;
import singleVarResolution.SGData;
import singleVarResolution.SimilarGraphFinder;
import singleVarResolution.StarGraph;
import utils.Constants;
import utils.FileIO;

/**
 * @author Harry Tran on 7/9/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class MainRecover {
	private static final int numberOfThread = Constants.numberOfThread;
	private static int TOPK = Constants.TOPK_RESULT;
	private static int TOPK_BEAMSEARCH = Constants.TOPK_BEAMSEARCH;

	private static String InputData = "/home/nmt140230/RecoverJSName/StarGraphTestData/"; //  38k functions
	private static String TrainingData = "/home/nmt140230/RecoverJSName/StarGraphData"; // 2.1M function ~ 7.9 M sg

	private static String InputData2 = "/home/nmt140230/RecoverJSName/GitTestData/"; //  15k sg ~ 5.3k function
	private static String TrainingData2 = "/home/nmt140230/RecoverJSName/GitTrainData"; // 2.1M sg ~ 652k function
	private static String TrainingFileList = "./resources/tmp/trainingFileList.txt";
	private static String cacheFolder = "./resources/cache/";

	private static String tmpOutput = "./resources/tmp/tmp.txt";
	private static String tmpOutputAccuracy = "./resources/tmp/tmpAccuracy.txt";
	private static String tmpOutputNoBS = "./resources/tmp/tmp_noBS.txt";
	private static String tmpOutputAccuracyNoBS = "./resources/tmp/tmpAccuracy_noBS.txt";
	private static String tmpCache_Association = "./resources/tmp/tmpCacheAssociation.txt";
	private static String tmpAnalysis_VarNumTesting = "./resources/tmp/tmpVarNumberTesting.txt";
	private static String tmpRuningTime = "./resources/tmp/tmpRunningTime.txt";
	private static String tmpAnalysisTrainingInfo = "./resources/tmp/tmpTrainingInfo.txt";
	private static String tmpAnalysisTestingInfo = "./resources/tmp/tmpTestingInfo.txt";
	private static String tmpCurrentResolving = "./resources/tmp/tmpCurrentResolving.txt";
	private static String tmp1EdgeSG = "./resources/tmp/tmp1EdgeSG.txt";
	private static String tmp1Edge1VarFuncVsName = "./resources/tmp/tmp1Edge1VarFuncVsName.txt";
	private static String tmpPerfectResolved = "./resources/tmp/tmpPerfectResolved.txt";
	private static String tmpTrainFunctioName = "./resources/tmp/tmpTrainFunctionName.txt";
	private static String tmpTestFunctioName = "./resources/tmp/tmpTestFunctionName.txt";

	private static String asscociationData = "/home/nmt140230/RecoverJSName/HashAssocData";
	private static SGData sgData = new SGData();
	private static SimilarGraphFinder sf;
	private static HashMap<Pair<String, String>, Double> cache_Association = new HashMap<>();
	private Set<FunctionInfo> functionList;
	private ArrayList<ProcessingOneFunction> pfs = new ArrayList<>();
	private HashMap<StarGraph, ArrayList<String>> cache = new HashMap<>();
	private HashMap<StarGraph, ArrayList<String>> cacheNoBs = new HashMap<>();
	private StringBuilder timeRecord = new StringBuilder();

	long startTime, endTime;
	long startTimeLocal, endTimeLocal;
	int cnt = 0;
	static int cntDone = 0;
	static int cntAllDone = 0;
	static long totalAss = 0;
	static long totalAssCounted = 0;

	private void startClock() {
		startTime = System.nanoTime();
	}

	private void endClock(String mess) {
		endTime   = System.nanoTime();
		long timeSecs = (endTime - startTime) / 1000000000;
		double timeMins = (double) timeSecs / 60.0;
		timeRecord.append(mess).append(timeMins).append(" minutes").append("\n");
	}
	private void startClockLocal() {
		startTimeLocal = System.nanoTime();
	}

	private double endClockLocal() {
		endTimeLocal   = System.nanoTime();
		long timeSecs = (endTimeLocal - startTimeLocal) / 1000000000;
		return (double) timeSecs / 60.0;
	}
	public void loadInput() {

		startClock();

		try {
			sgData.getTestData(InputData2, -1);
			functionList = sgData.testFunctionSet;
			System.out.println(">>> The number of loaded function for testing = " + Integer.toString(functionList.size()));
			write_analyzing_varNumber(functionList, tmpAnalysis_VarNumTesting);
		} catch (IOException e) {
			e.printStackTrace();
		}
		endClock("LoadInput time : ");
	}

	public void analyzingTrainingVsTesting() {
		FileIO.writeStringToFile(tmpAnalysisTrainingInfo, sgData.Analyzing_TrainingSet(sgData.sgSet));
		FileIO.writeStringToFile(tmpAnalysisTestingInfo, sgData.Analyzing_TrainingSet(sgData.sgSetTesting));
		show1EdgeSG();
		write_analyzing_functionName();
	}

	private void show1EdgeSG() {
		StringBuilder sb = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		for (FunctionInfo fi : functionList) {
			for (StarGraph sg : fi.getStarGraphsList())
				if (sg.getSizeGraph() == 1) {
					sb.append(fi.getDir()).append(" ").append(fi.getStarGraphsList().size()).append(" ").append(sg.getVarName()).append("\n");
					if (fi.getStarGraphsList().size() == 1) {
						// 1 edge 1 var
						String[] tmp = fi.getDir().split("_");
						if (tmp.length > 0)
							sb2.append(tmp[tmp.length - 1]).append(" ").append(sg.getVarName()).append("\n");
					}
				}
		}
		FileIO.writeStringToFile(tmp1EdgeSG, sb.toString());
		FileIO.writeStringToFile(tmp1Edge1VarFuncVsName, sb2.toString());
	}

	public void loadTrainingData() {
		startClock();
		sgData.getTrainingData(TrainingData2, -1, TrainingFileList, true);
		endClock("LoadTraining time: ");

		System.out.println(sgData.varVarAssociation.showInfo());
		System.out.println(sgData.tokenVarVarAssociation.showInfo());

		startClock();
		sgData.IndexingGraphByEdges();
		endClock("Indexing graph time: ");
	}

	public class ProcessingOneFunction implements Runnable {
		FunctionInfo fi;
		int cnt;
		boolean isSolved = false;
		long totalAss = 0, totalAssCounted = 0;
		HashMap<StarGraph, ArrayList<String>> resolvedVarName_withoutBS = new HashMap<>();
		HashMap<StarGraph, ArrayList<String>> resolvedVarName = new HashMap<>();

		public ProcessingOneFunction(FunctionInfo fi, int cnt) {
			this.fi = fi;
			this.cnt = cnt;
		}

		private void beamSearchInvocation(ArrayList<ArrayList<Pair<String, Double>>> tmp, HashMap<Integer, StarGraph> idToSG) {
			BeamSearch bs = new BeamSearch(tmp, sgData.varVarAssociation, sgData.tokenVarVarAssociation, cacheFolder + Integer.toString(cnt) + ".txt");

			ArrayList<ArrayList<String>> resolvedWithBs = bs.getTopKRecoveringResult(TOPK_BEAMSEARCH);

			this.totalAss = bs.totalAss;
			this.totalAssCounted = bs.totalAssCounted;

			for (int i = 0; i < fi.getStarGraphsList().size(); ++i) {
				StarGraph sg = idToSG.get(i);

				ArrayList<String> tmp2 = new ArrayList<>();
				for (ArrayList<String> arr : resolvedWithBs) tmp2.add(arr.get(i));

				resolvedVarName.put(sg, tmp2);
			}
		}

		private double getCombinationScore(double scTask, double scItself, int sgsize) {
			double weightTask = 1.0 / sgsize;
			double weightItself = 1.0 - weightTask;
//			weightTask = 0.5;	weightItself = 0.5;
			return scTask * weightTask + scItself * weightItself;
		}

		public void run() {
			ArrayList<ArrayList<Pair<String, Double>>> tmp = new ArrayList<>();
			HashMap<Integer, StarGraph> idToSG = new HashMap<>();


			int cc = 0;
			for (StarGraph sg : fi.getStarGraphsList()) {
				ArrayList<Pair<String, Double>> res = new ArrayList<>();
				ArrayList<Pair<String, Double>> res_itself;
				Map<String, Double> res_func = new HashMap<>();
				// Itself
				if (Constants.singleVar)
					res_itself = sf.getCandidateListForStarGraph(sg);

//              Enable this code if using only Itself
				if (Constants.singleVar && !Constants.task)
					res.addAll(res_itself);

				// Task
				if (Constants.task) {
					for (String str : sgData.nameSet) {
						double sc = sgData.varFuncAssociation.getAsscociationScore(str, fi.getFuncName(), Constants.usingTokenizedFunctionName);
						if (sc > 0) {
							res_func.put(str, sc);
						}
					}
				}

//				Enable this code if using only Task
				if (Constants.task && !Constants.singleVar) {
					for (String key : res_func.keySet()) {
						res.add(new Pair<>(key, res_func.get(key)));
					}
				}

				// Merge itself + task
				if (Constants.singleVar && Constants.task) {
					for (Pair<String, Double> p : res_itself) {
						double scFunc = res_func.getOrDefault(p.getKey(), 0.0);

						double newScore = getCombinationScore(scFunc, p.getValue(), sg.getSizeGraph());

						if (newScore > 0)
							res.add(new Pair<>(p.getKey(), newScore));
					}
				}

				// Sort result

				res.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

				ArrayList<String> res2 = new ArrayList<>();
				ArrayList<Pair<String, Double>> res_shorten = new ArrayList<>();
				for (Pair<String, Double> p : res) {
					res2.add(p.getKey());
					res_shorten.add(p);
					if (res2.size() == TOPK) break;
				}

				resolvedVarName_withoutBS.put(sg, res2);


				tmp.add(res_shorten);
				idToSG.put(cc++, sg);
			}

			if (Constants.multiVar)
				beamSearchInvocation(tmp, idToSG);

			isSolved = true;
			System.out.print(">");
		}
	}

	private boolean isDone(Set<ProcessingOneFunction> running) {
		for (ProcessingOneFunction pf : running)
			if (!pf.isSolved) return false;
		return true;
	}

	public void process() {
		startClock();

		sf = new SimilarGraphFinder(sgData.mapEdgeToGraphs);


		StringBuilder resStr = new StringBuilder();
		StringBuilder resStrNoBs = new StringBuilder();

		Iterator<FunctionInfo> it = functionList.iterator();
		ArrayList<FunctionInfo> currFi = new ArrayList<>();
		while (it.hasNext()) {
			currFi.add(it.next());

			if (currFi.size() == numberOfThread) {

				startClockLocal();

				StringBuilder sb = new StringBuilder();

				cntDone = 0;
				ExecutorService executor = Executors.newFixedThreadPool(numberOfThread);
				Set<ProcessingOneFunction> running = new HashSet<>();
				for (FunctionInfo fi : currFi) {
					ProcessingOneFunction pf = new ProcessingOneFunction(fi, cnt++);
					executor.execute(pf);
					running.add(pf);
					sb.append(fi.getDir()).append(" ").append(fi.getStarGraphsList().size()).append("\n");
				}
				FileIO.writeStringToFile(tmpCurrentResolving, sb.toString());
				cntAllDone += numberOfThread;
				executor.shutdown();
				try {
					while (!executor.isTerminated()) {
						if (isDone(running)) {
							executor.shutdownNow();
							System.out.println("\n Terminated threads manually");
							break;
						}
					}
				} catch (Exception e) {
					System.out.println("Waiting error");
					executor.shutdownNow();
				}
				pfs.addAll(running);
				System.out.println("[" + Integer.toString(cntAllDone) + "/" + Integer.toString(functionList.size()) + "] "
						+ Double.toString(endClockLocal()) + " minutes");
				currFi.clear();

			}
		}

		if (currFi.size() > 0) {
			ExecutorService executor = Executors.newFixedThreadPool(currFi.size());
			Set<ProcessingOneFunction> running = new HashSet<>();
			for (FunctionInfo fi : currFi) {
				ProcessingOneFunction pf = new ProcessingOneFunction(fi, cnt++);
				executor.execute(pf);
				running.add(pf);
			}
			cntAllDone += currFi.size();
			executor.shutdown();
			try {
				while (!executor.isTerminated()) {
					if (isDone(running)) {
						executor.shutdownNow();
						System.out.println("\n Terminated threads manually");
						break;
					}
				}
			} catch (Exception e) {
				System.out.println("Waiting error");
				executor.shutdownNow();
			}
			pfs.addAll(running);
			System.out.println("[" + Integer.toString(cntAllDone) + "/" + Integer.toString(functionList.size()) + "]");
			currFi.clear();
		}


		System.out.println("FINISHED all threads for testing");
		System.out.println("The Number of DONE = " + Integer.toString(cntAllDone));

		endClock("Resolving time: ");

		StringBuilder sbPerfect = new StringBuilder();

		for (ProcessingOneFunction pf : pfs) {

			totalAssCounted += pf.totalAssCounted;
			totalAss += pf.totalAss;

			resStr.append("\n").append(">>>>> Function ").append(pf.fi.getDir()).append(" <<<<<").append(pf.fi.getStarGraphsList().size()).append("\n");
			resStrNoBs.append("\n").append(">>>>> Function ").append(pf.fi.getDir()).append(" <<<<<").append(pf.fi.getStarGraphsList().size()).append("\n");

			boolean perfect = true;
			for (StarGraph sg : pf.fi.getStarGraphsList()) {
				ArrayList<String> names = pf.resolvedVarName.getOrDefault(sg, new ArrayList<>());
				ArrayList<String> namesNoBs = pf.resolvedVarName_withoutBS.getOrDefault(sg, new ArrayList<>());

				if (names == null || names.size() == 0) {
					perfect = false;
				} else {
					perfect &= names.get(0).equals(sg.getVarName());
				}

				resStr.append("----------\n");
				resStr.append(sg.toString());
				resStr.append("---\n");

				resStrNoBs.append("----------\n");
				resStrNoBs.append(sg.toString());
				resStrNoBs.append("---\n");

				int ccc = 0;
				for (String str : names) {
					resStr.append(str).append(" ");
					if (++ccc == TOPK) break;
				}

				ccc = 0;
				for (String str : namesNoBs) {
					resStrNoBs.append(str).append(" ");
					if (++ccc == TOPK) break;
				}

				resStr.append("\n");
				resStrNoBs.append("\n");

				cache.put(sg, names);
				cacheNoBs.put(sg, namesNoBs);
			}

			if (perfect && pf.fi.getStarGraphsList().size() >= 1)
				sbPerfect.append(pf.fi.getDir()).append(" ").append(pf.fi.getStarGraphsList().size()).append("\n");
		}
		FileIO.writeStringToFile(tmpOutput, resStr.toString());
		FileIO.writeStringToFile(tmpOutputNoBS, resStrNoBs.toString());
		FileIO.writeStringToFile(tmpPerfectResolved, sbPerfect.toString());
		analyzing(cache, tmpOutputAccuracy);
		analyzing(cacheNoBs, tmpOutputAccuracyNoBS);
	}

	private void write_caching() {
		try {
			StringBuilder sb = new StringBuilder();
			for (Pair<String, String> key : cache_Association.keySet()) {
				try {
					sb.append(key.getKey()).append(" ").append(key.getValue()).append(" ").append(cache_Association.get(key)).append("\n");
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
			sb.append("Total association request = ").append(totalAss).append("\n");
			sb.append("Total association found   = ").append(totalAssCounted).append("\n");
			FileIO.writeStringToFile(tmpCache_Association, sb.toString());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void write_analyzing_functionName() {
		HashMap<String, Integer> testName = sgData.mapTestFunctionName;
		HashMap<String, Integer> trainName = sgData.mapTrainFunctionName;

		StringBuilder sb = new StringBuilder();
		for (String key : trainName.keySet()) {
			sb.append(key).append(" ").append(trainName.get(key)).append("\n");
		}

		StringBuilder sb2 = new StringBuilder();

		int cnt = 0;
		for (String key : testName.keySet()) {
			sb2.append(key).append(" ").append(testName.get(key)).append("\n");
			if (trainName.containsKey(key)) ++cnt;
		}
		sb2.append("Num_of_funcName_in_corpus = ").append(cnt).append(" / ").append(testName.size());

		FileIO.writeStringToFile(tmpTrainFunctioName, sb.toString());
		FileIO.writeStringToFile(tmpTestFunctioName, sb2.toString());
	}

	private void write_analyzing_varNumber(Set<FunctionInfo> fl, String file) {
		StringBuilder sb = new StringBuilder();
		for (FunctionInfo fi : fl) {
			sb.append(fi.getDir()).append(" ").append(fi.getStarGraphsList().size()).append("\n");
		}
		FileIO.writeStringToFile(file, sb.toString());
	}

	private  boolean isInTopK(ArrayList<String> list, int k, String oracle) {
		for (int i = 0; i < Math.min(k, list.size()); ++i)
			if (oracle.equals(list.get(i))) return true;
		return false;
	}

	private  void analyzing(HashMap<StarGraph, ArrayList<String>> cache, String fileout) {
		StringBuilder res = new StringBuilder();

		int[] tops = {1, 5, 10, 30};
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

	public void write_RuningTime() {
		FileIO.writeStringToFile(tmpRuningTime, timeRecord.toString());
	}

	public static void main(String[] args) {
		MainRecover mr = new MainRecover();
		mr.loadTrainingData();
		mr.loadInput();
		mr.analyzingTrainingVsTesting();
		mr.process();
		mr.write_caching();
		mr.write_RuningTime();
	}
}
