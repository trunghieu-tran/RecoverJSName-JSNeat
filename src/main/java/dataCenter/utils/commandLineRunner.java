package dataCenter.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static java.lang.System.exit;

/**
 * @author Harry Tran on 5/23/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class commandLineRunner {
	private final static String bashShellDir = "./src/main/bashShell/";
	private final static String pythonMainFile = "runTopicModelMain.sh";
	private final static String pythonTMTrainingFile = "runTopicModelTraining.sh";
	private final static String pythonTMPredictionFile = "runTopicModelPredictionByFile.sh";
	private final static Logger LOGGER = Logger.getLogger(topicModelling.class.getName());

	public static void pythonTopicModelTraining(){
		try {
			ProcessBuilder builder = new ProcessBuilder();
			if (commandLineRunner.isWindows()) {
				builder.command("cmd.exe", "/c", "dir"); // I'm not sure to run this command in Windows =))
			} else {
				builder.command("bash", pythonTMTrainingFile);
			}
			builder.directory(new File(bashShellDir));

			LOGGER.info(builder.directory().getAbsolutePath());
			LOGGER.info(builder.command().toString());

			Process process = builder.start();
			StreamGobbler streamGobbler =
					new StreamGobbler(process.getInputStream(), System.out::println);
			Executors.newSingleThreadExecutor().submit(streamGobbler);
			int exitCode = process.waitFor();
			assert exitCode == 0;
			Thread.sleep(3000);
		} catch (Exception e) {
			LOGGER.info(e.getMessage());
		}
	}

	private static class StreamGobbler implements Runnable {
		private InputStream inputStream;
		private Consumer<String> consumer;

		public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
			this.inputStream = inputStream;
			this.consumer = consumer;
		}

		@Override
		public void run() {
			new BufferedReader(new InputStreamReader(inputStream)).lines()
					.forEach(consumer);
		}
	}

	public static boolean isWindows() {
		return System.getProperty("os.name")
				.toLowerCase().startsWith("windows");
	}


	public static void runTopicModelPrediction() throws Exception{
		try {
			ProcessBuilder builder = new ProcessBuilder();
			if (commandLineRunner.isWindows()) {
				builder.command("cmd.exe", "/c", "dir"); // I'm not sure to run this command in Windows =))
			} else {
				builder.command("bash", pythonMainFile);
			}
			builder.directory(new File(bashShellDir));

			LOGGER.info(builder.directory().getAbsolutePath());

			Process process = builder.start();
			StreamGobbler streamGobbler =
					new StreamGobbler(process.getInputStream(), System.out::println);
			Executors.newSingleThreadExecutor().submit(streamGobbler);
			int exitCode = process.waitFor();
			assert exitCode == 0;
			try {
				Thread.sleep(3000);
			} catch (Exception e) {
				LOGGER.info(e.getMessage());
			}
			process.destroy();
		} catch (Exception e) {
			LOGGER.info(e.getMessage());
		}
	}

	private static String getAbsolutePathFrom(String path) {
		File f = new File(path);
		return f.getAbsolutePath();
	}

	public static void runTopicModelPredictionByFile(String inputFile, String outputFile){
		try {
			ProcessBuilder builder = new ProcessBuilder();
			if (commandLineRunner.isWindows()) {
				builder.command("cmd.exe", "/c", "dir"); // I'm not sure to run this command in Windows =))
			} else {
				builder.command("bash", pythonTMPredictionFile, getAbsolutePathFrom(inputFile), getAbsolutePathFrom(outputFile));
			}
			builder.directory(new File(bashShellDir));

//			LOGGER.info(builder.directory().getAbsolutePath());
//			LOGGER.info(builder.command().toString());

			Process process = builder.start();
			StreamGobbler streamGobbler =
					new StreamGobbler(process.getInputStream(), System.out::println);
			Executors.newSingleThreadExecutor().submit(streamGobbler);
			int exitCode = process.waitFor();
			assert exitCode == 0;
			try {
				Thread.sleep(50);
			} catch (Exception e) {
				LOGGER.info(e.getMessage());
			}
			process.destroyForcibly();
		} catch (Exception e) {
			LOGGER.info(e.getMessage());
		}
	}

	public static void main(String[] args) {
		try {
//			pythonTopicModelTraining();
//			runTopicModelPrediction();
			String in = "./resources/parsedData/testingData/zzo_server_/topicModel.txt";
			String out = "./resources/parsedData/testingData/zzo_server_/topicDistribution.txt";
			runTopicModelPredictionByFile(in, out);
//			commandLineRunner.pythonTopicModelTraining();
		} catch (Exception e) {
			LOGGER.info(e.getMessage());
		}
		exit(0);
	}
}
