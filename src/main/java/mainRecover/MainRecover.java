package mainRecover;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import singleVarResolution.SGData;

/**
 * @author Harry Tran on 7/9/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class MainRecover {
	private static String InputData = "/home/nmt140230/RecoverJSName/StarGraphData/";
	private static SGData sgData = new SGData();
	private Set<FunctionInfo> functionList;
	public void loadInput() {
		try {
			sgData.getTestData(InputData);
			functionList = sgData.testFunctionSet;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadTrainingData() {
		sgData.getData(InputData, -1);
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
		mr.loadTrainingData();
		mr.loadInput();
		mr.process();
	}
}
