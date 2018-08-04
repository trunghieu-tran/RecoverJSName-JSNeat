package association.OldAssociation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class AssociationCalculator {
	HashMap<Integer, Integer> var3Hash = new HashMap<>();
	HashMap<Integer, Integer> var2Hash = new HashMap<>();
	HashMap<Integer, Integer> var1Hash = new HashMap<>();

	String flag, path;
	public static void main(String[] args) {
		String path = "../HashAssocData";
		AssociationCalculator ac;
		try {
			ac = new AssociationCalculator("indirect", path, -1);
			System.out.println( ac.getAssocScore("a", "b", ""));
			System.out.println( ac.getAssocScore("b", "c", ""));
			System.out.println( ac.getAssocScore("c", "d", ""));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param flag: "indirect"
	 * @param path
	 * @param numOfHash: -1
	 * @throws IOException
	 */
	public AssociationCalculator(String flag, String path, int numOfHash) throws IOException {
		this.flag = flag;
		this.path = path;
		//Read data from file
		File fileHash1 = new File(path + "/hash1.txt");
		File fileHash2 = new File(path + "/hash2.txt");
		File fileHash3 = new File(path + "/hash3.txt");
		
		BufferedReader br1 = new BufferedReader(new FileReader(fileHash1));
		BufferedReader br2 = new BufferedReader(new FileReader(fileHash2));
		BufferedReader br3 = new BufferedReader(new FileReader(fileHash3));
		
		String line1;
		int cnt = 0;
		while ((line1 = br1.readLine()) != null) {
			String[] subs = line1.split(" ");
			var1Hash.put(Integer.parseInt(subs[0]), Integer.parseInt(subs[1]));
			if (++cnt == numOfHash) break;
		}
		br1.close();
		
		String line2;
		cnt = 0;
		while ((line2 = br2.readLine()) != null) {
			String[] subs = line2.split(" ");
			var2Hash.put(Integer.parseInt(subs[0]), Integer.parseInt(subs[1]));
			if (++cnt == numOfHash) break;
		}
		br2.close();
		
		String line3;
		cnt = 0;
		while ((line3 = br3.readLine()) != null) {
			String[] subs = line3.split(" ");
			var3Hash.put(Integer.parseInt(subs[0]), Integer.parseInt(subs[1]));
			if (++cnt == numOfHash) break;
		}
		br3.close();
	}
	
	public double getAssocScore(String var1, String var2, String rel) {
		int n1 = var1Hash.getOrDefault(var1.hashCode(), 0);
		int n2 = var1Hash.getOrDefault(var2.hashCode(), 0);
		if (n1 * n2 == 0) return 0.0;
		int n1and2 = 0;
		if ( flag.equals("indirect") ) {
			if ( var2Hash.containsKey((var1+var2).hashCode())) {
				n1and2 = var2Hash.getOrDefault((var1+var2).hashCode(), 0);
			} else if ( var2Hash.containsKey((var2+var1).hashCode())) {
				n1and2 = var2Hash.getOrDefault((var2+var1).hashCode(), 0);
			}
		}
		double mau = n1 + n2 - n1and2;
		if (mau <= 0) return 0;
		return (double) n1and2 /  mau;
	}
	
	public double getAssocScoreTriplet(String var1, String var2, String var3) {
		int n1 = var1Hash.getOrDefault(var1.hashCode(), 0);
		int n2 = var1Hash.getOrDefault(var2.hashCode(), 0);
		int n3 = var1Hash.getOrDefault(var3.hashCode(), 0);
		if (n1 * n2 * n3 == 0) return 0.0;
		int n1and2 = 0;
		if ( flag.equals("indirect") ) {
			n1and2 = Math.max(var2Hash.getOrDefault(var2+var1, 0), 
					var2Hash.getOrDefault(var1+var2, 0));
		}
		if ( n1and2 == 0 ) { return 0; }
		int n123 = 0;
		if ( flag.equals("indirect") ) {
			n123 = Math.max(n123, var3Hash.getOrDefault(var1+var2+var3, 0));
			n123 = Math.max(n123, var3Hash.getOrDefault(var1+var3+var2, 0));
			n123 = Math.max(n123, var3Hash.getOrDefault(var2+var1+var3, 0));
			n123 = Math.max(n123, var3Hash.getOrDefault(var2+var3+var1, 0));
			n123 = Math.max(n123, var3Hash.getOrDefault(var3+var2+var1, 0));
			n123 = Math.max(n123, var3Hash.getOrDefault(var3+var1+var2, 0));
		}
		double result = n123 / n1and2 ;
		return result;
	}
}
