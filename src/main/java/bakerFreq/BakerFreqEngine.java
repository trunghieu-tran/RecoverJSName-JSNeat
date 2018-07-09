package bakerFreq;

import javafx.util.Pair;

import java.util.*;

/**
 * @author Harry Tran on 6/6/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class BakerFreqEngine {
	private int idVarName;
	private ArrayList<BakerFreqItem> bakerFreqItemList = new ArrayList<>();

	public BakerFreqEngine(int idVarName, ArrayList<BakerFreqItem> bakerFreqItemList) {
		this.idVarName = idVarName;
		this.bakerFreqItemList = bakerFreqItemList;
	}

	public ArrayList<Pair<Integer, Double>> getFinalCandidateList() {
		ArrayList<Pair<Integer, Double>> res = new ArrayList<>();
		HashSet<Integer> varNameIDSet = new HashSet<>();
		for (BakerFreqItem bi: bakerFreqItemList) {
			if (bi != null && bi.SetVarNameIDisNotNull()) {
				varNameIDSet.addAll(bi.getSetVarNameID());
			}
		}

		for (int idx : varNameIDSet) {
			double prob = 1;

			for (BakerFreqItem bi: bakerFreqItemList) {
				if (bi != null && bi.SetVarNameIDisNotNull()) {
					if (!bi.containsVarNameID(idx)) {
						prob = 0;
						break;
					} else {
						prob *= bi.getProbabilityofVarNameID(idx);
					}
				}
			}

			if (prob > 0) {
				res.add(new Pair<>(idx, prob));
			}
		}

		res.sort((o1, o2) -> {
			if (o1.getValue() < o2.getValue()) return 1;
			else if (o1.getValue() > o2.getValue()) return -1;
			else return 0;
		});

		return res;
	}
}
