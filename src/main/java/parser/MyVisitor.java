package parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.*;


public class MyVisitor implements NodeVisitor{
	HashSet<String> programEntities = new HashSet<>();
	HashSet<String> variableNames = new HashSet<>();
	//HashSet<String> relationships = new HashSet<>();
	HashMap<Record, Integer> recordList = new HashMap<>();
	
	public MyVisitor()
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
	
	public void printToFile() throws IOException
	{
		HashMap<String,Integer> pe = new HashMap<>(); //Program Entity
		HashMap<String,Integer> vn = new HashMap<>(); //Variable Names
		HashMap<String,Integer> re = new HashMap<>(); //Relationship
		int peIndex = 0, vnIndex = 0, reIndex = 0;
		
		File file1 = new File("../Data/_Baker/file1.txt");
		FileWriter fileWriter1 = new FileWriter(file1);
	    PrintWriter printWriter1 = new PrintWriter(fileWriter1);
		File file2 = new File("../Data/_Baker/file2.txt");
		FileWriter fileWriter2 = new FileWriter(file2);
	    PrintWriter printWriter2 = new PrintWriter(fileWriter2);
		File file3 = new File("../Data/_Baker/file3.txt");
		FileWriter fileWriter3 = new FileWriter(file3);
	    PrintWriter printWriter3 = new PrintWriter(fileWriter3);
		File file4 = new File("../Data/_Baker/file4.txt");
		FileWriter fileWriter4 = new FileWriter(file4);
	    PrintWriter printWriter4 = new PrintWriter(fileWriter4);
	    
		for ( Record record: recordList.keySet() )
		{
			if (! pe.containsKey(record.pe) ) {
				pe.put(record.pe, peIndex);
				printWriter1.println(peIndex + " " + record.pe);
				peIndex++;
			}
			if (! vn.containsKey(record.name) ) {
				vn.put(record.name, vnIndex);
				printWriter2.println(vnIndex + " " + record.name);
				vnIndex++;
			}
			if (! re.containsKey(record.relationship) ) {
				re.put(record.relationship, reIndex);
				printWriter3.println(reIndex + " " + record.relationship);
				reIndex++;
			}
			
			printWriter4.println(pe.get(record.pe) + " "+  vn.get(record.name) + " " 
			+ re.get(record.relationship) + " " + recordList.get(record));
		}
		printWriter1.close();
		printWriter2.close();
		printWriter3.close();
		printWriter4.close();
	}
	
	@Override
	public boolean visit(AstNode node) {
		switch(node.getType())
		{
			case (Token.GETPROP): visitProperyGet(node); break;
			case (Token.ASSIGN): visitAssignment(node); break;
			case (Token.VAR):
			case (Token.LET):
			case (Token.CONST): visitVarInit(node); break;
		}
		return true;
	}

	private void visitAssignment(AstNode node) {
		if ( node instanceof Assignment )
		{
			Assignment asn = (Assignment) node;
			if ( asn.getLeft() instanceof Name )
			{
				String name = ((Name) asn.getLeft()).getIdentifier();
				AstNode right = asn.getRight();
				if ( right instanceof InfixExpression && !(right instanceof PropertyGet) )
				{
					visitInfExp(right, name);
				}
				String pe = getProgramEntity(right);
				if ( pe != null)
				{
					addRecord(pe, name, "Assignment");
				}
			}
			else if ( asn.getRight() instanceof Name )
			{
				String name = ((Name) asn.getRight()).getIdentifier();
				AstNode left = asn.getLeft();
				if ( left instanceof InfixExpression && !(left instanceof PropertyGet))
				{
					visitInfExp(left, name);
				}
				String pe = getProgramEntity(left);
				if ( pe != null)
				{
					addRecord(pe, name, "Assignment");
				}
			}
		}
	}
	
	private void visitInfExp(AstNode node, String name) {
		//System.out.println("Here");
		InfixExpression ie = (InfixExpression)node;
		String left = getProgramEntity(ie.getLeft());
		String right = getProgramEntity(ie.getRight());
		if ( left != null )
		{
			addRecord(left, name, "Assignment");
		}
		if ( right != null )
		{
			addRecord(right, name, "Assignment");
		}
	}

