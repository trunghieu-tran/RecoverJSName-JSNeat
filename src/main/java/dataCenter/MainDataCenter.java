package dataCenter;

import dataCenter.utils.FileIO;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author Harry Tran on 5/28/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class MainDataCenter {
	private final static Logger LOGGER = Logger.getLogger(MainDataCenter.class.getName());

	private static final String dataDir = "./resources/parsedData/";
	private static final String programEntityFile = dataDir + "programEntityData";
	private static final String varNameFile = dataDir + "varNameData";
	private static final String relationFile = dataDir + "relationData";
	private static final String relationRecordFile = dataDir + "relationRecordData";

	private ArrayList<ProgramEntityItem> programEntityList = new ArrayList<>();
	private ArrayList<VarNameItem> varNameItemList = new ArrayList<>();
	private ArrayList<RelationItem> relationList = new ArrayList<>();
	private ArrayList<RelationRecord> relationRecordList = new ArrayList<>();

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
	}

	private void loadRelationFromFile(String filename) {
		String data = FileIO.readStringFromFile(filename);
		String[] parts = data.split("\\n");
		for (int i = 0; i < parts.length; ++i) {
			String[] tmp = parts[i].split(" ");
			try {
				int idx = Integer.parseInt(tmp[0]);
				int freq = Integer.parseInt(tmp[2]);
				relationList.add(new RelationItem(idx, tmp[1], freq));
			} catch (Exception e) {
				LOGGER.info(e.getMessage());
			}
		}
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
				relationRecordList.add(new RelationRecord(idVN, idPE, idRE, freq));
			} catch (Exception e) {
				LOGGER.info(e.getMessage());
			}
		}
	}
	public  void loadData() {
		loadProgramEntityFromFile(programEntityFile);
		loadVarNameFromFile(varNameFile);
		loadRelationFromFile(relationFile);
		loadRelationRecordFromFile(relationRecordFile);

		LOGGER.info(String.valueOf(programEntityList.size()));
		LOGGER.info(String.valueOf(varNameItemList.size()));
		LOGGER.info(String.valueOf(relationList.size()));
		LOGGER.info(String.valueOf(relationRecordList.size()));
	}

	public static void main(String[] args) {
		System.out.println("=== Started ...");
		MainDataCenter dataCenter = new MainDataCenter();
		dataCenter.loadData();
		System.out.println("... Finished ===");
	}
}
