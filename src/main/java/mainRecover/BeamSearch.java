package mainRecover;

import association.AssociationCalculator;
import association.AssociationMiner;
import association.AssociationVarVar;
import association.TokenAssociationCalculator;
import javafx.util.Pair;
import utils.Constants;
import utils.FileIO;
import utils.Normalization;

import java.io.IOException;
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
	//
	private ArrayList<ArrayList<String>> currRecovering = new ArrayList<>();
	// a1 b1..
	// a2 b2
	private ArrayList<Integer> orderRecovering = new ArrayList<>();
	// 2 3 1 4
	private HashMap<Pair<Integer, String>, Double> mapVarNamevsScore = new HashMap<>();
	// a, a1 -> score
	private AssociationVarVar ac;
	private TokenAssociationCalculator tokAc;
	private HashMap<Pair<String, String>, Double> cache_Association = new HashMap<>();

	private HashMap<Pair<String, String>, Double> allAssociation = new HashMap<>();


	public long totalAss = 0;
	public long totalAssCounted = 0;
	private String fileOutAssociation;

	public BeamSearch(ArrayList< ArrayList<Pair<String, Double>> > candidateLists, AssociationVarVar ac, TokenAssociationCalculator tokAc, String file) {
		this.candidateLists = candidateLists;
		this.numOfVar = candidateLists.size();
		this.ac = ac;
		this.tokAc = tokAc;
		this.fileOutAssociation = file;
		updateMapVarNamevsScore();
		initializeAllAssociation();
	}

	private void initializeAllAssociation() {
		ArrayList<Double> vals = new ArrayList<>();
		for (int i = 0; i < candidateLists.size(); ++i)
			for (int j = i + 1; j < candidateLists.size(); ++j) {
				for (Pair<String, Double> p : candidateLists.get(i))
					for (Pair<String, Double> p2 : candidateLists.get(j)) {
						String key1 = p.getKey();
						String key2 = p2.getKey();

						Pair<String, String> tmp1 = new Pair<>(key1, key2);
						Pair<String, String> tmp2 = new Pair<>(key2, key1);
						double as = 0;
						if ( Constants.usingTokenizedVarName) {
							as = tokAc.getAssocScore(key1, key2, "");
						} else {
							as = ac.getAssocScore(key1, key2, "");
						}
						if (as > 0) {
							vals.add(as);
							allAssociation.put(tmp1, as);
							allAssociation.put(tmp2, as);
						}
					}
			}

		HashMap<Double, Double> mapSc = Normalization.normalizeMap(vals);
		for (Pair<String, String> p : allAssociation.keySet()) {
			double oldSc = allAssociation.get(p);
			if (mapSc.containsKey(oldSc))
				allAssociation.put(p, mapSc.get(oldSc));
		}
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
		int len = setName.size();
		if (len <= 1) return 0;

		double sum = 0;
		double res = 0;
		for (int i = 0; i < len; ++i)
			for (int j = i + 1; j < len; ++j) {
				String rel = "" ; // TODO - find rel based on their indexers
				double tmp = 0.0;

				if (Constants.usingNormalizationAllPair)
					tmp = allAssociation.getOrDefault(new Pair<>(setName.get(i), setName.get(j)), 0.0);
				else {
					if ( Constants.usingTokenizedVarName) {
						tmp = tokAc.getAssocScore(setName.get(i), setName.get(j), rel);
					} else {
						tmp = ac.getAssocScore(setName.get(i), setName.get(j), rel);
					}
				}
				res = Math.max(res, tmp);
				cache_Association.put(new Pair<>(setName.get(i), setName.get(j)), tmp);
				sum += tmp;
				totalAss++;
				if (tmp > 0) totalAssCounted++;
			}
		res = sum / (len * (len - 1) / 2);
		return res;
	}

	private double getConfidentScore(ArrayList<Double> similarScores, double scoreTogether) {
		if (similarScores.size() == 0) return 0;
		double sum = 0;
		for (double d : similarScores) sum += d;
		sum /= similarScores.size();
		return sum + scoreTogether;
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


			ArrayList<Double> tmpPTogether = new ArrayList<>();
			ArrayList<ArrayList<String>> tmpSetNameTmp = new ArrayList<>();

			for (Pair<String, Double> p : candI) {
				ArrayList<String> setNameTmp = new ArrayList<>(recovered);
				setNameTmp.add(p.getKey());
				double pTogether = getScoreTogether(setNameTmp);
				tmpPTogether.add(pTogether);
				tmpSetNameTmp.add(setNameTmp);
			}

			int ii = 0;

			if (!Constants.usingNormalizationAllPair)
				Normalization.normalize(tmpPTogether);

			for (Pair<String, Double> p : candI) {

				ArrayList<Double> setScoreTmp = new ArrayList<>(setScore);
				setScoreTmp.add(p.getValue());

				double pTogether = tmpPTogether.get(ii);
				double sc = pTogether;

				if (!Constants.usingOnlyVarVarOnBS)
					 sc = getConfidentScore(setScoreTmp, pTogether);

				allPosssibleRecover.add(new Pair<>(tmpSetNameTmp.get(ii), sc));
				++ii;
			}
		}
		allPosssibleRecover.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

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
					ArrayList<String> currRow = currRecovering.get(kk);
					String tmpStr = currRow.get(i);
					currRow.set(i, currRow.get(j));
					currRow.set(j, tmpStr);
				}
				orderRecovering.set(i, jj);
				orderRecovering.set(j, ii);
			}
	}

	private void writeOutCacheAssociation() {
		StringBuilder sb = new StringBuilder();
		for (Pair<String, String> key : cache_Association.keySet()) {
			sb.append(key.getKey()).append(" ").append(key.getValue()).append(" ").append(cache_Association.get(key)).append("\n");
		}
		FileIO.writeStringToFile(this.fileOutAssociation, sb.toString());
	}

	public ArrayList< ArrayList<String>> getTopKRecoveringResult(int K) {

		initTheFirstRecover(K);

		// recover the rest
		int id = getNextResolveID();
		while (id != -1) {
			orderRecovering.add(id);
			marked.add(id);
			recovering(id, K);
			id = getNextResolveID();
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
		writeOutCacheAssociation();

		return currRecovering;
	}
}
