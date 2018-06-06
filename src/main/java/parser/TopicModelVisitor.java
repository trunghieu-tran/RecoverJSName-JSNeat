package parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

import org.mozilla.javascript.ast.*;


public class TopicModelVisitor implements NodeVisitor{
	HashSet<String> programEntities = new HashSet<>();
	HashSet<String> variableNames = new HashSet<>();
	//HashSet<String> relationships = new HashSet<>();
	HashMap<Record, Integer> recordList = new HashMap<>();
	
	public TopicModelVisitor()
	{
		//relationships.add("property");
		//relationships.put("AssignVar", 1);
	}
	
	public void print()
	{
		for ( Record r: recordList.keySet() )
		{
			System.out.println(r.pe+ " "+  r.name + " " + r.relationship + " " + recordList.get(r));
		}
	}
	
	public void printToFile(String dest) throws IOException
	{
		HashMap<String,Pair> pe = new HashMap<>(); //Program Entity
		HashMap<String,Pair> vn = new HashMap<>(); //Variable Names
		HashMap<String,Integer> re = new HashMap<>(); //Relationship
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
			if (! re.containsKey(record.relationship) ) {
				re.put(record.relationship, reIndex);
				printWriter3.println(reIndex + " " + record.relationship);
				reIndex++;
			}
			//Print to file 4
			printWriter4.println(pe.get(record.pe).index + " "+  vn.get(record.name).index + " " 
			+ re.get(record.relationship) + " " + recordList.get(record));
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
		if ( node.getEnclosingFunction() == null )
			return true;
		if ( node instanceof Name )
		{
			String name = ((Name)node).getIdentifier();
			System.out.println(name);
		}
		return true;
	}
}
