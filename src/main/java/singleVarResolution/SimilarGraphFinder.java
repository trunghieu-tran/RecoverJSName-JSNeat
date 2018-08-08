package singleVarResolution;

import javafx.util.Pair;
import utils.Constants;

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
	public HashMap<Integer, HashSet<StarGraph>> mapEdgeToGraphs;

	public SimilarGraphFinder(HashMap<Integer, HashSet<StarGraph>> mapEdgeToGraphs) {
		this.mapEdgeToGraphs = mapEdgeToGraphs;
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
		HashMap<String, Integer> mapVarNamevsFreq = new HashMap<>();

		for (Pair<StarGraph, Double> p : similarGraphs) {
			String varName = p.getKey().getVarName();

			if (Constants.usingFrequencyStargraph) {
				mapVarNamevsFreq.put(varName, mapVarNamevsFreq.getOrDefault(varName, 0) + 1);
			}
			else {
				if (mapVarNameVsScore.containsKey(varName)) {
					mapVarNameVsScore.get(varName).add(p.getValue());
				} else {
					ArrayList<Double> scList = new ArrayList<>();
					scList.add(p.getValue());
					mapVarNameVsScore.put(varName, scList);
				}
			}
		}

		if (Constants.usingFrequencyStargraph) {
			for (String varName : mapVarNamevsFreq.keySet()) {
				res.add(new Pair<>(varName, mapVarNamevsFreq.get(varName) * 1.0));
			}
		}
		else {
			for (String varName : mapVarNameVsScore.keySet()) {
				ArrayList<Double> scList = mapVarNameVsScore.get(varName);
				if (scList.size() == 0) continue;
				double finalScore = getScoreOfVarName(scList);
				res.add(new Pair<>(varName, finalScore));
			}
		}

		res.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

		return res;
	}

	private HashSet<StarGraph> getCandidateStarGraphForGraph(StarGraph g) {
		HashSet<StarGraph> tmp = new HashSet<>();
		for (Edge e : g.getEdges()) {
			if (mapEdgeToGraphs.containsKey(e.hashCode)) {
				tmp.addAll(mapEdgeToGraphs.get(e.hashCode));
			}
		}
		return tmp;
	}

	private HashSet<Pair<StarGraph, Double>> getSimilarStarGraphWith(StarGraph g) {
		HashSet<StarGraph> candSg = getCandidateStarGraphForGraph(g);
		HashSet<Pair<StarGraph, Double>> res = new HashSet<>();
		for (StarGraph sg : candSg) {
			double sc = g.getSimilarScoreTo(sg);
			if (sc == -1.0) continue;
			res.add(new Pair<>(sg, sc));
		}
		return res;
	}
}
