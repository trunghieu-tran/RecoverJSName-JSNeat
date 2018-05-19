package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.VariableInitializer;

public class JSSymbol
{
	private AstNode node = null;
	
	private ArrayList<JSSymbol> children = new ArrayList<>();
	private Map<String, JSSymbol> localVars = new HashMap<String, JSSymbol>();
	private String name = null;
	private JSSymbol parent = null;

	public JSSymbol(AstNode node)
	{
		this.node = node;
		if (node instanceof VariableInitializer)
			name = ((Name)((VariableInitializer) node).getTarget()).getIdentifier();
		else if (node instanceof FunctionNode)
		{
			Name funcName = ((FunctionNode)node).getFunctionName();
			if (funcName != null)
				name = funcName.getIdentifier();
			else
			{
				AstNode parent = node.getParent();
				if (parent instanceof VariableInitializer)
				{
					name = ((Name)((VariableInitializer)parent).getTarget()).getIdentifier();
				}
				else
					name = "Anonymous";
			}
			FunctionNode funcNode = (FunctionNode)node;
			List<AstNode> args = funcNode.getParams();
			if (args != null)
			{
				for (AstNode argNode : args)
					addChild(argNode);
			}
		}
		else if (node instanceof Name)
			name = ((Name)node).getIdentifier();
	}
	
	public int getType() {
		return node.getType();
	}

	public void addChild(JSSymbol child)
	{
		if (child.getType() == Token.VAR)
		{
			//check if it is already added
			AstNode childNode = child.getNode();
			if (childNode instanceof VariableInitializer)
			{
				String varName = ((Name)((VariableInitializer) childNode).getTarget()).getIdentifier();
				if (localVars.containsKey(varName))
					return;
				localVars.put(varName, child);
			}
		}
		children.add(child);
		child.setParent(this);
	}
	
	public void addChild (AstNode node)
	{
		addChild(new JSSymbol(node));
	}
	
	public ArrayList<JSSymbol> getChildren()
	{
		return new ArrayList<>(children);
	}

	public AstNode getNode() {
		return node;
	}
	
	public boolean childExist (String name)
	{
		return localVars.containsKey(name);
	}

	public String getName() {
		return name;
	}
	
	public void visit (IJSSymbolVisitor visitor)
	{
		boolean ret = visitor.visit(this);
		if (ret)
		{
			for (JSSymbol child : children)
				child.visit(visitor);
		}
	}

	public JSSymbol getParent() {
		return parent;
	}

	public void setParent(JSSymbol parent) {
		this.parent = parent;
	}

	void setName(String name) {
		this.name = name;
	}
}