package baker.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * @author Harry Tran on 5/23/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class commandLineRunner {
	private final static String pythonMainFile = "/Users/tranhieu/Research_Projects/RecoverVarNameJS/program/RecoverJSName/src/main/bashShell/runTopicModelMain.sh";
	private final static Logger LOGGER = Logger.getLogger(topicModelling.class.getName());

	public static void pythonTopicModelRunning(int option) {
		try {
//			String[] cmd = new String[]{"/bin/sh", "/Users/tranhieu/Research_Projects/RecoverVarNameJS/program/RecoverJSName/src/main/bashShell/runTopicModelMain.sh"};
//			Process p = Runtime.getRuntime().exec(cmd);
			Process p2 = new ProcessBuilder("bash " + pythonMainFile).start();
//			Process p2 = new ProcessBuilder("ls", "-lh").start();
//			p2.waitFor();
//			p.waitFor();
			Runtime.getRuntime().exec("ls");
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

	public static void runTopicModelPrediction() {
		commandLineRunner.pythonTopicModelRunning(2);
	}

	public static void db() throws Exception{
		ProcessBuilder builder = new ProcessBuilder();
		if (commandLineRunner.isWindows()) {
			builder.command("cmd.exe", "/c", "dir");
		} else {
			builder.command("sh", "-c", "ls");
		}
		builder.directory(new File(System.getProperty("user.home")));
		Process process = builder.start();
		StreamGobbler streamGobbler =
				new StreamGobbler(process.getInputStream(), System.out::println);
		Executors.newSingleThreadExecutor().submit(streamGobbler);
		int exitCode = process.waitFor();
		assert exitCode == 0;
	}

	public static void main(String[] args) {
		try {
			db();
		} catch (Exception e) {
			LOGGER.info(e.getMessage());
		}
	}
}
