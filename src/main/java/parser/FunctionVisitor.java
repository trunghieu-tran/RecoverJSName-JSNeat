package parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.*;

import singleVarResolution.Edge;
import singleVarResolution.StarGraph;

/**
 * @author Mike
 * Visit a function to build a variable graph from it
 * @TODO: Array Access, Unary Expression
 */
public class FunctionVisitor implements NodeVisitor{
	HashSet<String> programEntities = new HashSet<>();
	HashSet<String> variableNames = new HashSet<>();
	//HashSet<String> relationships = new HashSet<>();
	HashMap<Record, Integer> recordList = new HashMap<>();
	HashSet<StarGraph> sgSet = new HashSet<>();
	String functionName;
	public FunctionVisitor(HashSet<String> vn, String functionName)
	{
		variableNames.addAll(vn);
		this.functionName = functionName;
	}
	
	public void print()
	{
		for ( Record r: recordList.keySet() )
		{
			System.out.println(r.pe+ " "+  r.name + " " + r.relationship + " " + r.type);
		}
	}
	
	public void printToFile(String dest) throws IOException
	{
		System.out.println(dest);
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
//		for ( Record record: recordList.keySet() )
//		{
////			//Collect freq and index for P.E. 
////			if (! pe.containsKey(record.pe) ) {
////				pe.put(record.pe, new Pair(peIndex,1));
////				peIndex++;
////			}
////			else {
////				int freq = pe.get(record.pe).freq;
////				int idx = pe.get(record.pe).index;
////				pe.put(record.pe, new Pair(idx,freq+1));
////			}
////			
////			//Collect freq and index for Variable Name.  
////			if (! vn.containsKey(record.name) ) {
////				vn.put(record.name, new Pair(vnIndex,1));
////				vnIndex++;
////			}
////			else {
////				int freq = vn.get(record.name).freq;
////				int idx = vn.get(record.name).index;
////				vn.put(record.name, new Pair(idx,freq+1));
////			}
//			
//			//Print to file 1
//			if (! pe.containsKey(record.pe) ) {
//				pe.put(record.pe, reIndex);
//				printWriter1.println(record.pe);
//			}
//			//Print fo file 2
//			if (! vn.containsKey(record.name) ) {
//				vn.put(record.name, reIndex);
//				printWriter2.println(record.name);
//			}
//			//Print to file 3
//			if (! re.containsKey(record.relationship) ) {
//				re.put(record.relationship, reIndex);
//				printWriter3.println(record.relationship);
//			}
//			//Print to file 4
//			printWriter4.println(record.pe + " "+ record.name + " " 
//			+ record.relationship + " " + record.type);
//		}
		
		printWriter1.close();
		printWriter2.close();
		printWriter3.close();
		printWriter4.close();
	}
	
	@Override
	public boolean visit(AstNode node) {
		if ( node.getEnclosingFunction() == null )
			return true;
		switch(node.getType())
		{
			case (Token.GETPROP): visitProperyGet(node); break;
			case (Token.ASSIGN): visitAssignment(node); break;
			case (Token.VAR):
			case (Token.LET):
			case (Token.CONST): visitVarInit(node); break;
			case (Token.IF): visitIfStmt(node); break;
		}
		return true;
	}

	private void visitIfStmt(AstNode node) {
		if ( node instanceof IfStatement )
		{
			IfStatement ifNode = (IfStatement) node;
			AstNode condNode = ifNode.getCondition();
			if ( condNode instanceof Name ) {
				// @TODO : add record variable of type boolean;
				addRecord("#bool", ((Name)condNode).getIdentifier(), "Boolean");
			}
			if ( condNode instanceof InfixExpression ) {
				// @TODO: if (a > b) : good; if ( a LOGICAL_EXPR b ): both a,b are bool;
				ArrayList<String> operands = new ArrayList<>(visitInfixExp(condNode));
				addFromInfix(operands, "");
				
				int operator = ((InfixExpression)condNode).getOperator();
				if ( operator == Token.AND || operator == Token.OR )
				{
					for ( String operand: operands) {
						addRecord("#bool", operand, "Boolean");
					}
				}
			}

		}
	}

