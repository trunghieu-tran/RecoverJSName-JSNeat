package mainRecover;

import java.util.ArrayList;

/**
 * @author Harry Tran on 7/9/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class MainRecover {
	private static String data = "/home/nmt140230/RecoverJSName/StarGraphData";

	private ArrayList<FunctionInfo> functionList;

	public void loadInput() {
		// TODO - each test is represented as FunctionInfo, load all test into functionList
	}

	public void loadTrainingData() {

	}
	private void processOne(FunctionInfo fi) {
		System.out.println(">>> Processing function " + fi.getDir());
	}

	public void process() {
		for (FunctionInfo fi : functionList) {
			processOne(fi);
		}
	}

	public static void main(String[] args) {
		MainRecover mr = new MainRecover();
		mr.loadInput();
		mr.process();
	}
}
