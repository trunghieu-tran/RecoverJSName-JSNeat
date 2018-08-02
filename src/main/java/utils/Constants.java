package utils;

/**
 * @author Harry Tran on 7/25/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class Constants {
	public static final int numberOfThread = 40;
	public static final boolean enableFuncNameEdge = false;
	public static final double THRESHOLD = 0.8;
	public static final int TOPK_RESULT = 30;
	public static final int TOPK_BEAMSEARCH = 30;
	public static final boolean usingOnlyVarVarOnBS = true;
	public static final boolean usingNormalizationAllPair = true;
	public static final boolean usingTokenizedFunctionName = false;

	public static final boolean task = false;
	public static final boolean singleVar = true;
	public static final boolean multiVar = true;
}
