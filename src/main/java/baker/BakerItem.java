package baker;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Harry Tran on 5/23/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class BakerItem {
	private int programEntityIndex;
	private int relationIndex;
	private ArrayList<Integer> nameCandidateIndex;

	public BakerItem(int programEntityIndex, int relationIndex, ArrayList<Integer> nameCandidate) {
		this.programEntityIndex = programEntityIndex;
		this.relationIndex = relationIndex;
		this.nameCandidateIndex = nameCandidate;

		Collections.sort(this.nameCandidateIndex);
		// this guarantees that when BakerItem is created, nameCandidateIndex is in the increasing order
	}

	public BakerItem() {
		 this.nameCandidateIndex = new ArrayList<Integer>();
	}

	public BakerItem getIntersection(BakerItem BakerItem) {
		BakerItem res = new BakerItem();
		if (getRelationIndex() == BakerItem.getRelationIndex()) {
			res.setProgramEntityIndex(getProgramEntityIndex()); // the programEntityIndex intersection set is not necessary
			res.setRelationIndex(getRelationIndex());

			int i1 = 0, i2 = 0;
			while (i1 < getNameCandidateIndex().size() && i2 < BakerItem.getNameCandidateIndex().size()) {
				int id1 = getCandIndexById(i1);
				int id2 = BakerItem.getCandIndexById(i2);
				if (id1 == id2 && id1 != -1) {
					res.addOneCandIndex(id1);
					i1++;
					i2++;
				} else if (id1 > id2) {
					i2++;
				} else {
					i1++;
				}
			}
		}
		return res;
	}

	private void addOneCandIndex(int id) {
		this.nameCandidateIndex.add(id);
	}

	public int getCandIndexById(int id) {
		if (0 < id && id < getNameCandidateIndex().size()) {
			return getNameCandidateIndex().get(id);
		}
		else {
			return -1;
		}
	}

	public boolean hasAnEmptyCandidateList() {
		return nameCandidateIndex.size() == 0;
	}

	public int getProgramEntityIndex() {
		return programEntityIndex;
	}

	public void setProgramEntityIndex(int programEntityIndex) {
		this.programEntityIndex = programEntityIndex;
	}

	public int getRelationIndex() {
		return relationIndex;
	}

	public void setRelationIndex(int relationIndex) {
		this.relationIndex = relationIndex;
	}

	public ArrayList<Integer> getNameCandidateIndex() {
		return nameCandidateIndex;
	}

	public void setNameCandidateIndex(ArrayList<Integer> nameCandidateIndex) {
		this.nameCandidateIndex = new ArrayList<>(nameCandidateIndex);
	}
}
