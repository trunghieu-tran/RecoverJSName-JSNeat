package association;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import utils.Tokenization;

public class TokenAssociationCalculator {
	HashMap<Integer, Integer> token2Hash = new HashMap<>();
	HashMap<Integer, Integer> token1Hash = new HashMap<>();

	String flag, path;
	public static void main(String[] args) {
		String path = "../HashAssocData";
		TokenAssociationCalculator tokAC;
		try { 
			tokAC = new TokenAssociationCalculator("indirect", path, -1);
			//System.out.println( tokAC.getAssocScore("a", "diMana", ""));
			System.out.println( tokAC.getAssocScore("linhDown", "personGuy", ""));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param flag: "indirect"
	 * @param path
	 * @param numOfHash: -1
	 * @throws IOException
	 */
	public TokenAssociationCalculator(String flag, String path, int numOfHash) throws IOException {
		this.flag = flag;
		this.path = path;
		//Read data from file
		File fileHash1 = new File(path + "/token1.txt");
		File fileHash2 = new File(path + "/token2.txt");
		
		BufferedReader br1 = new BufferedReader(new FileReader(fileHash1));
		BufferedReader br2 = new BufferedReader(new FileReader(fileHash2));
		
		String line1;
		int cnt = 0;
		while ((line1 = br1.readLine()) != null) {
			String[] subs = line1.split(" ");
			token1Hash.put(Integer.parseInt(subs[0]), Integer.parseInt(subs[1]));
			if (++cnt == numOfHash) break;
		}
		br1.close();
		
		String line2;
		cnt = 0;
		while ((line2 = br2.readLine()) != null) {
			String[] subs = line2.split(" ");
			token2Hash.put(Integer.parseInt(subs[0]), Integer.parseInt(subs[1]));
			if (++cnt == numOfHash) break;
		}
		br2.close();
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
