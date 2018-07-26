package parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.VariableInitializer;

public class FindVariableVisitor implements NodeVisitor{
	ArrayList<String> vn = new ArrayList<>();
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
			if ( name != null )
			{
				vn.add(name);
			}
		}
		return true;
	}
	
	public ArrayList<String> getVN() {
		return vn;
	}
}
