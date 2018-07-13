package association;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

//Non-hash Version
public class AssociationMiner {
	HashMap<String, Integer> var2Hash = new HashMap<>();
	HashMap<String, Integer> var1Hash = new HashMap<>();
	public static void main(String[] args) {
		String path = "../AssocData";
		AssociationMiner am = new AssociationMiner();
		am.loadAssocData(0, path);
		am.writeHashAssoc("../HashAssocData");
	}
	
	public void writeHashAssoc(String path) {
		File fileHash1 = new File(path + "/hash1.txt");
		File fileHash2 = new File(path + "/hash2.txt");
		
		FileWriter fileWriter1 = null;
		FileWriter fileWriter2 = null;
		try {
			fileWriter1 = new FileWriter(fileHash1);
			fileWriter2 = new FileWriter(fileHash2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    PrintWriter pw1 = new PrintWriter(fileWriter1);
	    PrintWriter pw2 = new PrintWriter(fileWriter2);
	    for(String i : var1Hash.keySet()) {
	    	pw1.println(i + " " + var1Hash.get(i));
	    }
	    for(String i : var2Hash.keySet()) {
	    	pw2.println(i + " " + var2Hash.get(i));
	    }
	    pw1.close();
	    pw2.close();
	}
	
	/**
	 * @param flag
	 * if flag == 0, indirect relationship
	 * if flag == 1, direct relationship
	 */
	public void loadAssocData(int flag, String path) {
		File corpus = new File(path);
		int count = 0;
		for( File dir: corpus.listFiles() ) {
			count++;
			if ( count % 100 == 0)
			{
				System.out.println("Processed " + count + " files");
			}
			//Each dir is a function
			for ( File file: dir.listFiles() ) {
				HashSet<String> varList = new HashSet<>();
				try {
					BufferedReader br = new BufferedReader(new FileReader(file));
					String st;
					while ((st = br.readLine()) != null) {
						String[] subs = st.split(" ");
						if ( flag == 0 ) {
							if ( subs.length == 1 ) {
								//hash1
								String varHashCode = subs[0];
								if ( var1Hash.containsKey(varHashCode) ) {
									var1Hash.put(varHashCode, var1Hash.get(varHashCode)+1);
								} else {
									var1Hash.put(varHashCode, 1);
								}
								varList.add(subs[0]);
							}
						}
						else if ( flag == 1 ) {
							if ( subs.length == 3 ) {
								//hash2
								String relHashCode = st;
								if ( var2Hash.containsKey(relHashCode) ) {
									var2Hash.put(relHashCode, var2Hash.get(relHashCode)+1);
								} else {
									var2Hash.put(relHashCode, 1);
								}
								
								//hash1.l
								String varHashCode1 = subs[0]+subs[2];
								if ( var1Hash.containsKey(varHashCode1) ) {
									var1Hash.put(varHashCode1, var1Hash.get(varHashCode1)+1);
								} else {
									var1Hash.put(varHashCode1, 1);
								}
								
								//hash1.2
								String varHashCode2 = subs[0]+subs[2];
								if ( var1Hash.containsKey(varHashCode2) ) {
									var1Hash.put(varHashCode2, var1Hash.get(varHashCode2)+1);
								} else {
									var1Hash.put(varHashCode2, 1);
								}
							}
						}
					}
					br.close();
				} 
				catch ( IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//Fill var2Hash if flag == 0
				ArrayList<String> al = new ArrayList<String>(varList);
				for (int i = 0; i < al.size() - 1; i++) {
					for ( int j = i + 1; j < al.size(); j++ ) {
						String relHashCode = al.get(i) + al.get(j);
						if ( var2Hash.containsKey(relHashCode) ) {
							var2Hash.put(relHashCode, var2Hash.get(relHashCode)+1);
						} else {
							var2Hash.put(relHashCode, 1);
						}
					}
				}
			}
		}
		

	}
}
//Hash Version
//public class AssociationMiner {
//
//	HashMap<Integer, Integer> var2Hash = new HashMap<>();
//	HashMap<Integer, Integer> var1Hash = new HashMap<>();
//	public static void main(String[] args) {
//		String path = "../AssocData";
//		AssociationMiner am = new AssociationMiner();
//		am.loadAssocData(0, path);
//		am.writeHashAssoc("../HashAssocData");
//	}
//	
//	public void writeHashAssoc(String path) {
//		File fileHash1 = new File(path + "/hash1.txt");
//		File fileHash2 = new File(path + "/hash2.txt");
//		
//		FileWriter fileWriter1 = null;
//		FileWriter fileWriter2 = null;
//		try {
//			fileWriter1 = new FileWriter(fileHash1);
//			fileWriter2 = new FileWriter(fileHash2);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	    PrintWriter pw1 = new PrintWriter(fileWriter1);
//	    PrintWriter pw2 = new PrintWriter(fileWriter2);
//	    for(Integer i : var1Hash.keySet()) {
//	    	pw1.println(i + " " + var1Hash.get(i));
//	    }
//	    for(Integer i : var2Hash.keySet()) {
//	    	pw2.println(i + " " + var2Hash.get(i));
//	    }
//	    pw1.close();
//	    pw2.close();
//	}
//	
//	/**
//	 * @param flag
//	 * if flag == 0, indirect relationship
//	 * if flag == 1, direct relationship
//	 */
//	public void loadAssocData(int flag, String path) {
//		File corpus = new File(path);
//		HashSet<String> varList = new HashSet<>();
//		for( File dir: corpus.listFiles() ) {
//			//Each dir is a function
//			for ( File file: dir.listFiles() ) {
//				try {
//					BufferedReader br = new BufferedReader(new FileReader(file));
//					String st;
//					while ((st = br.readLine()) != null) {
//						String[] subs = st.split(" ");
//						if ( flag == 0 ) {
//							if ( subs.length == 1 ) {
//								//hash1
//								int varHashCode = subs[0].hashCode();
//								if ( var1Hash.containsKey(varHashCode) ) {
//									var1Hash.put(varHashCode, var1Hash.get(varHashCode)+1);
//								} else {
//									var1Hash.put(varHashCode, 1);
//								}
//								varList.add(subs[0]);
//							}
//						}
//						else if ( flag == 1 ) {
//							if ( subs.length == 3 ) {
//								//hash2
//								int relHashCode = (subs[0]+subs[1]+subs[2]).hashCode();
//								if ( var2Hash.containsKey(relHashCode) ) {
//									var2Hash.put(relHashCode, var2Hash.get(relHashCode)+1);
//								} else {
//									var2Hash.put(relHashCode, 1);
//								}
//								
//								//hash1
//								int varHashCode = (subs[0]+subs[2]).hashCode();
//								if ( var1Hash.containsKey(varHashCode) ) {
//									var1Hash.put(varHashCode, var1Hash.get(varHashCode)+1);
//								} else {
//									var1Hash.put(varHashCode, 1);
//								}
//							}
//						}
//					}
//					br.close();
//				} 
//				catch ( IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//		//Fill var2Hash if flag == 0
//		ArrayList<String> al = new ArrayList<String>(varList);
//		for (int i = 0; i < al.size() - 1; i++) {
//			for ( int j = i; j < al.size(); j++ ) {
//				int relHashCode = (al.get(i) + al.get(j)).hashCode();
//				if ( var2Hash.containsKey(relHashCode) ) {
//					var2Hash.put(relHashCode, var2Hash.get(relHashCode)+1);
//				} else {
//					var2Hash.put(relHashCode, 1);
//				}
//			}
//		}
//	}
//
//}
