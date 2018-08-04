package association;

import java.util.HashMap;
import java.util.Objects;

/**
 * @author Harry Tran on 8/3/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class AssociationVarVar {
	HashMap<Integer, Integer> var2Hash = new HashMap<>();
	HashMap<Integer, Integer> var1Hash = new HashMap<>();

	private void addOneVar(String name) {
		int code = name.hashCode();
		var1Hash.put(code, var1Hash.getOrDefault(code, 0) + 1);
	}

	private void addOnePair(String name1, String name2, String rel) {
		int code = (name1 + name2 + rel).hashCode();
		var2Hash.put(code, var2Hash.getOrDefault(code, 0) + 1);
	}

	public void addInfo(String name1, String name2, String rel) {
		addOneVar(name1);
		addOneVar(name2);
		addOnePair(name1, name2, rel);

		if (Objects.equals(rel, "")) {
			addOnePair(name2, name1, rel);
		}
	}

	public String showInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("var1Hash size = ").append(var1Hash.size()).append("\n");
		sb.append("var2Hash size = ").append(var2Hash.size()).append("\n");
		return sb.toString();
	}

	public void mergeDataFrom(AssociationVarVar av) {
		for (int key : av.var1Hash.keySet())
			var1Hash.put(key, var1Hash.getOrDefault(key, 0) + av.var1Hash.get(key));
		for (int key : av.var2Hash.keySet())
			var2Hash.put(key, var2Hash.getOrDefault(key, 0) + av.var2Hash.get(key));
	}

	public double getAssocScore(String var1, String var2, String rel) {
		int n1 = var1Hash.getOrDefault(var1.hashCode(), 0);
		int n2 = var1Hash.getOrDefault(var2.hashCode(), 0);
		if (n1 * n2 == 0) return 0.0;
		int n1and2 = var2Hash.getOrDefault((var1 + var2 + rel).hashCode(), 0);
		double mau = n1 + n2 - n1and2;
		if (mau <= 0) return 0;
		return (double) n1and2 /  mau;
	}
}
