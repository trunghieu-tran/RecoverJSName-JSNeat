package dataCenter;

import baker.BakerEngine;
import baker.BakerItem;
import bakerFreq.BakerFreqEngine;
import bakerFreq.BakerFreqItem;
import dataCenter.utils.FileIO;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * @author Harry Tran on 6/17/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class ConfidentScore {
	private final static int TOP_RESULT = 50;
	private final static Logger LOGGER = Logger.getLogger(ConfidentScore.class.getName());
	private static final String dataDir = "./resources/parsedData/trainingData/TrainSet/";
	private static final String outputFile = "./resources/parsedData/trainingData/confident.txt";
	private ArrayList<String> trainingDirs = new ArrayList<>();
	private ArrayList<String> testingDirs = new ArrayList<>();
	private ArrayList<TrainingFunctionReader> trainingFunctions = new ArrayList<>();

	private static MainDataCenter dc;

	private int numVarVar = 0;
	private int numRecord = 0;
	private int numOfNotFound = 0;

	public ConfidentScore() {
		dc = new MainDataCenter();
		loadData();
	}

	private void loadData() {
		ArrayList<String> dirList = FileIO.getAllSubdirectoryFromDirectory(dataDir);

		for (int i = 0; i < dirList.size(); ++i)
			if (i % 5 == 0) testingDirs.add(dirList.get(i));
			else trainingDirs.add(dirList.get(i));

		for (String str : trainingDirs) {
			TrainingFunctionReader ir = new TrainingFunctionReader(str, true);
			trainingFunctions.add(ir);
		}
	}

//	private void processOne(String dir) {
//		InputReader ir = new InputReader(dir);
//		HashSet<Integer> varNameSet = ir.getVarNameIDSet();
//		for (int id : varNameSet) {
////				result += Integer.toString(id) + " " + ir.getVarNameByID(id) + "\n";
//
//			ArrayList<BakerFreqItem> bakerFreqItemList = new ArrayList<>();
//			ArrayList<Pair<Integer, Integer>> peReList = ir.getListPeReByID(id);
//
//			for (Pair<Integer, Integer> pp : peReList) {
//				String tmpPe = ir.getPeByID(pp.getKey());
//				String tmpRe = ir.getReByID(pp.getValue());
//				int originalPeID = dc.getProgramEntityID(tmpPe);
//				int originalReId = dc.getRelationID(tmpRe);
//
//				BakerFreqItem currBi = dc.getBakerFreqItemFromPeRe(originalPeID, originalReId);
//				bakerFreqItemList.add(currBi);
//			}
//
//			BakerFreqEngine be = new BakerFreqEngine(id, bakerFreqItemList);
//			ArrayList<Pair<Integer, Double>> res = be.getFinalCandidateList();
//			HashSet<String> varRes = new HashSet<>();
//			if (res.size() > 0) {
//				for (int i = 0; i < res.size(); ++i) {
//					int idx = res.get(i).getKey();
//					VarNameItem vni = dc.getVarNameItemByID(idx);
//					if (vni != null) {
//						varRes.add(vni.getValue());
//					}
//				}
//				varRes.add(ir.getVarNameByID(id));
//				for (String s : varRes)
//					System.out.println("var = " + s);
//				break;
//			}
//		}
//	}
//
////		HashSet<Integer> varNameSet = ir.getVarNameIDSet();
////		for (int id: varNameSet) {
//////			result += Integer.toString(id) + " " + ir.getVarNameByID(id) + "\n";
////
////			ArrayList<BakerItem> bakerItemList = new ArrayList<>();
////			ArrayList<Pair<Integer, Integer>> peReList = ir.getListPeReByID(id);
////
////			for (Pair<Integer, Integer> pp : peReList) {
////				String tmpPe = ir.getPeByID(pp.getKey());
////				String tmpRe = ir.getReByID(pp.getValue());
////				int originalPeID = dc.getProgramEntityID(tmpPe);
////				int originalReId = dc.getRelationID(tmpRe);
////
////				BakerItem currBi = dc.getBakerItemFromPeRe(originalPeID, originalReId);
////				bakerItemList.add(currBi);
////			}
////			BakerEngine be = new BakerEngine(id, bakerItemList);
////			ArrayList<Integer> res = be.getFinalCandidateList();
////			HashSet<String> varRes = new HashSet<>();
////			if (res.size() > 0) {
////				for (int i = 0; i < res.size(); ++i) {
////					varRes.add(dc.getVarNameItemByID(res.get(i)).getValue());
////				}
////				varRes.add(ir.getVarNameByID(id));
////				for (String s : varRes)
////					System.out.println("var = " + s);
////				break;
////			}
////		}
////	}

	private int countFunctioncontains(PartialCodeGraph pg) {
		int res = 0;
		for(TrainingFunctionReader tfr : trainingFunctions) {
			if (tfr.getGraph().contains(pg, true)) res++;
		}
		return res;
	}

	private int processOne(String str) {
		TrainingFunctionReader ir = new TrainingFunctionReader(str, false);
		ArrayList<String> tmpName = dc.getRandomVarName(TOP_RESULT);
		int res_oracle = countFunctioncontains(ir.getPgraph());
		int pos = 1;
		for (String s : tmpName) {
			ir.getPgraph().setCenter(s);
			int resTmp = countFunctioncontains(ir.getPgraph());
			if (resTmp >= res_oracle) pos++;
		}

		numVarVar += ir.getNumVarVar();
		numRecord += ir.getNumRecord();
		numOfNotFound += (res_oracle == 0) ? 1 : 0;
		return pos;
	}

	private double getTopK(int k, ArrayList<Integer> cand) {
		double res = 0.0;
		for (int i : cand)
			if (i <= k) res += 1.0;
		return res / (double) cand.size();
	}

	public void process() {
		ArrayList<Integer> res = new ArrayList<>();
		for (String str : testingDirs) {
			System.out.println("Processing " + str + " ...");
			res.add(processOne(str));
		}

		String tmp = "";
		for (int i = 1; i <= TOP_RESULT; ++i) {
			tmp += "TOP-" + Integer.toString(i) + " accuracy = " + Double.toString(getTopK(i, res)) + " \\n";
		}
		FileIO.writeStringToFile(outputFile, tmp);

		System.out.println(Integer.toString(numVarVar) + "/" + Integer.toString(numRecord));
		System.out.println("Training function number = " + Integer.toString(trainingDirs.size()));
		System.out.println("Testing function number = " + Integer.toString(testingDirs.size()));
		System.out.println("Number of not found graph = " + Integer.toString(numOfNotFound));
	}

	public static void main(String[] args) {
		System.out.println("Confident Score started...");
		ConfidentScore cs = new ConfidentScore();
		cs.process();
		System.out.println("Confident Score finished...");
	}
}
