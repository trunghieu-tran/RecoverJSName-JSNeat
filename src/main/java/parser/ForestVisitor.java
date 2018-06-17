package parser;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.mozilla.javascript.ast.*;

/**
 * @author Mike
 * Build a forest from the training dataset
 */
public class ForestVisitor implements NodeVisitor{
	String path;
	
	public ForestVisitor(String path) {
		this.path = path;
	}

	@Override
	public boolean visit(AstNode node) {
		if ( node instanceof FunctionNode )
		{
			HashSet<String> vn = new HashSet<>();
			FunctionNode fn = (FunctionNode) node;
			List<AstNode> list = fn.getParams();
			for(AstNode at: list)
			{
				if ( at instanceof Name )
				{
					vn.add(((Name)at).getIdentifier());
				}
			}
			FindVariableVisitor visitor = new FindVariableVisitor();
			node.visit(visitor);
			vn.addAll(visitor.getVN());
			FunctionVisitor2 fv = new FunctionVisitor2(vn);
			node.visit(fv);
			String dir = path + "_" + ((FunctionNode)node).getName();
			try {
				fv.printToFile(dir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
}
