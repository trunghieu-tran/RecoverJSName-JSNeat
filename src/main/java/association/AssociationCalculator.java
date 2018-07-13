package association;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class AssociationCalculator {
	HashMap<Integer, Integer> var2Hash = new HashMap<>();
	HashMap<Integer, Integer> var1Hash = new HashMap<>();
	String flag, path;
	public static void main(String[] args) {
		String path = "../HashAssocData";
		AssociationCalculator ac;
		try {
			ac = new AssociationCalculator("indirect", path);
			System.out.println( ac.getAssocScore("a", "b", ""));
			System.out.println( ac.getAssocScore("b", "c", ""));
			System.out.println( ac.getAssocScore("c", "d", ""));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public AssociationCalculator(String flag, String path) throws IOException {
		this.flag = flag;
		this.path = path;
		//Read data from file
		File fileHash1 = new File(path + "/hash1.txt");
		File fileHash2 = new File(path + "/hash2.txt");
		BufferedReader br1 = new BufferedReader(new FileReader(fileHash1));
		BufferedReader br2 = new BufferedReader(new FileReader(fileHash2));
		String line1;
		while ((line1 = br1.readLine()) != null) {
			String[] subs = line1.split(" ");
			var1Hash.put(subs[0].hashCode(), Integer.parseInt(subs[1]));
		}
		br1.close();
		String line2;
		while ((line2 = br2.readLine()) != null) {
			String[] subs = line2.split(" ");
			var2Hash.put(subs[0].hashCode(), Integer.parseInt(subs[1]));
		}
		br2.close();
		
	}
	public double getAssocScore(String var1, String var2, String rel) {
		int n1 = var1Hash.get(var1.hashCode());
		int n2 = var1Hash.get(var2.hashCode());
		int n1and2 = 0;
		if ( flag.equals("indirect") ) {
			if ( var2Hash.containsKey((var1+var2).hashCode())) {
				n1and2 = var2Hash.get((var1+var2).hashCode());
			} else if ( var2Hash.containsKey((var2+var1).hashCode())) {
				n1and2 = var2Hash.get((var2+var1).hashCode());
			}
		}
		return (double) n1and2 / (double) (n1 + n2 - n1and2);
	}
}
