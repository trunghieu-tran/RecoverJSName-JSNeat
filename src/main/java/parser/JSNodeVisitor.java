package parser;

import java.util.Stack;

import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.VariableInitializer;

public class JSNodeVisitor implements NodeVisitor
{
	Stack<JSSymbol> functionsStack = new Stack<>();
	int currentFuncEndOffset = -1;
	JSSymbol root = null;
	
	@Override
	public boolean visit(AstNode node) {
		if (node == null)
			return false;
		
		addToParent(node);
		
		return true;
	}
	
	private void addToParent(AstNode node)
	{
		if (root == null)
		{
			root = new JSSymbol(node);
			functionsStack.push(root);
			currentFuncEndOffset = node.getAbsolutePosition() + node.getLength();
			return;
		}
		
		if (functionsStack.size() == 0)
			return;

		int nodeType = node.getType();
		
		//we will track only variables and functions
		if (nodeType != Token.FUNCTION && nodeType != Token.VAR && nodeType != Token.OBJECTLIT && 
				!(nodeType == Token.NAME && node.getParent() instanceof ObjectProperty))
		{
			if (isVariableName(node))
			{
				//check if it is in the current function
				String symbolName = ((Name)node).getIdentifier();
				JSSymbol currentSymContainer = functionsStack.peek();
				if (!currentSymContainer.childExist(symbolName))
				{
					//this is a global symbol
					root.addChild(node);
				}
			}
			return;
		}
		
		if (node.getType() == Token.VAR && node instanceof VariableInitializer == false)
			return;
		
		JSSymbol currSym = null;
		
		JSSymbol parent = functionsStack.peek();
		if (parent.getNode().getAbsolutePosition() + parent.getNode().getLength() > node.getAbsolutePosition())
		{
			//add child node to parent
			currSym = new JSSymbol(node);
			parent.addChild(currSym);
		} 
		else //outside current function boundary
		{
			//pop current parent
			functionsStack.pop();
			addToParent(node);
			return;
		}

		//currSym is already set above
		if (nodeType == Token.FUNCTION || nodeType == Token.OBJECTLIT)
		{
			AstNode parentNode = node.getParent();
			AstNode leftNode = null;
			if (parentNode.getType() == Token.ASSIGN)
			{
				leftNode = ((Assignment)parentNode).getLeft();
			} 
			else if (parentNode instanceof ObjectProperty)
			{
				leftNode = ((ObjectProperty)parentNode).getLeft();
			}

			if (leftNode instanceof Name)
				currSym.setName(((Name)leftNode).getIdentifier());

			functionsStack.push(currSym);
			currentFuncEndOffset = node.getAbsolutePosition() + node.getLength();
		}
	}
	
	//This is a helper function to get variables used outside 
	//variable initializer
	private boolean isVariableName (AstNode node)
	{
		AstNode parentNode = node.getParent();
		
		if (parentNode == null || node instanceof Name == false)
			return false;
		
		int parentType =  parentNode.getType();
		
		if (parentType == Token.GETPROP) //get only the left most variable 
		{
			return (((PropertyGet)parentNode).getLeft() == node);
		}
		
		return (parentType != Token.FUNCTION && 
				parentType != Token.VAR &&
				parentType != Token.CALL
			);
		
	}
	
	public JSSymbol getRoot() {
		return root;
	}
}