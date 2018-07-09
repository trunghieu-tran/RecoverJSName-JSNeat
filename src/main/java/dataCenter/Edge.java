package dataCenter;

/**
 * @author Harry Tran on 6/16/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class Edge {
	private static final int base = 127;
	private static final int MOD = 1000000007;
	private String first;
	private String second;
	private String rel;
	private long hashCode;

	public Edge(String first, String second, String rel) {
		this.first = first.toLowerCase();
		this.second = second.toLowerCase();
		this.rel = rel.toLowerCase();
		hashCode = genHashCode();
	}

	public String getVarName() {
		return this.first;
	}


	public String getObserved() {
		return second;
	}

	public String getRel() {
		return rel;
	}

	public long getHashCode() {
		return hashCode;
	}

	private long genHashCode() {
		long res = 1;
		for (int i = 0; i < first.length(); ++i)
			res = (res * base + (long) first.charAt(i)) % MOD;
		for (int i = 0; i < second.length(); ++i)
			res = (res * base + (long) second.charAt(i)) % MOD;
		for (int i = 0; i < rel.length(); ++i)
			res = (res * base + (long) rel.charAt(i)) % MOD;
		return res;
	}
}
