package parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;

public class BakerVisitor implements NodeVisitor{
		String path = "";
		int anonymousCount = 0; //Handle Anonymous Function
		HashMap<Record, Integer> recordList = new HashMap<>();
		
		public void printToFile(String dest) throws IOException
		{
			if ( recordList.isEmpty() )
			{
				return;
			}
			File dir = new File(dest);
			if ( ! dir.exists() )
			{
				dir.mkdirs();
			}
			HashMap<String,Pair> pe = new HashMap<>(); //Program Entity
			HashMap<String,Pair> vn = new HashMap<>(); //Variable Names
			HashMap<String,Integer> rel = new HashMap<>(); //Relationship
			int peIndex = 0, vnIndex = 0, reIndex = 0;
			File file1 = new File(dest + "/peData.txt");
			FileWriter fileWriter1 = new FileWriter(file1);
		    PrintWriter printWriter1 = new PrintWriter(fileWriter1);
			File file2 = new File(dest + "/varNameData.txt");
			FileWriter fileWriter2 = new FileWriter(file2);
		    PrintWriter printWriter2 = new PrintWriter(fileWriter2);
			File file3 = new File(dest + "/relData.txt");
			FileWriter fileWriter3 = new FileWriter(file3);
		    PrintWriter printWriter3 = new PrintWriter(fileWriter3);
			File file4 = new File(dest + "/recordData.txt");
			FileWriter fileWriter4 = new FileWriter(file4);
		    PrintWriter printWriter4 = new PrintWriter(fileWriter4);
		    for ( Record record: recordList.keySet() )
			{
		    	//Record about PE and Var
			    if ( record.type == 0 ) { 
					//Collect freq and index for P.E. 
					if (! pe.containsKey(record.pe) ) {
						pe.put(record.pe, new Pair(peIndex,1));
						peIndex++;
					}
					else {
						int freq = pe.get(record.pe).freq;
						int idx = pe.get(record.pe).index;
						pe.put(record.pe, new Pair(idx,freq+1));
					}
					
					//Collect freq and index for Variable Name.  
					if (! vn.containsKey(record.name) ) {
						vn.put(record.name, new Pair(vnIndex,1));
						vnIndex++;
					}
					else {
						int freq = vn.get(record.name).freq;
						int idx = vn.get(record.name).index;
						vn.put(record.name, new Pair(idx,freq+1));
					}
					
					//Print to file 3
					if (! rel.containsKey(record.relationship) ) {
						rel.put(record.relationship, reIndex);
						printWriter3.println(reIndex + " " + record.relationship);
						reIndex++;
					}
					//Print to file 4
					printWriter4.println(pe.get(record.pe).index + " "+  vn.get(record.name).index + " " 
					+ rel.get(record.relationship) + " " + record.type + " " + recordList.get(record));
			    }
		    	//Record about Var-Var
		    	else if ( record.type == 1) {
					//Collect freq and index for Variable 1. 
					if (! vn.containsKey(record.pe) ) {
						vn.put(record.pe, new Pair(vnIndex,1));
						vnIndex++;
					}
					else {
						int freq = vn.get(record.pe).freq;
						int idx = vn.get(record.pe).index;
						vn.put(record.pe, new Pair(idx,freq+1));
					}
					
					//Collect freq and index for Variable 2.  
					if (! vn.containsKey(record.name) ) {
						vn.put(record.name, new Pair(vnIndex,1));
						vnIndex++;
					}
					else {
						int freq = vn.get(record.name).freq;
						int idx = vn.get(record.name).index;
						vn.put(record.name, new Pair(idx,freq+1));
					}
					
					//Print to file 3
					if (! rel.containsKey(record.relationship) ) {
						rel.put(record.relationship, reIndex);
						printWriter3.println(reIndex + " " + record.relationship);
						reIndex++;
					}
					//Print to file 4
					printWriter4.println(vn.get(record.pe).index + " "+  vn.get(record.name).index + " " 
					+ rel.get(record.relationship) + " " + record.type + " " + recordList.get(record));
			    	
		    	}
			}
			
			//Print to file 1
			for ( String key : pe.keySet())
			{
				printWriter1.println(pe.get(key).index + " " + key + " " + pe.get(key).freq);
			}
			
			//Print to file 2
			for ( String key : vn.keySet())
			{
				printWriter2.println(vn.get(key).index + " " + key + " " + vn.get(key).freq);
			}
			printWriter1.close();
			printWriter2.close();
			printWriter3.close();
			printWriter4.close();
		}
		
		@Override
		public boolean visit(AstNode node) {
			if ( node instanceof FunctionNode ) {
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
//				for(String s: vn) {
//					System.out.print(s + " ");
//				}
//				System.out.println();

				String functionName = ((FunctionNode)node).getName();
				if ( functionName.isEmpty() ) {
					functionName = "anonymous" + Integer.toString(anonymousCount++);
				}
				
				FunctionVisitor fv = new FunctionVisitor(vn, functionName);
				node.visit(fv);
//				String dir = path + "_" + functionName;
				recordList.putAll(fv.mergeRecord());
//				try {
//					
//					fv.printToFile(dir);
//					fv.print();
//				} 
//				catch (IOException e) {
//					e.printStackTrace();
//				}
			}
			return true;
		}
}
