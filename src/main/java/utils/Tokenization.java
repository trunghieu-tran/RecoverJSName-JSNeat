package utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;

public class Tokenization {
	public static void main(String[] args) {

		ArrayList<String> tests = tokenize("thisIsNgo_c");
		for(String s : tests) {
			System.out.print( s + " ");
		}
		System.out.println();
	}
	public static ArrayList<String> tokenize(String theString) {
		HashSet<String> stopWords = new HashSet<>();
		stopWords.add("get");
		stopWords.add("set");
		stopWords.add("I");
		stopWords.add("this");
		ArrayList<String> splits = split(theString);
		ArrayList<String> results = new ArrayList<>();
		for ( String str : splits ) {
			if (stopWords.contains(str) ) {
				continue;
			}
			Stemmer s = new Stemmer();
			for( char c: str.toCharArray() ) {
				s.add(c);
			}
			s.stem();
			results.add(s.toString());
		}
		return results;
	}
	public static ArrayList<String> split(String string) {
	    StringBuilder separatedWords = new StringBuilder();

	    for (int i=0; i<string.length(); i++) {
	        char c = string.charAt(i);

	        if (i > 0) {    
	            char previousC = string.charAt(i-1);

	            if ((!Character.isLowerCase(c) && !Character.isUpperCase(previousC)) //  UpperCamelCase, UPPER_DASHED
	                    || !Character.isLetterOrDigit(previousC)                    // lower_dashed
	                    || (i < string.length() - 1) && Character.isLowerCase(string.charAt(i+1))  && Character.isUpperCase(c)  ) // IAttribute
	            {       
	                separatedWords.append(" ");
	            }
	        }       
	        if (Character.isLetterOrDigit(c)) {
	            separatedWords.append(c);
	        }
	    }

	    ArrayList<String> tokens = new ArrayList<String>();

	    StringTokenizer tokenizer = new StringTokenizer(separatedWords.toString());

	    while(tokenizer.hasMoreTokens()) {
	        tokens.add(tokenizer.nextToken());
	    }
	    return tokens;
	}
}
