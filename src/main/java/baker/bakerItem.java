package baker;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Harry Tran on 5/23/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class bakerItem {
	private int programEntityIndex;
	private int relationIndex;
	private ArrayList<Integer> nameCandidateIndex;

	public bakerItem(int programEntityIndex, int relationIndex, ArrayList<Integer> nameCandidate) {
		this.programEntityIndex = programEntityIndex;
		this.relationIndex = relationIndex;
		this.nameCandidateIndex = nameCandidate;

		Collections.sort(this.nameCandidateIndex);
		// this guarantees that when bakerItem is created, nameCandidateIndex is in the increasing order
	}

	public bakerItem() {
		 this.nameCandidateIndex = new ArrayList<Integer>();
	}

	public void cloneItem(bakerItem bakerItem) {
		setRelationIndex(bakerItem.getRelationIndex());
		setProgramEntityIndex(bakerItem.getProgramEntityIndex());
		setNameCandidateIndex(bakerItem.getNameCandidateIndex());
	}
	public bakerItem getIntersection(bakerItem bakerItem) {
		bakerItem res = new bakerItem();
		if (getRelationIndex() == bakerItem.getRelationIndex()) {
			res.setProgramEntityIndex(getProgramEntityIndex()); // the programEntityIndex intersection set is not necessary
			res.setRelationIndex(getRelationIndex());

			int i1 = 0, i2 = 0;
			while (i1 < getNameCandidateIndex().size() && i2 < bakerItem.getNameCandidateIndex().size()) {
				int id1 = getCandIndexById(i1);
				int id2 = bakerItem.getCandIndexById(i2);
				if (id1 == id2) {
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
