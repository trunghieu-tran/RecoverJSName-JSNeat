package parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;


public class AssociateVisitor implements NodeVisitor{
	String path;
	String sgPath;
	public ArrayList<File> fileList = new ArrayList<>();
	int anonymousCount = 0; //Handle Anonymous Function
	public AssociateVisitor(String path) {
		this.path = path;
	}

	@Override
	public boolean visit(AstNode node) {
		if ( node instanceof FunctionNode )
		{
			HashMap<Record, Integer> recordList = new HashMap<>();
			HashSet<String> vn = new HashSet<>();
			FunctionNode fn = (FunctionNode) node;
			List<AstNode> list = fn.getParams();
			for(AstNode at: list) {
				if ( at instanceof Name ) {
					vn.add(((Name)at).getIdentifier());
				}
			}

			/**
			 * @TODO: add function hierarchy to find more variable
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

			String functionName = fn.getName();
			if ( functionName.isEmpty() ) {
				functionName = "anonymous" + Integer.toString(anonymousCount++);
				return true;
			}
			String dir = path + "_" + functionName;
			FunctionVisitor fv = new FunctionVisitor(vn, dir);
			node.visit(fv);
			for ( int i = 0; i < list.size()-1; i++ ) {
				for (int j = i; j < list.size(); j++ ) {
					AstNode nodeI = list.get(i), nodeJ = list.get(j);
					if ( nodeI instanceof Name && nodeJ instanceof Name ) {
						fv.addRecord(((Name)nodeI).getIdentifier(), 
								((Name)nodeJ).getIdentifier(), "CoArgument");
						fv.addRecord(((Name)nodeI).getIdentifier(), 
								((Name)nodeJ).getIdentifier(), "CoArgument");
					}
				}
			}
			try {
				//fv.printToFile(dir);
//				if (functionName.equals("tokenize"))
//				{
//					fv.print();
//				}
				//fv.print();
				fv.printAsso(dir);
				File assoFile = new File(dir + "/assoc.txt");
				fileList.add(assoFile);
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
			recordList = fv.recordList;
		}
		return true;
	
	}

}
