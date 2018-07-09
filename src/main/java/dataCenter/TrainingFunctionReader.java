package dataCenter;

import dataCenter.utils.FileIO;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * @author Harry Tran on 6/4/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */

public class TrainingFunctionReader implements Runnable{
	private final static Logger LOGGER = Logger.getLogger(TrainingFunctionReader.class.getName());
	private static String dataDir = "./resources/parsedData/trainingData/TrainSet/";
	private String inputDir ; // Set by default
	private int numOfDir;
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
	private ArrayList<String> varNameList = new ArrayList<>();

	private FullCodeGraph graph;
	private PartialCodeGraph pgraph;
	private String var_left;

	private int numVar;
	private int numVarVar = 0;
	private int numRecord = 0;
	private HashSet<Integer> varNameIDSet = new HashSet<>();
	private boolean isTrainingData;

	private boolean dataLoadedSuccessfully = true;

	public void setNumOfDir(int num) {
		this.numOfDir = num;
	}

	public void run()
	{
		try {
			loadData();
			if (isQualifiedData()) {
				genFullCodeGraph();
				if (!isTrainingData) {
					genPartialGraph();
				}
			}
		}
		catch (Exception e) {
			// Throwing an exception
			LOGGER.info(e.getMessage());
		}
		System.out.println("[" + Integer.toString(numOfDir) +"] Exit thread " +  inputDir);
	}

	public TrainingFunctionReader(String inputDir, boolean isTraningData) {
		this.inputDir = inputDir;
		this.isTrainingData = isTraningData;
	}

	private void genPartialGraph() {
		ArrayList<PartialEdge> edges = new ArrayList<>();
		for (HashMap.Entry<Integer, ArrayList< Pair<Integer, Integer> >> entry : mapVarNameIDvsPeReList.entrySet()) {
			String varName = mapIDvsvarName.get(entry.getKey());
			var_left = varName;
			for (Pair<Integer, Integer> p: entry.getValue()) {
				String peOrVar = mapIDvsPe.getOrDefault(p.getKey(), " ");
				String rel = mapIDvsRe.getOrDefault(p.getValue(), " ");
				edges.add(new PartialEdge(peOrVar, rel));
			}
			break;
		}
		pgraph = new PartialCodeGraph(edges, var_left);
	}

	public FullCodeGraph getGraph() {
		return graph;
	}

	public PartialCodeGraph getPgraph() {
		return pgraph;
	}

