package parser;

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
