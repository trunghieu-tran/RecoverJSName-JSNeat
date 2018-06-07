package dataCenter;

import baker.BakerItem;
import bakerFreq.BakerFreqItem;
import dataCenter.utils.FileIO;
import dataCenter.utils.NormalizationTool;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * @author Harry Tran on 5/28/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class MainDataCenter {
	private final static int LOWER_BOUND_FREQUENCY = 1000;
	private final static Logger LOGGER = Logger.getLogger(MainDataCenter.class.getName());

	private static final String dataTrainingDir = "./resources/parsedData/trainingData/";
	private static final String dataTrainingCFInputDir = "./resources/parsedData/trainingData/CFInput/";
	private static final String resourcesDir = "./resources/";

	private static final String programEntityFile = dataTrainingDir + "peData.txt";
	private static final String varNameFile = dataTrainingDir + "varNameData.txt";
	private static final String relationFile = dataTrainingDir + "relData.txt";
	private static final String relationRecordFile = dataTrainingDir + "recordData.txt";

	private static final String peReFile = dataTrainingDir + "peReData";
	private static final String cfInputFile = dataTrainingCFInputDir + "cfInputData";
	private static final String cfNormInputFile = dataTrainingCFInputDir + "cfNormInputData";

	private static final String jsReservedKeywords = resourcesDir + "JSReservedKeywords";

	private ArrayList<ProgramEntityItem> programEntityList = new ArrayList<>();
	private ArrayList<VarNameItem> varNameItemList = new ArrayList<>();
	private ArrayList<RelationItem> relationList = new ArrayList<>();
	private ArrayList<RelationRecord> relationRecordList = new ArrayList<>();
	private ArrayList<PeReItem> peReItemList = new ArrayList<>();
	private HashSet<String> reservedKeywordsSet = new HashSet<>();

	private HashMap<Pair<Integer, Integer>, Integer> mapPeRevsIndex = new HashMap<>();
	private HashMap<Pair<Integer, Integer>, ArrayList<Integer> > mapPeRevsListVarName = new HashMap<>();
	private HashMap<Pair<Integer, Integer>, ArrayList<Pair<Integer, Integer> > > mapPeRevsListVarNameFreq = new HashMap<>();
	private HashMap<Pair<Integer, Integer>, BakerItem > mapPeRevsBakerItem = new HashMap<>();
	private HashMap<Pair<Integer, Integer>, BakerFreqItem > mapPeRevsBakerFreqItem = new HashMap<>();

	private HashMap<String, Integer> mapVarNamevsId = new HashMap<>();
	private HashMap<String, Integer> mapPevsId = new HashMap<>();
	private HashMap<String, Integer> mapRevsId = new HashMap<>();

	private HashMap<Integer, VarNameItem> mapIdvsVarNamItem = new HashMap<>();
	private HashMap<Integer, ProgramEntityItem> mapIdvsPeItem = new HashMap<>();

	public MainDataCenter() {
		loadData();
	}

	private void loadProgramEntityFromFile(String filename) {
		String data = FileIO.readStringFromFile(filename);
		String[] parts = data.split("\\n");
		for (int i = 0; i < parts.length; ++i) {
			String[] tmp = parts[i].split(" ");
			try {
				int idx = Integer.parseInt(tmp[0]);
				int freq = Integer.parseInt(tmp[2]);
				ProgramEntityItem pei = new ProgramEntityItem(idx, tmp[1], freq);
				programEntityList.add(pei);
				mapPevsId.put(tmp[1], idx);
				mapIdvsPeItem.put(idx, pei);
			} catch (Exception e) {
				LOGGER.info(e.getMessage());
			}
		}
		LOGGER.info("DONE loadProgramEntityFromFile.");
	}

	private void loadVarNameFromFile(String filename) {
		String data = FileIO.readStringFromFile(filename);
		String[] parts = data.split("\\n");
		for (int i = 0; i < parts.length; ++i) {
			String[] tmp = parts[i].split(" ");
			try {
				int idx = Integer.parseInt(tmp[0]);
				int freq = Integer.parseInt(tmp[2]);
				VarNameItem vni = new VarNameItem(idx, tmp[1], freq);
				varNameItemList.add(vni);
				mapVarNamevsId.put(tmp[1], idx);
				mapIdvsVarNamItem.put(idx, vni);
			} catch (Exception e) {
				LOGGER.info(e.getMessage());
			}
		}
		LOGGER.info("DONE loadVarNameFromFile.");
	}

	private void loadRelationFromFile(String filename) {
		String data = FileIO.readStringFromFile(filename);
		String[] parts = data.split("\\n");
		for (int i = 0; i < parts.length; ++i) {
			String[] tmp = parts[i].split(" ");
			try {
				int idx = Integer.parseInt(tmp[0]);
				int freq = -1;
				relationList.add(new RelationItem(idx, tmp[1], freq));
				mapRevsId.put(tmp[1], idx);
			} catch (Exception e) {
				LOGGER.info(e.getMessage());
			}
		}
		LOGGER.info("DONE loadRelationFromFile.");
	}

	private void loadRelationRecordFromFile(String filename) {
		String data = FileIO.readStringFromFile(filename);
		String[] parts = data.split("\\n");
		for (int i = 0; i < parts.length; ++i) {
			String[] tmp = parts[i].split(" ");
			try {
				int idPE = Integer.parseInt(tmp[0]);
				int idVN = Integer.parseInt(tmp[1]);
				int idRE = Integer.parseInt(tmp[2]);
				int freq = Integer.parseInt(tmp[3]);
				Pair<Integer, Integer> currPair = new Pair<> (idPE, idRE);
				relationRecordList.add(new RelationRecord(idVN, idPE, idRE, freq));


				if (!mapPeRevsIndex.containsKey(currPair)) {
					int idPeRe = peReItemList.size();
					peReItemList.add(new PeReItem(idPeRe, idPE, idRE));
					mapPeRevsIndex.put(currPair, idPeRe);
				}

				if (!mapPeRevsListVarName.containsKey(currPair)) {
					ArrayList<Integer> listVarName = new ArrayList<>();
					listVarName.add(idVN);
					mapPeRevsListVarName.put(currPair, listVarName);
				} else {
					mapPeRevsListVarName.get(currPair).add(idVN);
				}

				if (!mapPeRevsListVarNameFreq.containsKey(currPair)) {
					ArrayList<Pair<Integer, Integer>> listVarNameFreq = new ArrayList<>();
					listVarNameFreq.add(new Pair<>(idVN, freq));
					mapPeRevsListVarNameFreq.put(currPair, listVarNameFreq);
				} else {
					mapPeRevsListVarNameFreq.get(currPair).add(new Pair<>(idVN, freq));
				}
			} catch (Exception e) {
				LOGGER.info(e.getMessage());
			}
		}

		for (Pair<Integer, Integer> key : mapPeRevsListVarName.keySet()) {
			BakerItem bi = new BakerItem(key.getKey(), key.getValue(), mapPeRevsListVarName.get(key));
			mapPeRevsBakerItem.put(key, bi);
		}

		for (Pair<Integer, Integer> key : mapPeRevsListVarNameFreq.keySet()) {
			BakerFreqItem bi = new BakerFreqItem(key.getKey(), key.getValue(), mapPeRevsListVarNameFreq.get(key));
			mapPeRevsBakerFreqItem.put(key, bi);
		}
		LOGGER.info("DONE loadRelationRecordFromFile.");
	}


	public BakerItem getBakerItemFromPeRe(int pe, int re) {
		return mapPeRevsBakerItem.get(new Pair<>(pe, re));
	}

	public BakerFreqItem getBakerFreqItemFromPeRe(int pe, int re) {
		return mapPeRevsBakerFreqItem.get(new Pair<>(pe, re));
	}

	public void generateInputForCF() {
		StringBuilder res = new StringBuilder();
		for (RelationRecord r: relationRecordList) {
			int varNameId = r.getIdVarName();
			int peId = r.getIdProgramEntity();
			Pair<Integer, Integer> tmp = new Pair<>(r.getIdProgramEntity(), r.getIdRelation());
			boolean isSatisfied = mapPeRevsIndex.containsKey(tmp);
			isSatisfied &= getVarNameItemByID(varNameId).getFrequency() >= LOWER_BOUND_FREQUENCY;
			isSatisfied &= getProgramEntityItemByID(peId).getFrequency() >= LOWER_BOUND_FREQUENCY;
			isSatisfied &= !isViolatedReservedKeywords(getVarNameItemByID(varNameId).getValue());
			if (isSatisfied) {
				int idPeRe = mapPeRevsIndex.get(tmp);
				res.append(r.getIdVarName()).append(" ")
						.append(idPeRe).append(" ")
						.append(r.getFrequency()).append("\n");
			}
		}
		FileIO.writeStringToFile(cfInputFile, res.toString());
		LOGGER.info("DONE generateInputForCF.");
	}

	private void savePeReItemsToFile(String filename) {
		StringBuilder res = new StringBuilder();
		Collections.sort(peReItemList, PeReItem.PeReItemCompatator);
		for (PeReItem p: peReItemList) {
			res.append(p.toString()).append("\n");
		}
		FileIO.writeStringToFile(filename, res.toString());
		LOGGER.info("DONE savePeReItemsToFile.");
	}

	private void loadJSReservedKeywordsFromFile(String filename) {
		String data = FileIO.readStringFromFile(filename);
		String[] parts = data.split("\\n");
		Collections.addAll(reservedKeywordsSet, parts);
		LOGGER.info("DONE loadJSReservedKeywordsFromFile.");
	}

	private void loadData() {
		loadJSReservedKeywordsFromFile(jsReservedKeywords);
		loadProgramEntityFromFile(programEntityFile);
		loadVarNameFromFile(varNameFile);
		loadRelationFromFile(relationFile);
		loadRelationRecordFromFile(relationRecordFile);
		savePeReItemsToFile(peReFile);
		LOGGER.info("DONE loadData");
	}

	public int getVarNameFreqencyByID(int id) {
		return getVarNameItemByID(id).getFrequency();
	}

	public boolean isViolatedReservedKeywords(String varName) {
		return reservedKeywordsSet.contains(varName);
	}

	public int getVarNameID(String varName) {
		return mapVarNamevsId.getOrDefault(varName, -1);
	}

	public int getProgramEntityID(String pe) {
		return mapPevsId.getOrDefault(pe, -1);
	}

	public int getRelationID(String re) {
		return mapRevsId.getOrDefault(re, -1);
	}

	public ProgramEntityItem getProgramEntityItemByID(int id) {
		return mapIdvsPeItem.getOrDefault(id, null);
	}

	public VarNameItem getVarNameItemByID(int id) {
		return mapIdvsVarNamItem.getOrDefault(id, null);
	}

	public static void main(String[] args) {
		System.out.println("=== Started ...");
		MainDataCenter dataCenter = new MainDataCenter();
//		dataCenter.generateInputForCF();
//		NormalizationTool.normalizeCFMatrix(cfInputFile, cfNormInputFile);
		System.out.println("... Finished ===");
	}
}
