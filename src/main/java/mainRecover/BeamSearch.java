package mainRecover;

import javafx.util.Pair;

import java.util.*;

/**
 * @author Harry Tran on 7/9/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class BeamSearch {
	private static final String UNRESOLVED = "UNRESOLVED";
	private ArrayList< ArrayList<Pair<String, Double>> > candidateLists;
	private int numOfVar;
	private HashSet<Integer> marked = new HashSet<>();
	private ArrayList<ArrayList<String>> currRecovering = new ArrayList<>();
	private ArrayList<Integer> orderRecovering = new ArrayList<>();
	private HashMap<Pair<Integer, String>, Double> mapVarNamevsScore = new HashMap<>();

	public BeamSearch(ArrayList<ArrayList<Pair<String, Double>>> candidateLists) {
		this.candidateLists = candidateLists;
		this.numOfVar = candidateLists.size();
		updateMapVarNamevsScore();
	}

	private void updateMapVarNamevsScore() {
		for (int i = 0; i < numOfVar; ++i) {
			for (Pair<String, Double> p : candidateLists.get(i)) {
				mapVarNamevsScore.put(new Pair<>(i, p.getKey()), p.getValue());
			}
		}
	}

	private double getVarNameScore(int idx, String name) {
		return mapVarNamevsScore.getOrDefault(new Pair<>(idx, name), 0.0);
	}

	private int getNextResolveID() {
		int res = -1;
		double currBest = 0;
		for (int i = 0; i < numOfVar; ++i)
			if (!marked.contains(i) && candidateLists.get(i).size() > 0) {
				double curr = candidateLists.get(i).get(0).getValue();
				if (res == -1 || currBest < curr) {
					res = i;
					currBest = curr;
				}
			}
		return res;
	}

	private double getScoreTogether(ArrayList<String> setName) {
		// TODO
		return 0.5;
	}

	private double getConfidentScore(ArrayList<Double> similarScores, double scoreTogether) {
		// TODO
		return 0.5;
	}

	private void recovering(int idx, int K) {
		ArrayList< Pair<ArrayList<String>, Double>> allPosssibleRecover = new ArrayList<>();
		ArrayList<Pair<String, Double>> candI = candidateLists.get(idx);

		for (int i = 0; i < currRecovering.size(); ++i) {
			ArrayList<Double> setScore = new ArrayList<>();
			ArrayList<String> recovered = currRecovering.get(i);

			for (int j = 0; j < recovered.size(); ++j) {
				setScore.add(getVarNameScore(orderRecovering.get(j), recovered.get(j)));
			}

			for (Pair<String, Double> p : candI) {
				ArrayList<String> setNameTmp = new ArrayList<>(recovered);
				setNameTmp.add(p.getKey());
				ArrayList<Double> setScoreTmp = new ArrayList<>(setScore);
				setScoreTmp.add(p.getValue());

				double pTogether = getScoreTogether(setNameTmp);
				double sc = getConfidentScore(setScoreTmp, pTogether);
				allPosssibleRecover.add(new Pair<>(setNameTmp, sc));
			}
		}
		allPosssibleRecover.sort(new Comparator<Pair<ArrayList<String>, Double>>() {
			@Override
			public int compare(Pair<ArrayList<String>, Double> o1, Pair<ArrayList<String>, Double> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		currRecovering.clear();
		for (int i = 0; i < Math.min(K, allPosssibleRecover.size()); ++i) {
			currRecovering.add(allPosssibleRecover.get(i).getKey());
		}
	}

	private void initTheFirstRecover(int K) {
		int id = getNextResolveID();
		if (id == -1) return;

		orderRecovering.add(id);
		marked.add(id);

		ArrayList<Pair<String, Double>> candI = candidateLists.get(id);
		for (int i = 0; i < Math.min(K, candI.size()); ++i) {
			ArrayList<String> recovered = new ArrayList<>();
			recovered.add(candI.get(i).getKey());
			currRecovering.add(recovered);
		}
	}

	private void reverseToOriginalOrder() {
		for (int i = 0; i < numOfVar; ++i)
			for (int j = i + 1; j < numOfVar; ++j) {
				int ii = orderRecovering.get(i);
				int jj = orderRecovering.get(j);
				if (ii <= jj) continue;
				for (int kk = 0; kk < currRecovering.size(); ++kk) {
					String tmpStr = currRecovering.get(kk).get(i);
					currRecovering.get(kk).set(i, currRecovering.get(kk).get(j));
					currRecovering.get(kk).set(j, tmpStr);
				}
				orderRecovering.set(i, jj);
				orderRecovering.set(j, ii);
			}
	}

	public ArrayList< ArrayList<String>> getTopKRecoveringResult(int K) {
		// first recover
		initTheFirstRecover(K);

		// recover the rest
		int id = getNextResolveID();
		while (id != -1) {
			orderRecovering.add(id);
			marked.add(id);
			recovering(id, K);
		}

		// Add UNRESOLVED
		for (int i = 0; i < numOfVar; ++i) {
			if (!marked.contains(i)) {
				for (int j = 0; j < currRecovering.size(); ++j)
					currRecovering.get(j).add(UNRESOLVED);
				orderRecovering.add(i);
			}
		}

		reverseToOriginalOrder();

		return currRecovering;
	}
}
