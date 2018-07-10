package parser;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

public class JSErrorReporter implements ErrorReporter {

	@Override
	public void error(String arg0, String arg1, int arg2, String arg3, int arg4) {
		// TODO Auto-generated method stub

	}

	@Override
	public EvaluatorException runtimeError(String arg0, String arg1, int arg2, String arg3, int arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void warning(String arg0, String arg1, int arg2, String arg3, int arg4) {
		// TODO Auto-generated method stub

	}

}
