package dataCenter;

import dataCenter.utils.FileIO;
import dataCenter.utils.commandLineRunner;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Harry Tran on 6/13/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class TopicDistributionGenerator {
	private static final String dataDir = "./resources/parsedData/TestSet/";
//	private static final String dataDir = "./resources/parsedData/testingData/";
	private static ArrayList<String> dirList;
	private static final String topicModelFile = "topicModel.txt";
	private static final String topicDistributionFile = "topicDistribution.txt";
	private static final String runTMPredictionForAllFile = "./src/main/bashShell/runTMPredictionForAll.sh";

	public TopicDistributionGenerator() {
		dirList = FileIO.getAllSubdirectoryFromDirectory(dataDir);
//		int i = 0;
//		for (String dir : dirList) {
//			System.out.println(">>> Processing " + dir + "... [" + Integer.toString(++i) + "/" + Integer.toString(dirList.size()) +"]");
//			commandLineRunner.runTopicModelPredictionByFile(this.getTopicModelFile(dir), this.getTopicDistributionFile(dir));
//		}
		printRunTMPredictionForAllFile(dirList, runTMPredictionForAllFile);
	}

	private static String getAbsolutePathFrom(String path) {
		File f = new File(path);
		return f.getAbsolutePath();
	}

	private void printRunTMPredictionForAllFile(ArrayList<String> dirList, String filename) {
		String res = "cd ../python/topicModelling/\n";
		for (String dir :dirList) {
			res += "python3 main.py 4 " + getAbsolutePathFrom(dir + topicModelFile) + " " + getAbsolutePathFrom(dir + topicDistributionFile) + "\n";
			res += "echo \"DONE prediction " + getAbsolutePathFrom(dir + topicModelFile) + "\"\n";
		}
		FileIO.writeStringToFile(filename, res);
	}

	private String getTopicModelFile(String dir) {
		return dir + topicModelFile;
	}

	private String getTopicDistributionFile(String dir) {
		return dir + topicDistributionFile;
	}

	public static void main(String[] args) {
		System.out.println("=== Started TopicDistributionGenerator ...");
		TopicDistributionGenerator tg = new TopicDistributionGenerator();
		System.out.println("... DONE  TopicDistributionGenerator ===");
	}
}
