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
	private String dir;

	public FunctionInfo(HashSet<StarGraph> starGraphsList, String dir) {
		this.starGraphsList = starGraphsList;
		this.dir = dir;
	}

	public HashSet<StarGraph> getStarGraphsList() {
		return starGraphsList;
	}

	public String getDir() {
		return dir;
	}
}
