package bakerFreq;

import baker.BakerEngine;
import baker.BakerItem;
import dataCenter.InputReader;
import dataCenter.MainDataCenter;
import dataCenter.VarNameItem;
import dataCenter.utils.FileIO;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Harry Tran on 6/6/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class MainBakerFreq {
	private final static Logger LOGGER = Logger.getLogger(MainBakerFreq.class.getName());
	private static String UNRESOLVED = "UNRESOLVED";
	private static final int TOP_RESULT = 30;
	private static final String dataDir = "./resources/parsedData/testingData/";
	private static final String resultReportFile = "./resources/parsedData/resultReport";

	private static ArrayList<String> dirList;

	private static ArrayList<InputReader> irList = new ArrayList<>();
	private static MainDataCenter dc;
	private static ArrayList<String> outputFileList = new ArrayList<>();

	private static HashMap<String, ArrayList<String> > mapResult = new HashMap<>();

	public MainBakerFreq() {
		dirList = FileIO.getAllSubdirectoryFromDirectory(dataDir);
		dc = new MainDataCenter();
		for (String dir : dirList) {
			InputReader ir = new InputReader(dir);
			irList.add(ir);
			outputFileList.add(dir + "predictionByBakerFreq.txt");
		}
	}

	private static String varNameNormalization(String name) {
		name = name.toLowerCase();
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < name.length(); ++i) {
			if ('a' <= name.charAt(i) && name.charAt(i) <= 'z') {
				res.append(name.charAt(i));
			}
		}
		if (res.length() == 0) res = new StringBuilder(name);
		return res.toString();
	}

	public static boolean isSimilar(String s1, String s2) {
		String s1Normalized = varNameNormalization(s1);
		String s2Normalized = varNameNormalization(s2);
		boolean res = s1Normalized.equals(s2Normalized);
		res |= s1Normalized.contains(s2Normalized);
		res |= s2Normalized.contains(s1Normalized);
		return res;
	}

	private void getAnalysing(int top) {
		String res = "";
		int numOfUnresolved = 0;
		int numOfCorrectAns = 0;
		for (Map.Entry<String, ArrayList<String>> entry : mapResult.entrySet()) {
			boolean isUnresolved = true;
			for (int i = 0; i < Math.min(entry.getValue().size(), top); ++i) {
				isUnresolved &= entry.getValue().get(i).equals(UNRESOLVED);
				if (isSimilar(entry.getValue().get(i), entry.getKey())) {
					numOfCorrectAns++;
					break;
				}
			}
			if (isUnresolved) numOfUnresolved++;
		}

		double recall = (1 - (double) numOfUnresolved / mapResult.size()) * 100;
		double precision = ((double) numOfCorrectAns / (mapResult.size() - numOfUnresolved)) * 100;
		res += "=== Analysis ===\n";
		res += "Number of variable name = " + Integer.toString(mapResult.size()) + "\n";
		res += "Recall = " + Double.toString(recall) + " %\n";
		res += "Precision = " + Double.toString(precision) + " %\n\n";

		for (Map.Entry<String, ArrayList<String>> entry : mapResult.entrySet()) {
			res += entry.getKey() + " : ";
			for (int i = 0; i < Math.min(entry.getValue().size(), top); ++i) {
				res += entry.getValue().get(i) + " ";
			}
			res += "\n";
		}
		FileIO.writeStringToFile(resultReportFile + Integer.toString(top) + ".txt", res);
	}

	private void processOne(InputReader ir, String outputFile) {
		String result = "";
		HashSet<Integer> varNameSet = ir.getVarNameIDSet();
		for (int id: varNameSet) {
			result += Integer.toString(id) + " " + ir.getVarNameByID(id) + "\n";

			ArrayList<BakerFreqItem> bakerFreqItemList = new ArrayList<>();
			ArrayList<Pair<Integer, Integer>> peReList = ir.getListPeReByID(id);

			for (Pair<Integer, Integer> pp : peReList) {
				String tmpPe = ir.getPeByID(pp.getKey());
				String tmpRe = ir.getReByID(pp.getValue());
				int originalPeID = dc.getProgramEntityID(tmpPe);
				int originalReId = dc.getRelationID(tmpRe);

				BakerFreqItem currBi = dc.getBakerFreqItemFromPeRe(originalPeID, originalReId);
				bakerFreqItemList.add(currBi);
			}

			BakerFreqEngine be = new BakerFreqEngine(id, bakerFreqItemList);
			ArrayList<Pair<Integer, Double>> res = be.getFinalCandidateList();

			result += Integer.toString(res.size()) + "\n";
			if (res.size() > 0) {
				int numOfAdded = 0;
				String ttt = "";
				ArrayList<String> tempRes = new ArrayList<>();
				for (int i = 0; i < res.size(); ++i) {
					int idx = res.get(i).getKey();
					double prob = res.get(i).getValue();

					VarNameItem vni = dc.getVarNameItemByID(idx);
					if (vni != null && numOfAdded < TOP_RESULT) {
						ttt += "(" + Double.toString(prob) + ")";
						ttt += vni.getValue() + " ";
						tempRes.add(vni.getValue());
						numOfAdded++;
					}
				}
				mapResult.put(ir.getVarNameByID(id), tempRes);
				result += ttt + "\n";
			} else {
				ArrayList<String> tmp = new ArrayList<>();
				tmp.add(UNRESOLVED);
				mapResult.put(ir.getVarNameByID(id), tmp);
			}
			result += "\n";
		}
		FileIO.writeStringToFile(outputFile, result);
	}

	public void process() {
		for (int i = 0; i < irList.size(); ++i) {
			System.out.println(">>> Processing " + dirList.get(i) + "... [" + Integer.toString(i + 1) + "/" + Integer.toString(irList.size()) +"]");
			processOne(irList.get(i),  outputFileList.get(i));
		}
	}

	public static void main(String[] args) {
		System.out.println("=== Started BakerFreq...");

		MainBakerFreq bakerFreq = new MainBakerFreq();
		bakerFreq.process();
		bakerFreq.getAnalysing(1);
		bakerFreq.getAnalysing(3);
		bakerFreq.getAnalysing(5);
		bakerFreq.getAnalysing(10);
		bakerFreq.getAnalysing(20);
		bakerFreq.getAnalysing(TOP_RESULT);

		System.out.println("... Finished ===");
	}
}

/*
=== Analysis ===
Number of variable name = 171
Recall = 98.24561403508771 %
Precision = 54.761904761904766 %
 */