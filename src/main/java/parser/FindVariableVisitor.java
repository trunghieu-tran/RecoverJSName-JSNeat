package parser;

import java.util.HashSet;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.VariableInitializer;

public class FindVariableVisitor implements NodeVisitor{
	HashSet<String> vn = new HashSet<>();
	@Override
	public boolean visit(AstNode node) {
		if ( node instanceof VariableInitializer)
		{
			String name = null;
			VariableInitializer vi = (VariableInitializer) node;
			if ( vi.getTarget() instanceof Name )
			{
				name = ((Name) vi.getTarget()).getIdentifier();
			}
			if ( name != null)
			{
				vn.add(name);
			}
		}
		return true;
	}
	
	public HashSet<String> getVN() {
		return vn;
	}
	

}