	private void genFullCodeGraph() {
		ArrayList<Edge> edges = new ArrayList<>();
		for (HashMap.Entry<Integer, ArrayList< Pair<Integer, Integer> >> entry : mapVarNameIDvsPeReList.entrySet()) {
			String varName = mapIDvsvarName.get(entry.getKey());
			for (Pair<Integer, Integer> p: entry.getValue()) {
				String peOrVar = mapIDvsPe.getOrDefault(p.getKey(), " ");
				String rel = mapIDvsRe.getOrDefault(p.getValue(), " ");
				if (!peOrVar.equals(varName)) {
					edges.add(new Edge(varName, peOrVar, rel));
				}
			}
		}

		for (HashMap.Entry<Integer, ArrayList< Pair<Integer, Integer> >> entry : mapVarNameIDvsVar.entrySet()) {
			String varName = mapIDvsvarName.get(entry.getKey());
			for (Pair<Integer, Integer> p: entry.getValue()) {
				String peOrVar = mapIDvsvarName.getOrDefault(p.getKey(), " ");
				String rel = mapIDvsRe.getOrDefault(p.getValue(), " ");
				if (!peOrVar.equals(varName)) {
					edges.add(new Edge(varName, peOrVar, rel));
				}
			}
		}

		graph = new FullCodeGraph(edges);
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

	public boolean isQualifiedData() {
		return dataLoadedSuccessfully && mapIDvsPe.size() > 0 && mapIDvsvarName.size() > 0 && mapRecordvsType.size() >= 5;
	}

	public int getNumVarVar() {
		return numVarVar;
	}

	public int getNumRecord() {
		return numRecord;
	}

	private void loadRelationRecordFromFile(String filename) {
		String data = FileIO.readStringFromFile(filename);
		String[] parts = data.split("\\n");
		numRecord = parts.length;
		for (int i = 0; i < parts.length; ++i) {
			String[] tmp = parts[i].split(" ");
			try {
				int idPE = Integer.parseInt(tmp[0]);
				int idVN = Integer.parseInt(tmp[1]);
				int idRE = Integer.parseInt(tmp[2]);
				int var_var = Integer.parseInt(tmp[3]);
				int freq = Integer.parseInt(tmp[4]);

				if (var_var == 1) numVarVar++;

				Pair<Integer, Integer> currPair = new Pair<> (idPE, idRE);
				Pair<Integer, Pair<Integer, Integer>> recordPair= new Pair<> (idVN, currPair);

				mapRecordvsType.put(recordPair, var_var);

				if (var_var == 0) {
					if (!mapVarNameIDvsPeReList.containsKey(idVN)) {
						ArrayList<Pair<Integer, Integer>> tmpArray = new ArrayList<>();
						tmpArray.add(currPair);
						mapVarNameIDvsPeReList.put(idVN, tmpArray);
					} else {
						mapVarNameIDvsPeReList.get(idVN).add(currPair);
					}
				} else {
					if (idPE == idVN) continue;
					if (!mapVarNameIDvsVar.containsKey(idVN)) {
						ArrayList<Pair<Integer, Integer>> tmpArray = new ArrayList<>();
						tmpArray.add(currPair);
						mapVarNameIDvsVar.put(idVN, tmpArray);
					} else {
						mapVarNameIDvsVar.get(idVN).add(currPair);
					}
				}

//				if (var_var == 1) {
//					if (!mapVarNameIDvsPeReList.containsKey(idVN)) {
//						ArrayList< Pair<Integer, Integer> > tmpArray = new ArrayList<>();
//						tmpArray.add(currPair);
//						mapVarNameIDvsPeReList.put(idVN, tmpArray);
//					} else {
//						mapVarNameIDvsPeReList.get(idVN).add(currPair);
//					}
//				}
			} catch (Exception e) {
//				LOGGER.info(e.getMessage() + " " + filename);
				System.out.println(e.getMessage() + " " + filename);
			}
		}

//		LOGGER.info("DONE loadRelationRecordFromFile.");
	}

	private void loadVarNameFromFile(String filename) {
		String data = FileIO.readStringFromFile(filename);
		String[] parts = data.split("\\n");
		for (int i = 0; i < parts.length; ++i) {
			String[] tmp = parts[i].split(" ");
			try {
				int idx = Integer.parseInt(tmp[0]);
				mapIDvsvarName.put(idx, tmp[1]);
				varNameIDSet.add(idx);
			} catch (Exception e) {
//				LOGGER.info(e.getMessage() + " " + filename);
				System.out.println(e.getMessage() + " " + filename);
			}
		}
		this.numVar = parts.length;
//		LOGGER.info("DONE loadVarNameFromFile.");
	}

	private void loadProgramEntityFromFile(String filename) {
		String data = FileIO.readStringFromFile(filename);
		String[] parts = data.split("\\n");
		for (int i = 0; i < parts.length; ++i) {
			String[] tmp = parts[i].split(" ");
			try {
				int idx = Integer.parseInt(tmp[0]);
				//int freq = Integer.parseInt(tmp[2]);
				mapIDvsPe.put(idx, tmp[1]);
			} catch (Exception e) {
//				LOGGER.info(e.getMessage() + " " + filename);
				System.out.println(e.getMessage() + " " + filename);
			}
		}
//		LOGGER.info("DONE loadProgramEntityFromFile.");
	}

	private void loadRelationFromFile(String filename) {
		String data = FileIO.readStringFromFile(filename);
		String[] parts = data.split("\\n");
		for (int i = 0; i < parts.length; ++i) {
			String[] tmp = parts[i].split(" ");
			try {
				int idx = Integer.parseInt(tmp[0]);
				mapIDvsRe.put(idx, tmp[1]);
			} catch (Exception e) {
//				LOGGER.info(e.getMessage() + " " + filename);
				System.out.println(e.getMessage() + " " + filename);
			}
		}
//		LOGGER.info("DONE loadRelationFromFile.");
	}

	public static void main(String[] args) {
		System.out.println("TrainingFunctionReader started");

		ArrayList<String> dirList = FileIO.getAllSubdirectoryFromDirectory(dataDir);

		for (String str : dirList) {
			System.out.println("Parsing loading function " + str);
			TrainingFunctionReader ir = new TrainingFunctionReader(str, true);
			System.out.println("DONE loading function " + str);
		}
		System.out.println("TrainingFunctionReader finished");
	}
}
