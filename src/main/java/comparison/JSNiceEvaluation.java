package comparison;

import javafx.util.Pair;
import parser.CheckJSNiceParser;
import utils.FileIO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Harry Tran on 7/23/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class JSNiceEvaluation {
	private static String originalTests = "/home/txt171930/JSNice/testSet/";
	private static String recoveredTests = "/home/txt171930/JSNice/recovered/";
	private static String filesPairMatching = "/home/txt171930/JSNice/filesPairMatching.txt";
	private static String outputJSNice = "/home/txt171930/JSNice/outputJSNice.txt";
	private static String resultJSNice = "/home/txt171930/JSNice/resultJSNice.txt";

	public ArrayList<Pair<String, String>> filesPair = new ArrayList<>();
	StringBuilder sbR = new StringBuilder();

	private boolean isOkFile(String file) {
		try {
			String str = FileIO.readStringFromFile(file);
			str = str.replaceAll("\n[ \t]*\n", "");
			return str.length() > 10;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}

	public JSNiceEvaluation() {
		Set<String> origins = FileIO.getAllFilesFromDirectory(originalTests);
		Set<String> recovered = FileIO.getAllFilesFromDirectory(recoveredTests);
		sbR.append("Num of origin = ").append(origins.size()).append("\n");
		sbR.append("Num of recovered = ").append(recovered.size()).append("\n");
		for (String str : origins)
			if (recovered.contains(str)) {
				if (isOkFile(originalTests + str) && isOkFile(recoveredTests + str))
					filesPair.add(new Pair<>(originalTests + str, recoveredTests + str));
			}
		sbR.append("Valid pair = ").append(filesPair.size()).append("\n");
	}

	public void process() {
		sbR.append("Num Of recovered match = ").append(filesPair.size()).append("\n");
		StringBuilder sb = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();

		int nVar = 0, nVarMatched = 0;
		int nErr = 0;

		for (Pair<String, String> p : filesPair) {
			System.out.print(">");
			sb.append(p.getKey()).append(" ").append(p.getValue()).append("\n");
			CheckJSNiceParser cp = new CheckJSNiceParser(p.getKey(), p.getValue());
			if (!cp.checkLength()) {
				nErr++;
				continue;
			}
			ArrayList<ArrayList<String>> allVar1 = cp.getAllVar1();
			ArrayList<ArrayList<String>> allVar2 = cp.getAllVar2();
			for (int i = 0; i < allVar1.size(); ++i)
				for (int j = 0; j < allVar1.get(i).size(); ++j) {
					String s1 = allVar1.get(i).get(j);
					String s2 = allVar2.get(i).get(j);
					++nVar;
					if (s1.equals(s2)) ++nVarMatched;
					sb2.append(s1).append(" ").append(s2).append("\n");
				}
		}

		sbR.append("Num of unmatched size = ").append(nErr).append("\n");
		sbR.append("Num of var = " ).append(nVar).append("\n");
		sbR.append("Num of var matched = ").append(nVarMatched).append("\n");
		sbR.append("Accuracy = ").append((double) nVarMatched / nVar).append("\n");

		FileIO.writeStringToFile(resultJSNice, sbR.toString());
		FileIO.writeStringToFile(filesPairMatching, sb.toString());
		FileIO.writeStringToFile(outputJSNice, sb2.toString());
	}
	public static void main(String[] args) {
		JSNiceEvaluation je = new JSNiceEvaluation();
		je.process();
	}
}
