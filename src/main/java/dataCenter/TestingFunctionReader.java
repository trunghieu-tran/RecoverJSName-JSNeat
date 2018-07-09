package dataCenter;

import bakerFreq.BakerFreqEngine;
import bakerFreq.BakerFreqItem;
import dataCenter.utils.FileIO;
import javafx.util.Pair;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author Harry Tran on 6/22/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class TestingFunctionReader implements Runnable{
	public final static String UNRESOLVED = "UNRESOLVED";
	public final static int TOPK = 30;
	private final static int TOP_CANDIDATE = 20;
	private final static Logger LOGGER = Logger.getLogger(TestingFunctionReader.class.getName());
	private String inputDir ; // Set by default


	private static final String programEntityFile =  "peData.txt";
	private static final String varNameFile = "varNameData.txt";
	private static final String relationFile = "relData.txt";
	private static final String relationRecordFile = "recordData.txt";

	private HashMap<Integer, String> mapIDvsPe = new HashMap<>();
	private HashMap<Integer, String> mapIDvsRe = new HashMap<>();
	private HashMap<Integer, String> mapIDvsvarName = new HashMap<>();
	private HashMap<Integer, ArrayList< Pair<Integer, Integer> > > mapVarNameIDvsPeReList = new HashMap<>();
	private HashMap<Integer, ArrayList< Pair<Integer, Integer> > > mapVarNameIDvsVar = new HashMap<>();
	private HashMap<Pair< Integer, Pair <Integer, Integer>>, Integer> mapRecordvsType = new HashMap<>();
	private HashSet<Pair<Integer, Integer>> setVarVar = new HashSet<>();
	private HashSet<Integer> varNameIDSet = new HashSet<>();
	private MainDataCenter dc;
	private ArrayList< ArrayList < String > > currRecovering = new ArrayList<>();
	private ArrayList<TrainingFunctionReader> trainingFunctions;
	private ArrayList<Integer> order;
	private ArrayList<Boolean> marked = new ArrayList<>();

	private HashMap<Integer, ArrayList<PartialEdge>> mapIdVarvsPartialEdges = new HashMap<>();
	private HashMap<Pair<Integer, String>, Integer> mapCountPartialGraph = new HashMap<>();

	private boolean isRun = false;
	public boolean isSolved = false;
	private int numOfDir;
	public int graphFound = 0;
	public int graphNotFound = 0;
	public String sampleGraphFound = "";
	public String sampleGraphNotFound = "";
	private boolean dataLoadedSuccessfully = true;

	public void setNumOfDir(int num) {
		this.numOfDir = num;
	}

	public boolean isRun() {
		return isRun;
	}

	public TestingFunctionReader(String dir, MainDataCenter dc, ArrayList<TrainingFunctionReader> trainingFunctions) {
		this.inputDir = dir;
		this.dc = dc;
		this.trainingFunctions = trainingFunctions;
		isRun = true;
	}

	public void run()
	{
		try {
			loadData();
		}catch (Exception e) {
			dataLoadedSuccessfully = false;
			System.out.println("Error loading data: " + e.getMessage() + " " + inputDir);
		}

		if (isQualified()){
			process();
		}
		System.out.println("[" + Integer.toString(numOfDir) +"] Exit thread " +  inputDir);
	}

	private void printOutTheOracle() {
		System.out.println("=== ORACLE ===");
		String res = "";
		for (int i : order) {
			res += mapIDvsvarName.get(i) + ", ";
		}
		System.out.println(res);
		System.out.println("=== === ===");
	}

	private int countFunctioncontains(PartialCodeGraph pg) {
		int res = 0;
		for(TrainingFunctionReader tfr : trainingFunctions) {
			if (tfr.getGraph().contains(pg, true)) res++;
		}
		return res;
	}

	public String getPrintOutResult() {
		String res = inputDir + "-- Oracle \n";
		for (int i : order) {
			res += mapIDvsvarName.get(i) + ", ";
		}
		res += "\n -- Prediction --\n";
		for (ArrayList<String> astr : currRecovering) {
			String tmp = "";
			for (String str : astr) {
				tmp += str + ", ";
			}
			tmp += "\n";
			res += tmp;
		}
		return res;
	}

	public void printCurrStack(int num) {
		System.out.println("---");
		System.out.println("Stack number " + Integer.toString(num));
		for (ArrayList<String> astr : currRecovering) {
			String res = "";
			for (String str : astr) {
				res += str + ", ";
			}
			System.out.println(res);
		}
	}

	public boolean isQualified() {
//		if (getVariableNumber() > 20 || getVariableNumber() == 0) return false;
//		if (getVariableNumber() > 3 || getVariableNumber() <= 2) return false;
//		for (int idx : mapIDvsvarName.keySet()) {
//			if (varNameNormalization(mapIDvsvarName.get(idx)).length() < 3) return false;
//		}
//			int freq = dc.getVarNameFreqencyByID(dc.getVarNameID(mapIDvsvarName.get(idx)));
//			System.out.println("== " + mapIDvsvarName.get(idx) + Integer.toString(freq));
//			if (freq < THRESHOLD_FREQ) return false;
		if (mapIDvsPe.size() <= 0) return false;
		if (mapIDvsvarName.size() <= 0) return false;
		if (!dataLoadedSuccessfully) return false;
		return true;
	}
	public int getVariableNumber() {
		return varNameIDSet.size();
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

	public int getResolvedNum() {
		int cc = 0;
		for (boolean mark : marked)
			cc += mark ? 1 : 0;
		return cc;
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


	public double getAccuracyTop(int k) {
		if (k > TOPK) k = TOPK;
		double cnt = 0.0;
		for (int i = 0; i < order.size(); ++i) {
			String originName = mapIDvsvarName.get(order.get(i));
			boolean found = false;
			for (int j = 0; j < k; ++j) {
				try {
					String recoveredName = currRecovering.get(j).get(i);
					if (isSimilar(recoveredName, originName)) {
						found = true;
						break;
					}
				} catch (Exception e) {

				}
			}
			if (found) cnt += 1.0;
		}
		return cnt / (double) varNameIDSet.size();
	}

	public void process() {
		try {
			isRun = true;
			order = getRecoveredOrder();

			int firstVar = order.get(0);
			ArrayList<Integer> candidateID = getListCandidate(firstVar);

			if (candidateID.size() > 0) {
				marked.add(true);
				mapIdVarvsPartialEdges.put(firstVar, getListPartialEdgeFromPE(firstVar));

				for (int i = 0; i < Math.min(candidateID.size(), TOPK); ++i) {
					ArrayList<String> strList = new ArrayList<>();
					String varName;
					varName = dc.getVarNameItemByID(candidateID.get(i)).getValue();
					strList.add(varName);
					currRecovering.add(strList);

					PartialCodeGraph pg = new PartialCodeGraph(mapIdVarvsPartialEdges.get(firstVar), varName);
					mapCountPartialGraph.put(new Pair<>(firstVar, varName), countFunctioncontains(pg));
				}
			} else {
				marked.add(false);
				ArrayList<String> strList = new ArrayList<>();
				strList.add(UNRESOLVED);
				currRecovering.add(strList);
			}

			for (int i = 1; i < order.size(); ++i) {
				if (!processNext(i)) {
					marked.add(false);
					for (ArrayList<String> arrayList : currRecovering) {
						arrayList.add(UNRESOLVED);
					}
				} else marked.add(true);
			}

			isSolved = true;

		} catch (Exception e) {
			LOGGER.info(e.getMessage());
		}
	}

	private boolean isConnected(int idRecovering, int idRecovered) {
		return setVarVar.contains(new Pair<>(idRecovering, idRecovered))
				|| setVarVar.contains(new Pair<>(idRecovered, idRecovering));
	}

	private int getRelation(int  idRecovering, int idRecovered) {
		for (Pair<Integer, Integer> p : mapVarNameIDvsVar.getOrDefault(idRecovering, new ArrayList<>())) {
			if (p.getKey() == idRecovered) return p.getValue();
		}
		return 0;
	}


	private ArrayList<PartialEdge> getListPartialEdgeFromPE(int varId) {
		ArrayList<PartialEdge> edges = new ArrayList<>();
		for (Pair<Integer, Integer> p : mapVarNameIDvsPeReList.getOrDefault(varId, new ArrayList<>())) {
			String peOrVar = mapIDvsPe.getOrDefault(p.getKey(), " ");
			String rel = mapIDvsRe.getOrDefault(p.getValue(), " ");
			edges.add(new PartialEdge(peOrVar, rel));
		}
		return edges;
	}

	private ArrayList<PartialEdge> getListPartialEdgeFromVar(int varId, int pos, ArrayList<String> recoveredVar) {
		ArrayList<PartialEdge> edges_fromVar = new ArrayList<>();
		for (int j = 0; j < pos; ++j) {
			if (marked.get(j)) {
				int idj = order.get(j);
				if (isConnected(varId, idj)) {
					String peOrVar = recoveredVar.get(j);
					String rel = mapIDvsRe.getOrDefault(getRelation(varId, idj), "");
					if (rel.equals(""))
						rel = mapIDvsRe.getOrDefault(getRelation(idj, varId), "");
					edges_fromVar.add(new PartialEdge(peOrVar, rel));
				}
			}
		}
		return edges_fromVar;
	}

	private boolean processNext(int pos) {
		try {
			int varId = order.get(pos);

			ArrayList<Integer> candidateID = getListCandidate(varId);

			if (candidateID.size() == 0) {
				return false;
			}

			ArrayList<Pair<ArrayList<String>, Double>> topResult = new ArrayList<>();
			ArrayList<PartialEdge> edgesOfVarId = getListPartialEdgeFromPE(varId);
			ArrayList<ArrayList<Integer>> resAll = new ArrayList<>();
			ArrayList<String> lastName = new ArrayList<>();

			for (ArrayList<String> recoveredVar : currRecovering) {

				for (int j = 0; j < pos; ++j)
					if (marked.get(j)) {
						int idj = order.get(j);
						if (isConnected(varId, idj)) continue;
						String nameJ = recoveredVar.get(j);
						if (mapCountPartialGraph.containsKey(new Pair<>(idj, nameJ))) continue;
						PartialCodeGraph pgraph = new PartialCodeGraph(mapIdVarvsPartialEdges.getOrDefault(idj, new ArrayList<>()), nameJ);
						mapCountPartialGraph.put(new Pair<>(idj, nameJ), countFunctioncontains(pgraph));
					}

				ArrayList<PartialEdge> edges_fromVar = getListPartialEdgeFromVar(varId, pos, recoveredVar);
				edges_fromVar.addAll(edgesOfVarId);

				for (int i : candidateID) {
					String varName = dc.getVarNameItemByID(i).getValue();
					ArrayList<Integer> resList = new ArrayList<>();

					PartialCodeGraph pg = new PartialCodeGraph(edges_fromVar, varName);
					int cntVarID = countFunctioncontains(pg);


					for (int j = 0; j < pos; ++j)
						if (marked.get(j)) {
							int idj = order.get(j);
							String nameJ = recoveredVar.get(j);
							int cc = mapCountPartialGraph.getOrDefault(new Pair<>(idj, nameJ), 0);
							if (isConnected(varId, idj)) {
								ArrayList<PartialEdge> edgesOfVarJ = mapIdVarvsPartialEdges.getOrDefault(idj, new ArrayList<>());

								String rel = mapIDvsRe.getOrDefault(getRelation(varId, idj), "");
								if (rel.equals(""))
									rel = mapIDvsRe.getOrDefault(getRelation(idj, varId), "");

								edgesOfVarJ.add(new PartialEdge(varName, rel));
								PartialCodeGraph pgj = new PartialCodeGraph(edgesOfVarJ, nameJ);
								cc += countFunctioncontains(pgj);
							}
							resList.add(cc);
						} else resList.add(0);

					resList.add(cntVarID);

					resAll.add(resList);
					lastName.add(varName);

//					int resTmp = 0;
//					for (int j = 0; j < resList.size(); ++j) resTmp += resList.get(j);

//					if (resTmp > 0) {
//						ArrayList<String> newRecover = new ArrayList<>(recoveredVar);
//						newRecover.add(varName);
//						topResult.add(new Pair<>(newRecover, resTmp));
//					}

					// This code is for watching graphs founded or not founded
					if (cntVarID > 0) {
						graphFound++;
						if (graphFound <= 2) {
							sampleGraphFound += "---" + "\n";
							sampleGraphFound += pg.toString();
						}
					}
					else {
						graphNotFound++;
						if (graphNotFound <= 2) {
							sampleGraphNotFound += "---" + "\n";
							sampleGraphNotFound += pg.toString();
						}
					}
					// End watching
				}

			}

			// TODO re-check this code
			ArrayList<Integer> maxV = new ArrayList<>();
			for (int i = 0; i <= pos; ++i)
			if ((i < pos && marked.get(i)) || i == pos)
			{
				int tmax = 0;
				for (int j = 0; j < resAll.size(); ++j) {
					tmax = Math.max(tmax, resAll.get(j).get(i));
				}
				maxV.add(tmax);
			} else maxV.add(0);

			for (int j = 0; j < resAll.size(); ++j) {
				double resJ = 0;
				for (int i = 0; i <= pos; ++i)
					if (i == pos || marked.get(i)) {
						if ((double) maxV.get(i) != 0)
							resJ += (double) resAll.get(j).get(i) / (double) maxV.get(i);
					}
				resJ /= (double)(pos);
				ArrayList<String> newRecover = new ArrayList<>(currRecovering.get(j));
				newRecover.add(lastName.get(j));
				topResult.add(new Pair<>(newRecover, resJ));
			}

			//
			if (topResult.size() == 0) return false;

			topResult.sort(new Comparator<Pair<ArrayList<String>, Double>>() {
				@Override
				public int compare(Pair<ArrayList<String>, Double> o1, Pair<ArrayList<String>, Double> o2) {
					return o2.getValue().compareTo(o1.getValue());
				}
			});

			ArrayList<ArrayList<String>> tmpRecovering = new ArrayList<>();
			for (int i = 0; i < Math.min(topResult.size(), TOPK); ++i) {
				tmpRecovering.add(topResult.get(i).getKey());
			}
			currRecovering = tmpRecovering;
			mapIdVarvsPartialEdges.put(varId, edgesOfVarId);
			return true;

		} catch (Exception e) {
//			LOGGER.info(e.getMessage() + " " + Integer.toString(order.get(pos)));
			return false;
		}
	}

	private void loadData() {
		try {
			loadProgramEntityFromFile(inputDir + programEntityFile);
			loadRelationFromFile(inputDir + relationFile);
			loadVarNameFromFile(inputDir + varNameFile);
			loadRelationRecordFromFile(inputDir + relationRecordFile);
		} catch (Exception e) {
			System.out.println("Error loading data " + this.inputDir);
			dataLoadedSuccessfully = false;
		}
	}

	private void loadRelationRecordFromFile(String filename) {
		try {
			String data = FileIO.readStringFromFile(filename);
			String[] parts = data.split("\\n");
			for (int i = 0; i < parts.length; ++i) {
				String[] tmp = parts[i].split(" ");
				try {
					int idPE = Integer.parseInt(tmp[0]);
					int idVN = Integer.parseInt(tmp[1]);
					int idRE = Integer.parseInt(tmp[2]);
					int var_var = Integer.parseInt(tmp[3]);
					int freq = Integer.parseInt(tmp[4]);

					Pair<Integer, Integer> currPair = new Pair<>(idPE, idRE);
					Pair<Integer, Pair<Integer, Integer>> recordPair = new Pair<>(idVN, currPair);

					mapRecordvsType.put(recordPair, var_var);

					if (var_var == 0) {
						if (!mapVarNameIDvsPeReList.containsKey(idVN)) {
							ArrayList<Pair<Integer, Integer>> tmpArrary = new ArrayList<>();
							tmpArrary.add(currPair);
							mapVarNameIDvsPeReList.put(idVN, tmpArrary);
						} else {
							mapVarNameIDvsPeReList.get(idVN).add(currPair);
						}
					} else {
						if (idPE == idVN) continue;
						setVarVar.add(new Pair<>(idPE, idVN));
						setVarVar.add(new Pair<>(idVN, idPE));
						if (!mapVarNameIDvsVar.containsKey(idVN)) {
							ArrayList<Pair<Integer, Integer>> tmpArray = new ArrayList<>();
							tmpArray.add(currPair);
							mapVarNameIDvsVar.put(idVN, tmpArray);
						} else {
							mapVarNameIDvsVar.get(idVN).add(currPair);
						}
					}
				} catch (Exception e) {
					System.out.println(e.getMessage() + " " + filename);
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage() + " " + filename);
		}
	}

	private void loadVarNameFromFile(String filename) {
		String data = FileIO.readStringFromFile(filename);
		String[] parts = data.split("\\n");
		for (int i = 0; i < parts.length; ++i) {
			String[] tmp = parts[i].split(" ");
			try {
				int idx = Integer.parseInt(tmp[0]);
				if (!tmp[1].equals("")) {
					mapIDvsvarName.put(idx, tmp[1]);
					varNameIDSet.add(idx);
				}
			} catch (Exception e) {
				System.out.println(e.getMessage() + " " + filename);
			}
		}
	}

	private void loadProgramEntityFromFile(String filename) {
		String data = FileIO.readStringFromFile(filename);
		String[] parts = data.split("\\n");
		for (int i = 0; i < parts.length; ++i) {
			String[] tmp = parts[i].split(" ");
			try {
				int idx = Integer.parseInt(tmp[0]);
				//int freq = Integer.parseInt(tmp[2]);
				if (!tmp[1].equals(""))
					mapIDvsPe.put(idx, tmp[1]);

			} catch (Exception e) {
				System.out.println(e.getMessage() + " " + filename);
			}
		}
	}

	private void loadRelationFromFile(String filename) {
		String data = FileIO.readStringFromFile(filename);
		String[] parts = data.split("\\n");
		for (int i = 0; i < parts.length; ++i) {
			String[] tmp = parts[i].split(" ");
			try {
				int idx = Integer.parseInt(tmp[0]);
				if (!tmp[1].equals(""))
					mapIDvsRe.put(idx, tmp[1]);
			} catch (Exception e) {
				System.out.println(e.getMessage() + " " + filename);
			}
		}
	}

	public ArrayList<Integer> getRecoveredOrder() {
		ArrayList<Integer> tmp = new ArrayList<>();
		HashMap<Integer, Integer> connections = new HashMap<>();
		HashSet<Integer> marked = new HashSet<>();

		for (Pair<Integer, Pair<Integer, Integer>> key : mapRecordvsType.keySet()) {
			int inc = (mapRecordvsType.get(key) == 0)? 1 : 0;
			if (connections.containsKey(key.getKey())) {
				connections.put(key.getKey(), connections.get(key.getKey()) + inc);
			} else {
				connections.put(key.getKey(), inc);
			}

		}

		for (int i =  0; i < varNameIDSet.size(); ++i) {
			int idPos = 0;
			int maxV = -1;
			for(int id : connections.keySet())
				if (!marked.contains(id) && connections.get(id) > maxV) {
					maxV = connections.get(id);
					idPos = id;
				}
			tmp.add(idPos);
			marked.add(idPos);
			if (mapVarNameIDvsVar.containsKey(idPos)) {
				for (Pair<Integer, Integer> p : mapVarNameIDvsVar.get(idPos)) {
					if (!marked.contains(p.getKey()) && connections.containsKey(p.getKey())) {
						if (connections.get(p.getKey()) > 0)
							connections.put(p.getKey(), connections.get(p.getKey()) - 1);
					}
				}
			}
		}
		return tmp;
	}

	private ArrayList<Integer> getListCandidate(int idVar) {

		ArrayList<BakerFreqItem> bakerFreqItemList = new ArrayList<>();
		ArrayList<Pair<Integer, Integer>> peReList = mapVarNameIDvsPeReList.getOrDefault(idVar, new ArrayList<>());

		for (Pair<Integer, Integer> pp : peReList) {
			String tmpPe = mapIDvsPe.getOrDefault(pp.getKey(), "");
			String tmpRe = mapIDvsRe.getOrDefault(pp.getValue(), "");
			int originalPeID = dc.getProgramEntityID(tmpPe);
			int originalReId = dc.getRelationID(tmpRe);

			BakerFreqItem currBi = dc.getBakerFreqItemFromPeRe(originalPeID, originalReId);
			bakerFreqItemList.add(currBi);
		}

		BakerFreqEngine be = new BakerFreqEngine(idVar, bakerFreqItemList);
		ArrayList<Pair<Integer, Double>> resPair = be.getFinalCandidateList();
		ArrayList<Integer> res = new ArrayList<>();
		for (Pair<Integer, Double> pp : resPair) {
			res.add(pp.getKey());
			if (res.size() >= TOP_CANDIDATE) break;
		}
		return res;
	}
	public String getInputDir() {
		return inputDir;
	}

	public static void main(String[] args) {
		System.out.println("Started testingFunctionReader...");
//		MainDataCenter dc = new MainDataCenter();
//		TestingFunctionReader tfr = new TestingFunctionReader(testingDataDir, dc);
//		tfr.process();
		System.out.println("Finished testingFunctionReader...");
	}
}