	private void visitProperyGet(AstNode node) {
		if ( node instanceof PropertyGet )
		{
			PropertyGet pg = (PropertyGet)node;
			AstNode target = pg.getTarget();
			if (pg.getParent() instanceof FunctionCall)
			{
				FunctionCall fc = (FunctionCall) pg.getParent();
				String pe = ((Name)pg.getRight()).getIdentifier()+ "("
						+ fc.getArguments().size() + ")";
				if ( target instanceof Name )
				{
					String name = ((Name) target).getIdentifier();
					addRecord(pe, name, "FunctionCall");
				}
				
				ArrayList<AstNode> list = new ArrayList<>();
				//Argument
				for( AstNode ast : fc.getArguments() )
				{
					if ( ast instanceof Name )
					{
						list.add(ast);
						String name = ((Name)ast).getIdentifier();
						addRecord(pe, name, "Argument");
					}
				}
				
				//CoArgument
				for ( int i = 0; i < list.size()-1; i++ )
				{
					for ( int j = i+1; j < list.size(); j++ )
					{
						String name1 = ((Name) list.get(i)).getIdentifier();
						String name2 = ((Name) list.get(j)).getIdentifier();
						addRecord(name1, name2, "CoArgument");
						addRecord(name2, name1, "CoArgument");
					}
				}
				
			}
			else
			{
				if ( target instanceof Name )
				{
					String name = ((Name) target).getIdentifier();
					String pe = pg.getProperty().getIdentifier(); 
					addRecord(pe, name, "FieldAccess");
				}
			}
		}
	}
	
	private void visitVarInit(AstNode node) {
		if ( node instanceof VariableInitializer )
		{
			String name;
			VariableInitializer vi = (VariableInitializer) node;
			if ( vi.getTarget() instanceof Name )
			{
				name = ((Name) vi.getTarget()).getIdentifier();
				String pe = getProgramEntity(vi.getInitializer());
				if ( pe != null )
				{
					addRecord(pe, name, "Assignment");
				}
			}
			else
				return;
		}
	}
	
	//This can be become a recursive function in the future
	private String getProgramEntity(AstNode node) {

		if ( node instanceof Name )
		{
			return ((Name)node).getIdentifier();
		}
		if ( node instanceof FunctionCall )
		{
			AstNode target = ((FunctionCall)node).getTarget();
			if ( target instanceof PropertyGet )
			{
				PropertyGet pg = (PropertyGet)target;
				String pe = pg.getProperty().getIdentifier(); 
				return pe+"("+ ((FunctionCall)node).getArguments().size() + ")";
			}
		}
		if ( node instanceof PropertyGet )
		{
			PropertyGet pg = (PropertyGet)node;
			AstNode target = pg.getTarget();
			if (pg.getParent() instanceof FunctionCall)
			{
				FunctionCall fc = (FunctionCall) pg.getParent();
				String pe = ((Name)pg.getRight()).getIdentifier()+ "("
						+ fc.getArguments().size() + ")";
				if ( target instanceof Name )
				{
					String name = ((Name) target).getIdentifier();
					addRecord(pe, name, "FunctionCall");
				}
				
				ArrayList<AstNode> list = new ArrayList<>();
				//Argument
				for( AstNode ast : fc.getArguments() )
				{
					if ( ast instanceof Name )
					{
						list.add(ast);
						String name = ((Name)ast).getIdentifier();
						addRecord(pe, name, "Argument");
					}
				}
				
				//CoArgument
				for ( int i = 0; i < list.size()-1; i++ )
				{
					for ( int j = i+1; j < list.size(); j++ )
					{
						String name1 = ((Name) list.get(i)).getIdentifier();
						String name2 = ((Name) list.get(j)).getIdentifier();
						addRecord(name1, name2, "CoArgument");
						addRecord(name2, name1, "CoArgument");
					}
				}
				
			}
			else
			{
				String pe = pg.getProperty().getIdentifier(); 
				return pe;
			}
		}
		return null;
	}
	
	private void addRecord(String pe, String name, String relationship) {
		variableNames.add(name);
		programEntities.add(pe);
		Record r = new Record(pe, name, relationship);
		if(recordList.containsKey(r)) {
			recordList.put(r, recordList.get(r)+1);
		} else {
			recordList.put(r, 1);
		}
	}

}
