package core;

import dataCenter.MainDataCenter;
import dataCenter.TestingFunctionReader;
import dataCenter.TrainingFunctionReader;
import dataCenter.utils.FileIO;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Harry Tran on 6/19/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class BBS {
	private static final int numberOfThread = 20;
	// LOCAL data
//	private static final String trainingDataDir = "./resources/parsedData/trainingData/TrainSet/";
//	private static final String testingDataDir = "./resources/parsedData/TestSet/";
	// SERVER
	private static final String trainingDataDir = "../../nmt140230/RecoverJSName/TrainSet/";
	private static final String testingDataDir = "../../nmt140230/RecoverJSName/TestSet/";

	private static final String generalResolvedListFile = "./resources/parsedData/trainingData/generalBBS.txt";
	private static final String generalResultFile = "./resources/parsedData/trainingData/generalResultBBS.txt";
	private static final String InOFVocabFile = "./resources/parsedData/trainingData/InOfVocab.txt";
	private static final String OutOFVocabFile = "./resources/parsedData/trainingData/OutOfVocab.txt";
	private static final String UnresolvedFuncFile = "./resources/parsedData/trainingData/UnResolvedFunctions.txt";
	private static final String outputFile = "output.txt";
	private static final String outputDir = "./resources/parsedData/output/";

	private static MainDataCenter dc;
	private ArrayList<String> trainingDirs = new ArrayList<>();
	private ArrayList<String> testingDirs = new ArrayList<>();
	private ArrayList<TrainingFunctionReader> trainingFunctions = new ArrayList<>();
	ArrayList<Pair<TestingFunctionReader, String>> tfrList = new ArrayList<>();
	private String generalInfoStr = "";

	private String sampleGraphFound = "";
	private String sampleGraphNotFound = "";
	private String unresolvedFunc = "";
	private String generalResolvedList = "";

	public BBS() {
		dc = new MainDataCenter();
		loadData();
	}

	private void loadData() {
		ArrayList<String> dirList = FileIO.getAllSubdirectoryFromDirectory(trainingDataDir);
//		ArrayList<String> dirList = FileIO.getAllSubdirectoryFromDirectoryWithNumber(trainingDataDir, 100000);
//		ArrayList<String> dirList2 = FileIO.getAllSubdirectoryFromDirectory(testingDataDir);
		ArrayList<String> dirList2 = FileIO.getAllSubdirectoryFromDirectoryWithNumber(testingDataDir, 20);
		trainingDirs.addAll(dirList);
		testingDirs.addAll(dirList2);

		ArrayList<TrainingFunctionReader> trainingFunctionsTmp = new ArrayList<>();

		ExecutorService executor = Executors.newFixedThreadPool(numberOfThread);
		for (int i = 0; i < trainingDirs.size(); ++i) {
//			System.out.println("Training " + trainingDirs.get(i) + " ...[" + Integer.toString(i+1) + "/" + Integer.toString(trainingDirs.size()) + "]");
			TrainingFunctionReader ir = new TrainingFunctionReader(trainingDirs.get(i), true);
			ir.setNumOfDir(i + 1);
			executor.execute(ir);
			trainingFunctionsTmp.add(ir);
		}

		// This will make the executor accept no new threads
		// and finish all existing threads in the queue
		executor.shutdown();
		// Wait until all threads are finish
		try {
			if (!executor.awaitTermination(7, TimeUnit.DAYS))
				executor.shutdownNow();
		} catch (Exception e) {
			System.out.println("Waiting error");
			executor.shutdownNow();
		}
		System.out.println("Finished all threads for training");

		for (TrainingFunctionReader tf : trainingFunctionsTmp)
			if (tf.isQualifiedData()) trainingFunctions.add(tf);

		generalInfoStr += "N_testing = " + Integer.toString(testingDirs.size()) + "\n";
		generalInfoStr += "N_training = " + Integer.toString(trainingDirs.size()) + "\n";
		generalInfoStr += "N_training qualified = " + Integer.toString(trainingFunctions.size()) + "\n";
	}

	public void process() {
		ExecutorService executor = Executors.newFixedThreadPool(numberOfThread);

		for (int i = 0; i < testingDirs.size(); ++i)
		{
//			System.out.println("Processing " + testingDirs.get(i) + " ...[" + Integer.toString(i+1) + "/" + Integer.toString(testingDirs.size()) + "]");

			TestingFunctionReader tfr = new TestingFunctionReader(testingDirs.get(i), dc, trainingFunctions);
			tfr.setNumOfDir(i);
			executor.execute(tfr);

			tfrList.add(new Pair<>(tfr, testingDirs.get(i) + outputFile));
		}

		executor.shutdown();
		try {
			if (!executor.awaitTermination(7, TimeUnit.DAYS))
				executor.shutdownNow();
		} catch (Exception e) {
			System.out.println("Waiting error");
			executor.shutdownNow();
		}

		int cnt = 0;
		for (Pair<TestingFunctionReader, String> tf : tfrList) {
			if (tf.getKey().isQualified()) ++cnt;
		}

		generalInfoStr += "N_testing qualified = " + Integer.toString(cnt) + "\n";
		System.out.println("Finished all threads for testing");
	}

	public void writeOutInfo() {
		FileIO.writeStringToFile(InOFVocabFile, sampleGraphFound);
		FileIO.writeStringToFile(OutOFVocabFile, sampleGraphNotFound);
		FileIO.writeStringToFile(generalResultFile, generalInfoStr);
		FileIO.writeStringToFile(UnresolvedFuncFile, unresolvedFunc);
		FileIO.writeStringToFile(generalResolvedListFile, generalResolvedList);
	}

	public void analysis(int topK, boolean isWritten) {
		double sumAccuracy = 0.0;

		int totalRun = 0;
		int totalResolved = 0;

		int totalVar = 0;
		int totalResolvedVar = 0;
		int totalGraphFound = 0;
		int totalGraphNotFound = 0;



		for (Pair<TestingFunctionReader, String> p : tfrList) {
			if (p.getKey().isRun()) {
				totalRun++;
				if (p.getKey().isSolved) {
					totalGraphFound += p.getKey().graphFound;
					totalGraphNotFound += p.getKey().graphNotFound;
					totalResolved++;
					sumAccuracy += p.getKey().getAccuracyTop(topK);
					if (isWritten) {
						generalResolvedList += p.getValue() + "\n";
						sampleGraphFound += p.getKey().sampleGraphFound;
						sampleGraphNotFound += p.getKey().sampleGraphNotFound;
						// write output
						String outF = outputDir + Integer.toString(totalResolved) + outputFile;

						FileIO.writeStringToFile(outF, p.getKey().getPrintOutResult());
					}

					totalVar += p.getKey().getVariableNumber();
					totalResolvedVar += p.getKey().getResolvedNum();
				}
			} else {
				unresolvedFunc += "\n" + p.getKey().getInputDir();
			}
		}


		generalInfoStr += "\n" + "Top " + Integer.toString(topK) + " = " + Double.toString(sumAccuracy / totalResolved);

		if (isWritten) {
			generalInfoStr += "\n" + "- By function info -";
			generalInfoStr += "\n" + "nAll = " + Integer.toString(tfrList.size());
			generalInfoStr += "\n" + "nRun = " + Integer.toString(totalRun);
			generalInfoStr += "\n" + "nResolved = " + Integer.toString(totalResolved);
			generalInfoStr += "\n" + "Recall = " + Double.toString((double) totalResolved / (double) totalRun);

			generalInfoStr += "\n" + "- By variable info -";
			generalInfoStr += "\n" + "nVariable = " + Integer.toString(totalVar);
			generalInfoStr += "\n" + "nResolvedVar = " + Integer.toString(totalResolvedVar);
			generalInfoStr += "\n" + "Recall = " + Double.toString((double) totalResolvedVar / (double) totalVar);

			generalInfoStr += "\n" + "Graph found = " + Integer.toString(totalGraphFound);
			generalInfoStr += "\n" + "Graph not found = " + Integer.toString(totalGraphNotFound);

		}
	}

	public static void main(String[] args) {
		System.out.println("Started BBS...");
		System.out.println(Runtime.getRuntime().availableProcessors());
		BBS bbs = new BBS();
		bbs.process();
		bbs.analysis(1, false);
		bbs.analysis(5, false);
		bbs.analysis(10, true);
//		bbs.analysis(TestingFunctionReadetruer.TOPK, true);
		bbs.writeOutInfo();
		System.out.println(bbs.generalInfoStr);
		System.out.println("Finished BBS");
	}
}
