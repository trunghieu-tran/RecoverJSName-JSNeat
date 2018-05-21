package parser;

import java.util.HashMap;
import java.util.HashSet;

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
			if ( target instanceof Name )
			{
				String name = ((Name) target).getIdentifier();
				String pe = pg.getProperty().getIdentifier(); //Go to P.E
				addRecord(pe, name, "property");
			}
		}
		
		if ( node instanceof Assignment )
		{
			Assignment assignment = (Assignment) node;
			if ( assignment.getLeft() instanceof Name )
			{
				String name = ((Name) assignment.getLeft()).getIdentifier();
				String pe = getProgramEntity(assignment.getRight());
				addRecord(pe, name, "assignment");
			}
		}
		if ( node instanceof FunctionCall )
		{
			FunctionCall fc = (FunctionCall)node;
		}
		return true;
	}

	private String getProgramEntity(AstNode right) {
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
