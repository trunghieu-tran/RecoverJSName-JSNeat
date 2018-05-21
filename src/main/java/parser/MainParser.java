package parser;

import java.io.FileReader;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.IRFactory;
import org.mozilla.javascript.ast.AstRoot;

public class MainParser {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		String filePath = ".\\resources\\test.js";
		
		MainParser demo = new MainParser();
		demo.parseJS(filePath);
	}
	
	public void parseJS (String filePath) throws Exception
	{
		CompilerEnvirons env = new CompilerEnvirons();
		env.setRecoverFromErrors(true);
		
		FileReader strReader = new FileReader(filePath);

		IRFactory factory = new IRFactory(env, new JSErrorReporter());
		AstRoot rootNode = factory.parse(strReader, null, 0);
		
//		JSNodeVisitor nodeVisitor = new JSNodeVisitor();
//		rootNode.visit(nodeVisitor);
//		nodeVisitor.getRoot().visit(new JSSymbolVisitor());
		
		MyVisitor myVisitor = new MyVisitor();
		rootNode.visit(myVisitor);
		
	}	

}
