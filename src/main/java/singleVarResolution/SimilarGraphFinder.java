package singleVarResolution;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;
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

	private double getScoreOfVarName(ArrayList<Double> scList) {
		double sum = 0;
		for (Double d : scList) {
			sum += d;
		}
		return sum / scList.size();
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
			double finalScore = getScoreOfVarName(scList);
			res.add(new Pair<>(varName, finalScore));
		}

		res.sort(new Comparator<Pair<String, Double>>() {
			@Override
			public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});

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
