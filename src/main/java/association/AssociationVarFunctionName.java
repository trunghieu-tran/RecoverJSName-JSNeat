package association;

import utils.Constants;
import utils.Tokenization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Harry Tran on 7/25/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class AssociationVarFunctionName {
	Map<Integer, Integer> varNameCount = new HashMap<>();
	Map<Integer, Integer> funcNameCount = new HashMap<>();
	Map<Integer, Integer> varFuncCount = new HashMap<>();

	public void addVarName(String name) {
		int code = name.hashCode();
		varNameCount.put(code, varNameCount.getOrDefault(code, 0) + 1);
	}

	public void addFuncName(String name) {
		int code = name.hashCode();
		funcNameCount.put(code, funcNameCount.getOrDefault(code, 0) + 1);
	}

	public void addVarFuncName(String var, String func) {
		int code = (var + ":" + func).hashCode();
		varFuncCount.put(code, varFuncCount.getOrDefault(code, 0) + 1);
	}

	public void mergeDataFrom(AssociationVarFunctionName an) {
		for (int key : an.varNameCount.keySet()) {
			varNameCount.put(key, varNameCount.getOrDefault(key, 0) + 1);
		}
		for (int key : an.funcNameCount.keySet()) {
			funcNameCount.put(key, funcNameCount.getOrDefault(key, 0) + 1);
		}
		for (int key : an.varFuncCount.keySet()) {
			varFuncCount.put(key, varFuncCount.getOrDefault(key, 0) + 1);
		}
	}

	public void addInfo(String varName, String functionN, boolean usingToken) {
		addFuncName(functionN);
		addVarName(varName);
		addVarFuncName(varName, functionN);

		if (usingToken) {
			ArrayList<String> tokens = Tokenization.split(functionN);
			for (String str : tokens) {
				addVarName(str);
				addVarFuncName(varName, str);
			}
		}
	}

	public double getAsscociationScore(String var, String func, boolean usingToken) {
		if (!usingToken) {
			int code = (var + ":" + func).hashCode();
			int codeV = var.hashCode();
			int codeF = func.hashCode();

			if (!varNameCount.containsKey(codeV) || !funcNameCount.containsKey(codeF) || !varFuncCount.containsKey(code))
				return 0.0;

			int n_vf = varFuncCount.get(code);
			int n_v = varNameCount.get(codeV);
			int n_f = funcNameCount.get(codeF);

			double mau = n_v + n_f - n_vf;
			if (mau == 0) return 0.0;
			return (double) n_vf / mau;
		} else {
			ArrayList<String> tokens = Tokenization.split(func);
			double res = getAsscociationScore(var, func, false);
			for (String str : tokens)
				res = Math.max(res, getAsscociationScore(var, str, false));
			return res;
		}
	}
}
