package parser;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.mozilla.javascript.ast.*;

import singleVarResolution.StarGraph;

/**
 * @author Mike
 * Build a forest from the training dataset
 */
public class ForestVisitor implements NodeVisitor{
	String path;
	String sgPath;
	HashSet<StarGraph> sgSet = new HashSet<>();
	int anonymousCount = 0; //Handle Anonymous Function
	
	public ForestVisitor(String path) {
		this.path = path;
	}
	
	public HashSet<StarGraph> getStarForest() {
		return sgSet;
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
			/**
			 * @TODO: add function hierarchy to find more variable
			 * 
			 */
			FindVariableVisitor visitor = new FindVariableVisitor();
			node.visit(visitor);
			vn.addAll(visitor.getVN());
			//Test variable name set
//			for(String s: vn)
//			{
//				System.out.print(s + " ");
//			}
//			System.out.println();

			String functionName = ((FunctionNode)node).getName();
			if ( functionName.isEmpty() ) {
				functionName = "anonymous" + Integer.toString(anonymousCount++);
			}
			String dir = path + "_" + functionName;
			String temp = sgPath + "_" + functionName;
			FunctionVisitor fv = new FunctionVisitor(vn, dir);
			node.visit(fv);
			try {
				//fv.printToFile(dir);
//				if (functionName.equals("tokenize"))
//				{
//					fv.print();
//				}
				//fv.print();
				fv.printStarGraph(temp);
				sgSet.addAll(fv.getStarGraph());
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public void getSgPath(String sgPath) {
		this.sgPath = sgPath;
	}
}
