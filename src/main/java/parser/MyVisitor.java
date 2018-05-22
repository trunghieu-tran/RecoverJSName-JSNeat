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

	@Override
	public boolean visit(AstNode node) {
		
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
				else
				{
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
		
		if ( node instanceof Assignment )
		{
			Assignment asn = (Assignment) node;
			if ( asn.getLeft() instanceof Name )
			{
				String name = ((Name) asn.getLeft()).getIdentifier();
				String pe = getProgramEntity(asn.getRight());
				addRecord(pe, name, "Assignment");
			}
			else if ( asn.getRight() instanceof Name )
			{
				String name = ((Name) asn.getRight()).getIdentifier();
				String pe = getProgramEntity(asn.getLeft());
				addRecord(pe, name, "Assignment");
			}
		}
		
		if ( node instanceof VariableInitializer )
		{
			VariableInitializer vi = (VariableInitializer) node;
			String pe = getProgramEntity(vi.getInitializer());
			
		}
		
//		if ( node instanceof InfixExpression ) 
//		{
//
//		}
		
		if ( node instanceof FunctionCall )
		{
			FunctionCall fc = (FunctionCall)node;
		}
		return true;
	}

	private String getProgramEntity(AstNode right) {
		if ( right instanceof FunctionCall )
		{
			System.out.println("FunctionCall");
			return "";
		}
		if ( right instanceof Name )
		{
			return ((Name)right).getIdentifier();
		}
		return null;
	}
	
	private void addRecord(String pe, String name, String relationship)
	{
		variableNames.add(name);
		programEntities.add(pe);
		Record r = new Record(pe, name, relationship);
		if(recordList.containsKey(r)) {
			recordList.put(r, recordList.get(r)+1);
		} else {
			recordList.put(r, 0);
		}
	}

}
