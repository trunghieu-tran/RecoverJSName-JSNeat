package singleVarResolution;

/**
 * @author Harry Tran on 7/8/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class Edge {
	private String pe;
	private String rel;
	int freq = 0;
	int hashCode;

	public Edge(String pe, String rel, int freq) {
		this.pe = pe;
		this.rel = rel;
		this.freq = freq;
		long tmp = ((long) pe.hashCode() + rel.hashCode()) % Integer.MAX_VALUE;
		hashCode = (int) tmp;
	}

	public boolean isEquals(Edge e) {
		return this.hashCode == e.hashCode;
	}

	public String getRel() {
		return rel;
	}

	public String getPe() {
		return pe;
	}

	@Override
	public String toString() {
		return pe + " " + rel + " " + freq;
	}
}
