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
	int anonymousCount = 0; //Handle Anonymous Function
	
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
			//Test variable name set
//			for(String s: vn)
//			{
//				System.out.print(s + " ");
//			}
//			System.out.println();
			FunctionVisitor2 fv = new FunctionVisitor2(vn);
			node.visit(fv);
			String functionName = ((FunctionNode)node).getName();
			if ( functionName.isEmpty() ) {
				functionName = "anonymous" + Integer.toString(anonymousCount++);
			}
			String dir = path + "_" + functionName;
			try {
				fv.printToFile(dir);
//				fv.print();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
}
