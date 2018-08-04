package association;

import utils.Tokenization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author Harry Tran on 8/4/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class AssociationTokenVarVar {
	HashMap<Integer, Integer> token2Hash = new HashMap<>();
	HashMap<Integer, Integer> token1Hash = new HashMap<>();

	private void addOneVar(String name) {
		ArrayList<String> tokens = Tokenization.tokenize(name);

		// Add itself
		if (tokens.size() > 1) tokens.add(name);

		for ( String token: tokens ) {
			int tokenHashCode = token.hashCode();
			token1Hash.put(tokenHashCode, token1Hash.getOrDefault(tokenHashCode, 0) +1);
		}
	}

	private void addOnePair(String name1, String name2, String rel) {
		ArrayList<String> tokensI = Tokenization.tokenize(name1);
		ArrayList<String> tokensJ = Tokenization.tokenize(name2);

		// add itself
		if (tokensI.size() > 1) tokensI.add(name1);
		if (tokensJ.size() > 1) tokensJ.add(name2);

		for ( String tokenI : tokensI ) {
			for ( String tokenJ : tokensJ ) {
				int token2HashCode = (tokenI + tokenJ).hashCode();
				token2Hash.put(token2HashCode, token2Hash.getOrDefault(token2HashCode, 0) +1);
			}
		}
	}

	public void addInfo(String name1, String name2, String rel) {
		addOneVar(name1);
		addOneVar(name2);
		addOnePair(name1, name2, rel);
	}

	public String showInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("token1Hash size = ").append(token1Hash.size()).append("\n");
		sb.append("token2Hash size = ").append(token2Hash.size()).append("\n");
		return sb.toString();
	}

	public double getAssocScore(String var1, String var2, String rel) {
		ArrayList<String> tokens1 = Tokenization.tokenize(var1);
		ArrayList<String> tokens2 = Tokenization.tokenize(var2);

		//Calculate tokens1 | tokens2
		double tokens1G2 = 0;
		double sum = 0;
		//tokens1G2 = average(token1i | tokens2 );
		for ( String token1 : tokens1 ) {
			double token1iG2 = 0;
			//token1i | tokens2 = max( token1i | token2i );
			for ( String token2: tokens2 ) {
				//token1i | token2i = token2Hash.getOrDefault(key, defaultValue)
				int n12 = 0;
				n12 = Math.max(n12, token2Hash.getOrDefault((token1+token2).hashCode(), 0));
				n12 = Math.max(n12, token2Hash.getOrDefault((token2+token1).hashCode(), 0));
				//System.out.println(n12);
				int n2 = token1Hash.getOrDefault(token2.hashCode(), 0);
				//System.out.println(n2);
				double token1G2 = ( n2 == 0 ) ? 0 : (double ) n12 / (double) n2;
				token1iG2 = Math.max(token1iG2, token1G2);
			}
			sum += token1iG2;
		}
		tokens1G2 = sum / tokens1.size();

		//Calculate tokens2 | tokens1
		double tokens2G1 = 0;
		sum = 0;
		for ( String token2 : tokens2 ) {
			double token2iG1 = 0;
			for ( String token1: tokens1 ) {
				int n12 = 0;
				n12 = Math.max(n12, token2Hash.getOrDefault((token1+token2).hashCode(), 0));
				n12 = Math.max(n12, token2Hash.getOrDefault((token2+token1).hashCode(), 0));
				int n1 = token1Hash.getOrDefault(token1.hashCode(), 0);
				double token2G1 = ( n1 == 0 ) ? 0 : (double ) n12 / (double) n1;
				token2iG1 = Math.max(token2iG1, token2G1);
			}
			sum += token2iG1;
		}
		tokens2G1 = sum / tokens2.size();

		return (double) ( tokens1G2 + tokens2G1 ) / 2.0;
	}
}
