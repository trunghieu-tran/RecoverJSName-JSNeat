package parser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.mozilla.javascript.ast.*;

import singleVarResolution.StarGraph;
import singleVarResolution.StarGraphToPrint;

/**
 * @author Mike
 * Build a forest from the training dataset
 */
public class JSNiceVisitor implements NodeVisitor{
	String flag; //test //train //debug
	String path;
	String sgPath;
	HashSet<StarGraphToPrint> sgSet = new HashSet<>();
	int anonymousCount = 0; //Handle Anonymous Function
	ArrayList<ArrayList<String>> allVariables;
	public JSNiceVisitor(String path, String flag) {
		this.flag = flag;
		this.path = path;
		if ( flag.equals("jsnice") ) {
			allVariables = new ArrayList<>();
		}
	}
	
	public HashSet<StarGraphToPrint> getStarForest() {
		return sgSet;
	}
	
	public ArrayList<ArrayList<String>> getAllVar() {
		if ( flag.equals("jsnice") ) {
			return allVariables;
		}
		System.out.println("Wrong FLAG, will return NULL!");
		return null;
	}
	
	@Override
	public boolean visit(AstNode node) {
		if ( node instanceof FunctionNode )
		{
			ArrayList<String> vn = new ArrayList<>();
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
//			System.out.println("Variable Names List");
//			for(String s: vn)
//			{
//				System.out.print(s + " ");
//			}
//			System.out.println();
			if ( flag.equals("jsnice") && !vn.isEmpty()) {
				allVariables.add(vn);
			}
			String functionName = ((FunctionNode)node).getName();
			if ( functionName.isEmpty() ) {
				if ( flag.equals("test") || flag.equals("jsnice") ) {
					return true;
				}
				functionName = "anonymous" + Integer.toString(anonymousCount++);
				return true;
			}
//			String dir = path + "_" + functionName;
//			String temp = sgPath + "_" + functionName;
//			FunctionVisitor fv = new FunctionVisitor(vn, dir);
//			node.visit(fv); 
//			try {
//				//fv.printToFile(dir);
////				if (functionName.equals("tokenize"))
////				{
////					fv.print();
////				}
//				//fv.print();
//				fv.printStarGraph(temp);
//				sgSet.addAll(fv.getStarGraph());
//			} 
//			catch (IOException e) {
//				e.printStackTrace();
//			}
		}
		return true;
	}

	public void getSgPath(String sgPath) {
		this.sgPath = sgPath;
	}
}
