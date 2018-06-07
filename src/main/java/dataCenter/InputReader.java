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
public class InputReader {
	private final static Logger LOGGER = Logger.getLogger(InputReader.class.getName());
	private static final String dataDir = "./resources/parsedData/testingData/";
	private static String inputDir = dataDir ; // Set by default

	private static final String programEntityFile =  "peData.txt";
	private static final String varNameFile = "varNameData.txt";
	private static final String relationFile = "relData.txt";
	private static final String relationRecordFile = "recordData.txt";

	private HashMap<Integer, String> mapIDvsPe = new HashMap<>();
	private HashMap<Integer, String> mapIDvsRe = new HashMap<>();
	private HashMap<Integer, String> mapIDvsvarName = new HashMap<>();
	private HashMap<Integer, ArrayList< Pair<Integer, Integer> > > mapVarNameIDvsPeReList = new HashMap<>();

	private HashSet<Integer> varNameIDSet = new HashSet<>();

	public InputReader(String inputDir) {
		this.inputDir = inputDir;
		loadData();
	}

	public InputReader() {
		loadData();
	}

	private void loadData() {
		loadProgramEntityFromFile(inputDir + programEntityFile);
		loadRelationFromFile(inputDir + relationFile);
		loadVarNameFromFile(inputDir + varNameFile);

		loadRelationRecordFromFile(inputDir + relationRecordFile);
//		savePeReItemsToFile(peReFile);
//		LOGGER.info("DONE loadData");
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

				Pair<Integer, Integer> currPair = new Pair<> (idPE, idRE);
				if (!mapVarNameIDvsPeReList.containsKey(idVN)) {
					ArrayList< Pair<Integer, Integer> > tmpArrary = new ArrayList<>();
					tmpArrary.add(currPair);
					mapVarNameIDvsPeReList.put(idVN, tmpArrary);
				} else {
					mapVarNameIDvsPeReList.get(idVN).add(currPair);
				}
			} catch (Exception e) {
				LOGGER.info(e.getMessage());
			}
		}

		LOGGER.info("DONE loadRelationRecordFromFile.");
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
				LOGGER.info(e.getMessage());
			}
		}
		LOGGER.info("DONE loadVarNameFromFile.");
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
				LOGGER.info(e.getMessage());
			}
		}
		LOGGER.info("DONE loadProgramEntityFromFile.");
	}

	private void loadRelationFromFile(String filename) {
		String data = FileIO.readStringFromFile(filename);
		String[] parts = data.split("\\r\\n");
		for (int i = 0; i < parts.length; ++i) {
			String[] tmp = parts[i].split(" ");
			try {
				int idx = Integer.parseInt(tmp[0]);
				mapIDvsRe.put(idx, tmp[1]);
			} catch (Exception e) {
				LOGGER.info(e.getMessage());
			}
		}
		LOGGER.info("DONE loadRelationFromFile.");
	}

	public HashSet<Integer> getVarNameIDSet() {
		return varNameIDSet;
	}

	public ArrayList<Pair<Integer, Integer> > getListPeReByID(int id) {
		return mapVarNameIDvsPeReList.get(id);
	}

	public String getVarNameByID(int id) {
		return mapIDvsvarName.get(id);
	}

	public String getPeByID(int id) {
		return mapIDvsPe.get(id);
	}

	public String getReByID(int id) {
		return mapIDvsRe.get(id);
	}

	public static void main(String[] args) {
		System.out.println("InputReader started");

		ArrayList<String> dirList = FileIO.getAllSubdirectoryFromDirectory(dataDir);
		for (String str : dirList) {
			InputReader ir = new InputReader(dataDir + str + "/");
		}
		System.out.println("InputReader finished");
	}
}
