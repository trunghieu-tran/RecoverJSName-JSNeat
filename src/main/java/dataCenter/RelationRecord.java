package dataCenter;

/**
 * @author Harry Tran on 5/28/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class RelationRecord implements Item {
	private int idVarName;
	private int idProgramEntity;
	private int idRelation;
	private int frequency;

	public RelationRecord(int idVarName, int idProgramEntity, int idRelation, int frequency) {
		this.idVarName = idVarName;
		this.idProgramEntity = idProgramEntity;
		this.idRelation = idRelation;
		this.frequency = frequency;
	}

	public RelationRecord() {
	}

	public int getIdVarName() {
		return idVarName;
	}

	public int getIdProgramEntity() {
		return idProgramEntity;
	}

	public int getIdRelation() {
		return idRelation;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setIdVarName(int idVarName) {
		this.idVarName = idVarName;
	}

	public void setIdProgramEntity(int idProgramEntity) {
		this.idProgramEntity = idProgramEntity;
	}

	public void setIdRelation(int idRelation) {
		this.idRelation = idRelation;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
}
