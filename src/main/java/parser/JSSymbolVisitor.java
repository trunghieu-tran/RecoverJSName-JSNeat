package parser;

import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;

public class JSSymbolVisitor implements IJSSymbolVisitor
{

	public boolean visit(JSSymbol sym) {
		
		AstNode astNode = sym.getNode();
		if (astNode instanceof AstRoot)
			return true;
		
		int tabs = getNumTabs(sym);
		
		for (int i = 0; i < tabs; i++)
			System.out.print("\t");
	
		if (astNode.getType() == Token.FUNCTION)
			System.out.print("Function : ");
		System.out.println(sym.getName());
		
		return true;
	}
	
	private int getNumTabs(JSSymbol sym)
	{
		int tabs = 0;
		JSSymbol currSym = sym;
		while ((currSym = currSym.getParent()) != null)
			tabs++;
		return tabs;
	}
}