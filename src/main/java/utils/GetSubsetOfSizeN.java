package utils;

import java.util.ArrayList;
import java.util.HashSet;
public class GetSubsetOfSizeN {
	private static void getSubsets(ArrayList<String> superSet, int k, int idx, HashSet<String> current,ArrayList<HashSet<String>> solution) {
		//successful stop clause
		if (current.size() == k) {
			solution.add(new HashSet<>(current));
			return;
		}
		//unsuccessful stop clause
		if (idx == superSet.size()) return;
		String x = superSet.get(idx);
		current.add(x);
		//"guess" x is in the subset
		getSubsets(superSet, k, idx+1, current, solution);
		current.remove(x);
		//"guess" x is not in the subset
		getSubsets(superSet, k, idx+1, current, solution);
	}

	public static ArrayList<HashSet<String>> getSubsets(ArrayList<String> superSet, int k) {
		ArrayList<HashSet<String>> res = new ArrayList<>();
		getSubsets(superSet, k, 0, new HashSet<String>(), res);
		return res;
	}
}