package baker;

import java.util.ArrayList;

/**
 * @author Harry Tran on 5/23/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class bakerEngine {
	private int idVarName;
	private ArrayList<bakerItem> bakerItemList;

	public bakerEngine(int idVarName, ArrayList<bakerItem> bakerItemList) {
		this.idVarName = idVarName;
		this.bakerItemList = bakerItemList;
	}

	public ArrayList<bakerItem> getBakerItemList() {
		return bakerItemList;
	}

	public void setBakerItemList(ArrayList<bakerItem> bakerItemList) {
		this.bakerItemList = new ArrayList<>(bakerItemList);
	}

	public bakerItem runIntersect() {
		bakerItem curr = new bakerItem();
		if (getBakerItemList().size() > 0) {
			curr = getBakerItemList().get(0);
			for (int i = 1; i < getBakerItemList().size(); ++i) {
				curr = curr.getIntersection(getBakerItemList().get(i));
			}
		}
		return curr;
	}
}
