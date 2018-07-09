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
	long hashCode;

	public Edge(String pe, String rel, int freq) {
		this.pe = pe;
		this.rel = rel;
		this.freq = freq;
		hashCode = (long)pe.hashCode() * (long)rel.hashCode();
	}

	public boolean isEquals(Edge e) {
		return this.hashCode == e.hashCode;
	}
	
	@Override
	public String toString() {
		return pe + " " + rel + " " + freq;
	}
}
