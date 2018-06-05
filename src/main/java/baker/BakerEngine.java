package baker;

import java.util.ArrayList;

/**
 * @author Harry Tran on 5/23/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class BakerEngine {
	private int idVarName;
	private ArrayList<BakerItem> bakerItemList;

	public BakerEngine(int idVarName, ArrayList<BakerItem> bakerItemList) {
		this.idVarName = idVarName;
		this.bakerItemList = bakerItemList;
	}

	public ArrayList<BakerItem> getBakerItemList() {
		return bakerItemList;
	}

	public void setBakerItemList(ArrayList<BakerItem> bakerItemList) {
		this.bakerItemList = new ArrayList<>(bakerItemList);
	}

	private BakerItem runIntersect() {
		BakerItem curr = new BakerItem();
		if (getBakerItemList().size() > 0) {
			curr = getBakerItemList().get(0);
			for (int i = 1; i < getBakerItemList().size(); ++i) {
				BakerItem last = curr;
				curr = curr.getIntersection(getBakerItemList().get(i));
				if (curr.hasAnEmptyCandidateList()) {
					curr = last;
				}
			}
		}
		return curr;
	}

	public ArrayList<Integer> getFinalCandidateList() {
		BakerItem bi = this.runIntersect();
		return bi.getNameCandidateIndex();
	}
}
