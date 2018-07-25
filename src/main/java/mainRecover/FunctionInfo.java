package mainRecover;

import singleVarResolution.StarGraph;

import java.util.HashSet;

/**
 * @author Harry Tran on 7/9/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class FunctionInfo {
	private HashSet<StarGraph> starGraphsList;
	private HashSet<String> varVarRels;
	private String dir;
	private String funcName;

	public FunctionInfo(HashSet<StarGraph> starGraphsList, String dir) {
		this.starGraphsList = starGraphsList;
		this.dir = dir;
	}
	public FunctionInfo(String dir) {
		this.starGraphsList = new HashSet<>();
		this.dir = dir;
	}

	public HashSet<StarGraph> getStarGraphsList() {
		return starGraphsList;
	}


	public String getFuncName() {
		return funcName;
	}

	public void setFuncName(String funcName) {
		this.funcName = funcName;
	}

	public String getDir() {
		return dir;
	}
	public void addSG(StarGraph sg) {
		this.starGraphsList.add(sg);
		// TODO Auto-generated method stub
		
	}
}
