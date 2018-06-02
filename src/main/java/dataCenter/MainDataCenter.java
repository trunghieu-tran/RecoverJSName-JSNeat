package dataCenter;

import dataCenter.utils.FileIO;
import dataCenter.utils.NormalizationTool;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * @author Harry Tran on 5/28/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class MainDataCenter {
	private final static Logger LOGGER = Logger.getLogger(MainDataCenter.class.getName());

	private static final String dataTrainingDir = "./resources/parsedData/trainingData/";
	private static final String dataTrainingCFInputDir = "./resources/parsedData/trainingData/CFInput/";
	private static final String rescourcesDir = "./resources/";

	private static final String programEntityFile = dataTrainingDir + "peData.txt";
	private static final String varNameFile = dataTrainingDir + "varNameData.txt";
	private static final String relationFile = dataTrainingDir + "relData.txt";
	private static final String relationRecordFile = dataTrainingDir + "recordData.txt";

	private static final String peReFile = dataTrainingDir + "peReData";
	private static final String cfInputFile = dataTrainingCFInputDir + "cfInputData";
	private static final String cfNormInputFile = dataTrainingCFInputDir + "cfNormInputData";

	private static final String jsReservedKeywords = rescourcesDir + "JSReservedKeywords";

	private ArrayList<ProgramEntityItem> programEntityList = new ArrayList<>();
	private ArrayList<VarNameItem> varNameItemList = new ArrayList<>();
	private ArrayList<RelationItem> relationList = new ArrayList<>();
	private ArrayList<RelationRecord> relationRecordList = new ArrayList<>();
	private ArrayList<PeReItem> peReItemList = new ArrayList<>();
	private HashMap<Integer, Pair<Integer, Integer> > mapIndexVsPeRe = new HashMap<>();
	private HashMap<Pair<Integer, Integer>, Integer> mapPeRevsIndex = new HashMap<>();
	private ArrayList<String> reservedKeywordsList = new ArrayList<>();

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
				programEntityList.add(new ProgramEntityItem(idx, tmp[1], freq));
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
				varNameItemList.add(new VarNameItem(idx, tmp[1], freq));
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
				int idPeRe = peReItemList.size();
				relationRecordList.add(new RelationRecord(idVN, idPE, idRE, freq));
				peReItemList.add(new PeReItem(idPeRe, idPE, idRE));
				mapPeRevsIndex.put(new Pair<> (idPE, idRE), idPeRe);
			} catch (Exception e) {
				LOGGER.info(e.getMessage());
			}
		}
		LOGGER.info("DONE loadRelationRecordFromFile.");
	}


	public void generateInputForCF() {
		StringBuilder res = new StringBuilder();
		for (RelationRecord r: relationRecordList) {
			res.append(r.getIdVarName()).append(" ").append(mapPeRevsIndex.get(new Pair<>(r.getIdProgramEntity(), r.getIdRelation()))).append(" ").append(r.getFrequency()).append("\n");
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
		Collections.addAll(reservedKeywordsList, parts);
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



	public static void main(String[] args) {
		System.out.println("=== Started ...");
		MainDataCenter dataCenter = new MainDataCenter();
		dataCenter.generateInputForCF();
		NormalizationTool.normalizeCFMatrix(cfInputFile, cfNormInputFile);
		System.out.println("... Finished ===");
	}
}
