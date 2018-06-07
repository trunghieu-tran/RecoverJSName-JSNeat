package baker;

import dataCenter.InputReader;
import dataCenter.MainDataCenter;
import dataCenter.VarNameItem;
import dataCenter.utils.FileIO;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Harry Tran on 5/22/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class MainBaker {
	private static final int TOP_RESULT = 10;
	private static String inputDir = "./resources/parsedData/testingData/startServer/"; // Set by default
	private static String outputFile = inputDir + "prediction"; // Set by default

	private static MainDataCenter dc;
	private static InputReader ir;

	public MainBaker() {
		dc = new MainDataCenter();
		ir = new InputReader(inputDir);
	}

	public void process() {
		String result = "";
		HashSet<Integer> varNameSet = ir.getVarNameIDSet();
		for (int id: varNameSet) {
			result += Integer.toString(id) + " " + ir.getVarNameByID(id) + "\n";

			ArrayList<BakerItem> bakerItemList = new ArrayList<>();
			ArrayList<Pair<Integer, Integer> > peReList = ir.getListPeReByID(id);

			for (Pair<Integer, Integer> pp : peReList) {
				String tmpPe = ir.getPeByID(pp.getKey());
				String tmpRe = ir.getReByID(pp.getValue());
				int originalPeID = dc.getProgramEntityID(tmpPe);
				int originalReId = dc.getRelationID(tmpRe);

				BakerItem currBi = dc.getBakerItemFromPeRe(originalPeID, originalReId);
				bakerItemList.add(currBi);
			}
			BakerEngine be = new BakerEngine(id, bakerItemList);
			ArrayList<Integer> res = be.getFinalCandidateList();

			result += Integer.toString(res.size()) + "\n";
			if (res.size() > 0) {
				// Sort
				for (int i = 0; i < res.size(); ++i)
					for (int j = i + 1; j < res.size(); ++j)
						if (dc.getVarNameFreqencyByID(res.get(i)) < dc.getVarNameFreqencyByID(res.get(j))) {
							int temp = res.get(i);
							res.set(i, res.get(j));
							res.set(j, temp);
						}

				for (int i = 0; i < res.size(); ++i) {
					if (dc.getVarNameItemByID(res.get(i)).getValue().equals(ir.getVarNameByID(id))) {
						result += "FOUND" + "\n";
						break;
					}
				}
				//
				String ttt = "";
				for (int i = 0; i < Math.min(res.size(), TOP_RESULT); ++i) {
					int idx = res.get(i);
					VarNameItem vni = dc.getVarNameItemByID(idx);
					if (vni != null) ttt += vni.getValue() + " ";
				}
				result += ttt + "\n";
			}
			result += "\n";
		}
		FileIO.writeStringToFile(outputFile, result);
	}

	public static void main(String[] args) {
		System.out.println("=== Started Baker...");
		MainBaker baker = new MainBaker();
		baker.process();
		System.out.println("... Finished ===");
	}
}
