package parser;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.IRFactory;
import org.mozilla.javascript.ast.AstRoot;

public class CheckJSNiceParser {
	ArrayList<ArrayList<String>> allVar1 = new ArrayList<>();
	ArrayList<ArrayList<String>> allVar2 = new ArrayList<>();
	public static void main(String[] args) {
		CheckJSNiceParser temp = new CheckJSNiceParser("C:\\Users\\Mike\\Desktop\\canvasWithEvents.Orig.js", 
				"C:\\Users\\Mike\\Desktop\\canvasWithEvents.Re.js");
		if ( temp.checkLength() ) {
			System.out.println("Good");
		} else {
			System.out.println("Bad");
		}
		
		ArrayList<ArrayList<String>> test1 = temp.getAllVar1();
		ArrayList<ArrayList<String>> test2 = temp.getAllVar2();
		
	}
	public boolean checkLength() {
		if ( allVar1.size() != allVar2.size() ) {
			return false;
			//System.out.println("Problematic!");
		}
		for ( int i = 0; i < allVar1.size(); i++ ) {
			ArrayList<String> set1 = allVar1.get(i);
			ArrayList<String> set2 = allVar2.get(i);
			if ( set1.size() != set2.size() ) {
				return false;
//				System.out.println(set1.size());
//				for(String s : set1 ) {
//					System.out.print(s + " ");
//				}
//				System.out.println();
//				System.out.println(set2.size());
//				for(String s : set2 ) {
//					System.out.print(s + " ");
//				}
//				System.out.println();
//				System.out.println("Problematic at index " + i);
			}
		}
		return true;
	}
	public CheckJSNiceParser(String file1, String file2 ) {
		ArrayList<LinkedHashSet<String>> result = new ArrayList<>();
		try {
			CompilerEnvirons env = new CompilerEnvirons();
			env.setRecoverFromErrors(true);
			FileReader strReader = new FileReader(file1);
			IRFactory factory = new IRFactory(env, new JSErrorReporter());
			JSNiceVisitor myVisitor = new JSNiceVisitor("", "jsnice");
			AstRoot rootNode = factory.parse(strReader, null, 0);
			rootNode.visit(myVisitor);
			allVar1 = myVisitor.getAllVar();
		}
		catch (Exception e) {
			System.out.println("\nCan't parse file " + file1);
		}
		
		try {
			CompilerEnvirons env = new CompilerEnvirons();
			env.setRecoverFromErrors(true);
			FileReader strReader = new FileReader(file2);
			IRFactory factory = new IRFactory(env, new JSErrorReporter());
			JSNiceVisitor myVisitor = new JSNiceVisitor("", "jsnice");
			AstRoot rootNode = factory.parse(strReader, null, 0);
			rootNode.visit(myVisitor);
			allVar2 = myVisitor.getAllVar();
		}
		catch (Exception e) {
			System.out.println("\nCan't parse file " + file2);
		}
	}
	
	public ArrayList<ArrayList<String>> getAllVar1() {
		return allVar1;
	}

	public ArrayList<ArrayList<String>> getAllVar2() {
		return allVar2;
	}
}