	private void visitAssignment(AstNode node) {
		if ( node instanceof Assignment )
		{
			Assignment asn = (Assignment) node;
			if ( asn.getLeft() instanceof Name )
			{
				String name = ((Name) asn.getLeft()).getIdentifier();
				AstNode right = asn.getRight();
				if ( right instanceof InfixExpression && !(right instanceof PropertyGet) ) {
					ArrayList<String> operands = new ArrayList<>(visitInfixExp(right));
					addFromInfix(operands, name);
				}
				else {
					String pe = getProgramEntity(right);
					if ( pe != null) {
						if ( pe.contains("#array") ) {
							addRecord(pe.substring(0, pe.indexOf("#array")), name, "ArrayAccess");
							addRecord(pe.substring(pe.indexOf("#array")+6), name, "FieldAccess");
						}
						else addRecord(pe, name, "Assignment");
					}
				}
			}
			else if ( asn.getRight() instanceof Name )
			{
				String name = ((Name) asn.getRight()).getIdentifier();
				AstNode left = asn.getLeft();
				if ( left instanceof InfixExpression && !(left instanceof PropertyGet)) {
					ArrayList<String> operands = new ArrayList<>(visitInfixExp(left));
					addFromInfix(operands, name);
				}
				else {
					String pe = getProgramEntity(left);
					if ( pe != null) {
						if ( pe.contains("#array") ) {
							addRecord(pe.substring(0, pe.indexOf("#array")), name, "ArrayAccess");
							addRecord(pe.substring(pe.indexOf("#array")+6), name, "FieldAccess");
						}
						else addRecord(pe, name, "Assignment");
					}
				}
			}
		}
	}
	
	private void addFromInfix(ArrayList<String> operands, String name) {
//		for ( String operand: operands)
//		{
//			System.out.print(operand + " ");
//		}
//		System.out.println();
		for( int i = 0; i < operands.size()-1; i++ ) {
			for ( int j = i+1; j < operands.size(); j++ ) {
				String opr1 = operands.get(i), opr2 = operands.get(j);
				if ( !opr1.contains("#array") && !opr2.contains("#array") ) {
					return;
				} else {
					addRecord(opr1, opr2, "Infix");
					addRecord(opr2, opr1, "Infix");
				}
			}
		}
		if ( ! name.isEmpty() ) {
			for ( int i = 0; i < operands.size(); i++) {
			String pe = operands.get(i);
			if ( pe.contains("#array") ) {
				addRecord(pe.substring(0, pe.indexOf("#array")), name, "ArrayAccess");
				addRecord(pe.substring(pe.indexOf("#array")+6), name, "FieldAccess");
			}
			else addRecord(pe, name, "Assignment");
			}
		}
	}

	private HashSet<String> visitInfixExp(AstNode node) {
		HashSet<String> operands = new HashSet<>();
		if ( node != null ) {
		InfixExpression ie = (InfixExpression)node;
		AstNode astLeft = ie.getLeft();
		String left = "", right = "";
		AstNode astRight = ie.getRight();
		if ( astLeft instanceof InfixExpression && !(astLeft instanceof PropertyGet)) {
			operands.addAll(visitInfixExp(astLeft));
		}
		else {
			left = getProgramEntity(astLeft);
		}
		
		if ( astRight instanceof InfixExpression && !(astRight instanceof PropertyGet)) {
			operands.addAll(visitInfixExp(astRight));
		}
		else {
			right = getProgramEntity(astRight);
		}
		
		if ( !left.isEmpty() ) {
			operands.add(left);
		}
		if ( !right.isEmpty() ) {
			operands.add(right);
		}
		return operands;
		}
		else return operands;
	}

