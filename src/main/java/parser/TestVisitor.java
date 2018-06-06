package parser;
import java.io.IOException;

import org.mozilla.javascript.ast.*;


public class TestVisitor implements NodeVisitor{
	String path;
	
	public TestVisitor(String path) {
		this.path = path;
	}

	@Override
	public boolean visit(AstNode node) {
		if ( node instanceof FunctionNode )
		{
			FunctionVisitor fv = new FunctionVisitor();
			node.visit(fv);
			String dir = path + "/" + ((FunctionNode)node).getName();
			try {
				fv.printToFile(dir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
}
