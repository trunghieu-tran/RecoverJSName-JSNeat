package mainRecover;

import java.io.IOException;
import java.util.ArrayList;

import singleVarResolution.SGData;

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
		SGData sgData = new SGData();
		try {
			functionList = sgData.getTestData("../StarGraphTestData");
		} catch (IOException e) {
			e.printStackTrace();
		}
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
