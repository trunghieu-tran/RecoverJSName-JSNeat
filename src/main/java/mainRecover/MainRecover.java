package mainRecover;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import javafx.util.Pair;
import singleVarResolution.MainSingleVarResolution;
import singleVarResolution.SGData;
import singleVarResolution.SimilarGraphFinder;
import singleVarResolution.StarGraph;

/**
 * @author Harry Tran on 7/9/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class MainRecover {
	private static String InputData = "/home/nmt140230/RecoverJSName/StarGraphTestData/";
	private static SGData sgData = new SGData();
	private Set<FunctionInfo> functionList;
	private SimilarGraphFinder sf;
	int cnt = 0;
	public void loadInput() {
		try {
			sgData.getTestData(InputData);
			functionList = sgData.testFunctionSet;
			System.out.println(">>> The number of loaded function for testing = " + Integer.toString(functionList.size()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadTrainingData() {
		sgData.getData(InputData, 100000);
	}

	private void processOne(FunctionInfo fi) {
		StringBuilder tmp = new StringBuilder();
		tmp.append("Num of Var = ").append(fi.getStarGraphsList().size()).append("\n");
		for (StarGraph sg : fi.getStarGraphsList()) {
			ArrayList<Pair<String, Double>> res = sf.getCandidateListForStarGraph(sg);
			tmp.append(res.size()).append(" ");
		}
		tmp.append("\n");
		if (++cnt % 100 == 0) {
			System.out.println(">>> Processing function " + fi.getDir());
			System.out.println(tmp.toString());
		}
	}

	public void process() {
		sf = new SimilarGraphFinder(sgData.sgSet);
		for (FunctionInfo fi : functionList) {
			processOne(fi);
			if (cnt == 1000) break;
		}
	}

	public static void main(String[] args) {
		MainRecover mr = new MainRecover();
		mr.loadTrainingData();
		mr.loadInput();
		mr.process();
	}
}
