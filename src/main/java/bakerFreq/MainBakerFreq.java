package bakerFreq;

import dataCenter.InputReader;
import dataCenter.MainDataCenter;
import dataCenter.VarNameItem;
import dataCenter.utils.FileIO;
import dataCenter.utils.topicModelling;
import javafx.util.Pair;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author Harry Tran on 6/6/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class MainBakerFreq {
	private final static Logger LOGGER = Logger.getLogger(MainBakerFreq.class.getName());
	private static boolean enableTopicModel = true;
	private static String UNRESOLVED = "UNRESOLVED";
	private static final int TOP_RESULT = 10;
	private static final String dataDir = "./resources/parsedData/TestSet/";
//	private static final String dataDir = "./resources/parsedData/testingData/";
	private static final String resultReportFile = "./resources/parsedData/resultReport";
	private static final String resultNumVarReportFile = "./resources/parsedData/numVarReport.txt";

	private static ArrayList<String> dirList;

	private static ArrayList<InputReader> irList = new ArrayList<>();
	private static MainDataCenter dc;
	private static ArrayList<String> outputFileList = new ArrayList<>();

	private static HashMap<String, ArrayList<String> > mapResult = new HashMap<>();
	private ArrayList<Integer> numOfVarList = new ArrayList<>();

	private static topicModelling tm;

	public MainBakerFreq() {
		dirList = FileIO.getAllSubdirectoryFromDirectory(dataDir);
		dc = new MainDataCenter();
		tm = new topicModelling();
		for (String dir : dirList) {
			InputReader ir = new InputReader(dir);
			if (ir.hasTopicModel()) {
				irList.add(ir);
				outputFileList.add(dir + "predictionByBakerFreq.txt");
				numOfVarList.add(ir.getNumberOfVarName());
			}
		}
	}

	private void analyszingNumberOfVarName() {
		HashSet<Integer> setNum = new HashSet<>();
		setNum.addAll(numOfVarList);
		Iterator<Integer> itr = setNum.iterator();
		StringBuilder res = new StringBuilder();
		while (itr.hasNext()) {
			int value = itr.next();
			res.append(Integer.toString(value)).append(" ").append(Integer.toString(Collections.frequency(numOfVarList, value))).append("\n");
		}
		FileIO.writeStringToFile(resultNumVarReportFile, res.toString());
	}

	private static String varNameNormalization(String name) {
		name = name.toLowerCase();
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < name.length(); ++i) {
			if ('a' <= name.charAt(i) && name.charAt(i) <= 'z') {
				res.append(name.charAt(i));
			}
		}
		if (res.length() == 0) res = new StringBuilder("i");
		return res.toString();
	}

	public static boolean isSimilar(String s1, String s2) {
		String s1Normalized = varNameNormalization(s1);
		String s2Normalized = varNameNormalization(s2);
		boolean res = s1Normalized.equals(s2Normalized);
		if (s2Normalized.length() >= 3)
			res |= s1Normalized.contains(s2Normalized);
		if (s1Normalized.length() >= 3)
			res |= s2Normalized.contains(s1Normalized);
		return res;
	}

	private void getAnalysing(int top) {
		String res = "";
		int numOfUnresolved = 0;
		int numOfCorrectAns = 0;
		ArrayList<Boolean> mark = new ArrayList<>();

		for (Map.Entry<String, ArrayList<String>> entry : mapResult.entrySet()) {
			boolean isUnresolved = true;
			mark.add(false);
			for (int i = 0; i < Math.min(entry.getValue().size(), top); ++i) {
				isUnresolved &= entry.getValue().get(i).equals(UNRESOLVED);
				if (isSimilar(entry.getValue().get(i), entry.getKey())) {
					numOfCorrectAns++;
					mark.set(mark.size() - 1, true);
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

		int cc = 0;
		for (Map.Entry<String, ArrayList<String>> entry : mapResult.entrySet()) {
			if (cc < mark.size() && mark.get(cc))
				res += "[TRUE] ";
			else
				res += "[FALSE] ";
			res += entry.getKey() + " : ";
			for (int i = 0; i < Math.min(entry.getValue().size(), top); ++i) {
				res += entry.getValue().get(i) + " ";
			}
			res += "\n";
			++cc;
		}
		String outputFile = (!enableTopicModel) ? resultReportFile + Integer.toString(top) + ".txt" : resultReportFile + Integer.toString(top) + "_TM" + ".txt";
		FileIO.writeStringToFile(outputFile, res);
		System.out.println("DONE analyzing top " + Integer.toString(top));
	}

	private void getAnalysingGeneral() {
		String res = "";
		int numOfUnresolved = 0;
		int numOfCorrectAns = 0;
		ArrayList<Boolean> mark = new ArrayList<>(mapResult.entrySet().size());
		int cc = 0;

		for (Map.Entry<String, ArrayList<String>> entry : mapResult.entrySet()) {
			mark.set(cc, false);
			boolean isUnresolved = true;
			for (int i = 0; i < entry.getValue().size(); ++i) {
				isUnresolved &= entry.getValue().get(i).equals(UNRESOLVED);
				if (isSimilar(entry.getValue().get(i), entry.getKey())) {
					numOfCorrectAns++;
					mark.set(cc, true);
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

		cc = 0;
		for (Map.Entry<String, ArrayList<String>> entry : mapResult.entrySet()) {
			if (mark.get(cc))
				res += "[TRUE] ";
			else
				res += "[FALSE] ";
			res += entry.getKey() + " : " + Integer.toString(entry.getValue().size()) + "\n";
			++cc;
		}

		String outputFile = (!enableTopicModel) ? resultReportFile + "General" + ".txt" : resultReportFile + "General_TM" + ".txt";
		FileIO.writeStringToFile(outputFile, res);
	}

	private ArrayList<Pair<Integer, Double>> rankByTopicModel(ArrayList<Pair<Integer, Double>> currRes, InputReader ir) {
		ArrayList<Pair<Integer, Double>> res = new ArrayList<>();
		for (int i = 0; i < currRes.size(); ++i) {
			int idx = currRes.get(i).getKey();
			double prob = currRes.get(i).getValue();
			VarNameItem vni = dc.getVarNameItemByID(idx);
			if (vni != null) {
				double bestResult = -1;
				for (int topic = 0; topic < tm.getNumOfTopic(); ++topic) {
					double probTp = tm.getProbabilityOfNameInTopic(vni.getValue(), topic);
					bestResult = Math.max(bestResult, prob * probTp * ir.getTopicProbability(topic));
				}
				res.add(new Pair<>(idx, bestResult));
			}
		}

		res.sort((o1, o2) -> {
			if (o1.getValue() < o2.getValue()) return 1;
			else if (o1.getValue() > o2.getValue()) return -1;
			else return 0;
		});

		return res;
	}
	private void processOne(InputReader ir, String outputFile) {
		String result = "";
		HashSet<Integer> varNameSet = ir.getVarNameIDSet();
		for (int id: varNameSet)
			if (varNameNormalization(ir.getVarNameByID(id)).length() >= 3) {
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

				if (enableTopicModel) {
					res = rankByTopicModel(res, ir);
				}

				result += Integer.toString(res.size()) + "\n";
				if (res.size() > 0) {
					int numOfAdded = 0;
					String ttt = "";
					ArrayList<String> tempRes = new ArrayList<>();
					for (int i = 0; i < res.size(); ++i) {
						int idx = res.get(i).getKey();
						double prob = res.get(i).getValue();

						VarNameItem vni = dc.getVarNameItemByID(idx);
						if (vni != null && numOfAdded < TOP_RESULT && varNameNormalization(vni.getValue()).length() >= 3) {
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
		bakerFreq.analyszingNumberOfVarName();
		bakerFreq.process();
//		bakerFreq.getAnalysingGeneral();
		bakerFreq.getAnalysing(1);
//		bakerFreq.getAnalysing(3);
		bakerFreq.getAnalysing(5);
		bakerFreq.getAnalysing(10);
//		bakerFreq.getAnalysing(15);
//		bakerFreq.getAnalysing(TOP_RESULT);

		System.out.println("... Finished ===");
	}
}

/*
=== Analysis ===
Number of variable name = 171
Recall = 98.24561403508771 %
Precision = 54.761904761904766 %
 */