	private void visitProperyGet(AstNode node) {
		if ( node instanceof PropertyGet )
		{
			PropertyGet pg = (PropertyGet)node;
			AstNode target = pg.getTarget();
			if (pg.getParent() instanceof FunctionCall) //Function Call
			{
				FunctionCall fc = (FunctionCall) pg.getParent();
				String pe = ((Name)pg.getRight()).getIdentifier()+ "("
						+ fc.getArguments().size() + ")";
				if ( target instanceof Name )
				{
					String name = ((Name) target).getIdentifier();
					addRecord(pe, name, "FunctionCall");
				}
				
				ArrayList<AstNode> argumentList = new ArrayList<>();
				//Argument
				for( AstNode ast : fc.getArguments() )
				{
					if ( ast instanceof Name )
					{
						argumentList.add(ast);
						String name = ((Name)ast).getIdentifier();
						addRecord(pe, name, "Argument");
					}
				}
				
				//CoArgument
				for ( int i = 0; i < argumentList.size()-1; i++ )
				{
					for ( int j = i+1; j < argumentList.size(); j++ )
					{
						String name1 = ((Name) argumentList.get(i)).getIdentifier();
						String name2 = ((Name) argumentList.get(j)).getIdentifier();
						addRecord(name1, name2, "CoArgument");
						addRecord(name2, name1, "CoArgument");
					}
				}
			}
			else //Field Access
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
				if ( pe != null) {
					if ( pe.contains("#array") ) {
						addRecord(pe.substring(0, pe.indexOf("#array")), name, "ArrayAccess");
						addRecord(pe.substring(pe.indexOf("#array")+6), name, "FieldAccess");
					}
					else addRecord(pe, name, "Assignment");
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
			if ( target instanceof Name )
			{
				String name = ((Name) target).getIdentifier();
				//addRecord(pe, name, "FunctionCall");
			}
			if ( target instanceof PropertyGet )
			{
				PropertyGet pg = (PropertyGet)target;
				String pe = pg.getProperty().getIdentifier(); 
				return pe + "(" + ((FunctionCall)node).getArguments().size() + ")";
			}
		}
		if ( node instanceof PropertyGet )
		{
			PropertyGet pg = (PropertyGet)node;
			if (pg.getParent() instanceof FunctionCall)
			{
				FunctionCall fc = (FunctionCall) pg.getParent();
				String methodName = ((Name)pg.getRight()).getIdentifier()+ "("
						+ fc.getArguments().size() + ")";
				return methodName;
			}
			else
			{
				String fieldName = pg.getProperty().getIdentifier(); 
				return fieldName;
			}
		}
		if ( node instanceof ElementGet ) {
			ElementGet arrayNode = (ElementGet)node;
			AstNode target = arrayNode.getTarget();
			AstNode elem = arrayNode.getElement();
			if ( target instanceof Name ) {
				String elemName = "";
				if ( elem instanceof Name) {
					elemName = ((Name)elem).getIdentifier();
				}
				String ret = ((Name) target).getIdentifier() + "#array" + elemName;
				return ret;
			}
		}
		if ( node instanceof ArrayLiteral ) {
			return "[]";
		}
		return "";
	}

	private String normalizeVarName(String varName) {
		// TODO
		// this function is normalizing variable names into a standard var.name
		// such as: $varname, var_name, varname, varName, VarName... = varname
		//          element12, element23, $element$23$ ... = element_i
		// This depends on how we build the rules
		return varName;
	}

	private void addRecord(String pe1, String pe2, String relationship) {
		//String newName = this.normalizeVarName(name);
		//variableNames.add(newName);
		//programEntities.add(pe);
		if ( pe1.isEmpty() || pe2.isEmpty() )
		{
			return;
		}
		Record r = null;
		if ( variableNames.contains(pe1) && variableNames.contains(pe2) ) {
			if ( pe1.equals(pe2) ) {
				return;
			}
			r = new Record(pe1, pe2, relationship, 1);
		}
		else if ( variableNames.contains(pe2)){
			r = new Record(pe1, pe2, relationship, 0);
			programEntities.add(pe1);
		}
		else if ( variableNames.contains(pe1)) {
			r = new Record(pe2, pe1, relationship, 0);
			programEntities.add(pe2);
		}
		if(recordList.containsKey(r)) {
			recordList.put(r, recordList.get(r)+1);
		} else if ( r != null){
			recordList.put(r, 1);
		}
	}

	/**
	 * Merge record to build the Baker record
	 * 
	 */
	public HashMap<Record, Integer> mergeRecord() {
		return recordList;
	}

	public void buildStarGraph() {
		for( String varName : variableNames ) {
			StarGraph sg;
			HashSet<Edge> edges = new HashSet<>();
			for( Record r: recordList.keySet() ) {
				//Check: right now only pe-var relationship type
				if ( varName.equals(r.name) && r.type == 0) {
					edges.add(new Edge(r.pe, r.relationship, recordList.get(r)));
				}
			}
			if ( edges.isEmpty() ) {
				continue;
			} else {
				functionName = functionName.substring(functionName.lastIndexOf("/")+1);
				int hashCode = Objects.hash(functionName);
				//System.out.println(hashCode);
				sg = new StarGraph(edges, varName + "-" + hashCode);
				sgSet.add(sg);
			}
		}
	}
	
	public void printStarGraph(String dest) throws IOException {
		System.out.println(dest);
		this.buildStarGraph();
		if ( sgSet.isEmpty() )
		{
			return;
		}
		File dir = new File(dest);
		if ( ! dir.exists() )
		{
			dir.mkdirs();
		}
		for( StarGraph sg : sgSet ) {
			File sgFile = new File(dest + "/" + sg.getVarName() + ".txt");
			FileWriter fw = new FileWriter(sgFile);
			PrintWriter pw = new PrintWriter(fw);
			for ( Edge edge: sg.getEdges() )
			{
				String content = edge.toString();
				pw.println(content);
				//System.out.println(edge.toString());
			}
			pw.close();
		}
	}

	public HashSet<StarGraph> getStarGraph() {
		// TODO Auto-generated method stub
		this.buildStarGraph();
		return sgSet;
	}

}
