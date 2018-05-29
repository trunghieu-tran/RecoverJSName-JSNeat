package core;


/**
 * @author Harry Tran on 5/28/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class StartEngine {

	private static void loadData() {

	}

	private static void loadInput() {

	}

	private static void runPrediction() {

	}

	public static void start() {
		System.out.println("Do something...");
		// Load data or training data
		loadData();
		// Load input
		loadInput();
		// Predict
			// predict one by one var.name
			// using predicted var.name information for the next predict
		runPrediction();
	}

	public static void main(String[] args) {
		System.out.println("=== System started ...");
		StartEngine.start();
		System.out.println("... Sucessfully!!!!! ===");
	}
}
