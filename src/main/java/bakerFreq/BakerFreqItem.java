package bakerFreq;

import baker.BakerItem;
import javafx.util.Pair;

import java.util.*;

/**
 * @author Harry Tran on 6/6/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class BakerFreqItem {
	private int programEntityIndex;
	private int relationIndex;
	private int sumFreq;
	private ArrayList<Pair<Integer, Integer>> nameCandidateIndexvsFreq;
	private HashMap<Integer, Double> mapVarNamevsProb;
	private HashSet<Integer> setVarNameID;

	public BakerFreqItem(int programEntityIndex, int relationIndex, ArrayList<Pair<Integer, Integer>> nameCandidateIndexvsFreq) {
		this.programEntityIndex = programEntityIndex;
		this.relationIndex = relationIndex;
		this.nameCandidateIndexvsFreq = nameCandidateIndexvsFreq;
		this.sumFreq = this.getSumFreqency();
		this.setVarNameID = new HashSet<>();

		this.mapVarNamevsProb = new HashMap<>();
		for (Pair<Integer, Integer> p : nameCandidateIndexvsFreq) {
			mapVarNamevsProb.put(p.getKey(), (double) p.getValue()/sumFreq);
			setVarNameID.add(p.getKey());
		}
	}

	public HashSet<Integer> getSetVarNameID() {
		return setVarNameID;
	}

	public boolean SetVarNameIDisNotNull() {
		return setVarNameID != null;
	}

	public boolean containsVarNameID(int idx) {
		return mapVarNamevsProb.containsKey(idx);
	}

	public double getProbabilityofVarNameID(int idx) {
		return mapVarNamevsProb.get(idx);
	}

	private int getSumFreqency() {
		int sum = 0;
		for (Pair<Integer, Integer> p: nameCandidateIndexvsFreq) {
			sum += p.getValue();
		}
		return sum;
	}
}
