package dataCenter;

import java.util.Comparator;

/**
 * @author Harry Tran on 5/28/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class PeReItem {
	private int id;
	private int peId;
	private int reId;

	public PeReItem(int id, int peId, int reId) {
		this.id = id;
		this.peId = peId;
		this.reId = reId;
	}

	public int getId() {
		return id;
	}

	public int getPeId() {
		return peId;
	}

	public int getReId() {
		return reId;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setPeId(int peId) {
		this.peId = peId;
	}

	public void setReId(int reId) {
		this.reId = reId;
	}

	public static Comparator<PeReItem> PeReItemCompatator = new Comparator<PeReItem>() {
		@Override
		public int compare(PeReItem p1, PeReItem p2) {
			if (p1.getPeId() < p2.getPeId()) return 1;
			if (p1.getPeId() > p2.getPeId()) return 0;

			if (p1.getReId() < p2.getPeId()) return 1;
				else return 0;
		}
	};

	@Override
	public String toString() {
		return Integer.toString(id) + " " + Integer.toString(peId) + " " + Integer.toString(reId);
	}
}
