package comparison;

import javafx.util.Pair;
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
//	private static String originalTests = "/Users/tranhieu/Research_Projects/RecoverVarNameJS/program/RecoverJSName/resources/";
//	private static String recoveredTests = "/Users/tranhieu/Research_Projects/RecoverVarNameJS/program/RecoverJSName/resources/";

	public ArrayList<Pair<String, String>> filesPair = new ArrayList<>();

	private boolean isOkFile(String file) {
		try {
			String str = FileIO.readStringFromFile(file);
			return str.length() > 10;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}

	public JSNiceEvaluation() {
		Set<String> origins = FileIO.getAllFilesFromDirectory(originalTests);
		Set<String> recovered = FileIO.getAllFilesFromDirectory(recoveredTests);
		System.out.print("Num of origin = ");
		System.out.println(origins.size());
		System.out.print("Num of recovered = ");
		System.out.println(recovered.size());
		for (String str : origins)
			if (recovered.contains(str)) {
				if (isOkFile(originalTests + str) && isOkFile(recoveredTests + str))
					filesPair.add(new Pair<>(originalTests + str, recoveredTests + str));
			}
	}

	public static void main(String[] args) {
		JSNiceEvaluation je = new JSNiceEvaluation();
		System.out.print("Num Of recovered match = ");
		System.out.println(je.filesPair.size());
		StringBuilder sb = new StringBuilder();
		for (Pair<String, String> p : je.filesPair) {
			sb.append(p.getKey()).append(" ").append(p.getValue()).append("\n");
//			System.out.println(p.getKey() + " " + p.getValue());
		}
		FileIO.writeStringToFile(filesPairMatching, sb.toString());
	}
}
