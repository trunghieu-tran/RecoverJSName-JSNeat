package singleVarResolution;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Harry Tran on 7/8/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class SimilarGraphFinder {
	private HashSet<StarGraph> sgSet;

	public SimilarGraphFinder(HashSet<StarGraph> sgSet) {
		this.sgSet = sgSet;
	}

	public ArrayList<Pair<String, Double>> getCandidateListForStarGraph(StarGraph g) {
		ArrayList<Pair<String, Double>> res = new ArrayList<>();
		HashSet<Pair<StarGraph, Double>> similarGraphs = getSimilarStarGraphWith(g);
		HashMap<String, ArrayList<Double>> mapVarNameVsScore = new HashMap<>();

		for (Pair<StarGraph, Double> p : similarGraphs) {
			String varName = p.getKey().getVarName();
			if (mapVarNameVsScore.containsKey(varName)){
				mapVarNameVsScore.get(varName).add(p.getValue());
			} else {
				ArrayList<Double> scList = new ArrayList<>();
				scList.add(p.getValue());
				mapVarNameVsScore.put(varName, scList);
			}
		}

		for (String varName : mapVarNameVsScore.keySet()) {
			ArrayList<Double> scList = mapVarNameVsScore.getOrDefault(varName, new ArrayList<>());
			if (scList.size() == 0) continue;

			double sum = 0;
			boolean hasOne = false;
			for (Double d : scList) {
				sum += d;
				hasOne |= (d == 1.0);
				if (hasOne) break;
			}
			double finalScore = hasOne ? 1.0 : sum / scList.size();
			res.add(new Pair<>(varName, finalScore));
		}
		return res;
	}

	private HashSet<Pair<StarGraph, Double>> getSimilarStarGraphWith(StarGraph g) {
		HashSet<Pair<StarGraph, Double>> res = new HashSet<>();
		for (StarGraph sg : sgSet) {
			double sc = g.getSimilarScoreTo(sg);
			if (sc == -1.0) continue;
			res.add(new Pair<>(sg, sc));
		}
		return res;
	}
}
