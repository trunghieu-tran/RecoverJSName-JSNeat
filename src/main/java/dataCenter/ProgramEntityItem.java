package dataCenter;

/**
 * @author Harry Tran on 5/28/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class ProgramEntityItem implements Item {
	private int id;
	private String value;
	private int frequency;

	public ProgramEntityItem(int id, String value, int frequency) {
		this.id = id;
		this.value = value;
		this.frequency = frequency;
	}

	public ProgramEntityItem() {
	}

	public int getId() {
		return id;
	}

	public String getValue() {
		return value;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
}
