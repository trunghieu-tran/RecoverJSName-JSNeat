package parser;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

public class JSErrorReporter implements ErrorReporter {

	public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
		// TODO Auto-generated method stub

	}

	public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
		// TODO Auto-generated method stub

	}

	public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource,
			int lineOffset) {
		// TODO Auto-generated method stub
		return null;
	}

}
