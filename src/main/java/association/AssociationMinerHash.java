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
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class AssociationMinerHash {
	ConcurrentHashMap<Integer, Integer> var2Hash = new ConcurrentHashMap<>();
	ConcurrentHashMap<Integer, Integer> var1Hash = new ConcurrentHashMap<>();
	int n0Thread = 8;
	public static void main(String[] args) {
		String path = "../debugAssoc";
		AssociationMinerHash am = new AssociationMinerHash();
//		am.loadAssocDataMultiThread(0, path);
		am.loadAssocDataSingleThread(0, path);
		am.writeHashAssoc("../HashAssocData");
	}
	
	public void loadAssocDataSingleThread(int flag, String path) {
		File corpus = new File(path);
		int count = 0;
		for( File dir: corpus.listFiles() ) {
			count++;
			if ( count % 100 == 0)
			{
				System.out.println("Processed " + count + " files");
			}
			for ( File file : dir.listFiles() ) {
				HashSet<String> varList = new HashSet<>();
				try {
					BufferedReader br = new BufferedReader(new FileReader(file));
					String st;
					while ((st = br.readLine()) != null) {
						String[] subs = st.split(" ");
						if ( flag == 0 ) {
							if ( subs.length == 1 ) {
								//hash1
								int varHashCode = subs[0].hashCode();
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
								int relHashCode = st.replace(" ", "").hashCode();
								if ( var2Hash.containsKey(relHashCode) ) {
									var2Hash.put(relHashCode, var2Hash.get(relHashCode)+1);
								} else {
									var2Hash.put(relHashCode, 1);
								}
								
								//hash1.l
								int varHashCode1 = (subs[0]+subs[2]).hashCode();
								if ( var1Hash.containsKey(varHashCode1) ) {
									var1Hash.put(varHashCode1, var1Hash.get(varHashCode1)+1);
								} else {
									var1Hash.put(varHashCode1, 1);
								}
								
								//hash1.2
								int varHashCode2 = (subs[1]+subs[2]).hashCode();
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
						int relHashCode = (al.get(i) + al.get(j)).hashCode();
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
	    for(Integer i : var1Hash.keySet()) {
	    	pw1.println(i + " " + var1Hash.get(i));
	    	System.out.println(i + " " + var1Hash.get(i));
	    }
	    for(Integer i : var2Hash.keySet()) {
	    	pw2.println(i + " " + var2Hash.get(i));
	    	System.out.println(i + " " + var2Hash.get(i));
	    }
	    pw1.close();
	    pw2.close();
	}
	
	public class LoadOneFile implements Runnable {
		int flag;
		File file;
		public LoadOneFile(int flag, File f) {
			this.flag = flag;
			file = f;
		}
		@Override
		public void run() {
			HashMap<Integer, Integer> var2HashLocal = new HashMap<>();
			HashMap<Integer, Integer> var1HashLocal = new HashMap<>();
			HashSet<String> varList = new HashSet<>();
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String st;
				while ((st = br.readLine()) != null) {
					String[] subs = st.split(" ");
					if ( flag == 0 ) {
						if ( subs.length == 1 ) {
							//hash1
							int varHashCode = subs[0].hashCode();
							if ( var1HashLocal.containsKey(varHashCode) ) {
								var1HashLocal.put(varHashCode, var1HashLocal.get(varHashCode)+1);
							} else {
								var1HashLocal.put(varHashCode, 1);
							}
							varList.add(subs[0]);
						}
					}
					else if ( flag == 1 ) {
						if ( subs.length == 3 ) {
							//hash2
							int relHashCode = st.replace(" ", "").hashCode();
							if ( var2HashLocal.containsKey(relHashCode) ) {
								var2HashLocal.put(relHashCode, var2HashLocal.get(relHashCode)+1);
							} else {
								var2HashLocal.put(relHashCode, 1);
							}
							
							//hash1.l
							int varHashCode1 = (subs[0]+subs[2]).hashCode();
							if ( var1HashLocal.containsKey(varHashCode1) ) {
								var1HashLocal.put(varHashCode1, var1HashLocal.get(varHashCode1)+1);
							} else {
								var1HashLocal.put(varHashCode1, 1);
							}
							
							//hash1.2
							int varHashCode2 = (subs[1]+subs[2]).hashCode();
							if ( var1HashLocal.containsKey(varHashCode2) ) {
								var1HashLocal.put(varHashCode2, var1HashLocal.get(varHashCode2)+1);
							} else {
								var1HashLocal.put(varHashCode2, 1);
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
					int relHashCode = (al.get(i) + al.get(j)).hashCode();
					if ( var2HashLocal.containsKey(relHashCode) ) {
						var2HashLocal.put(relHashCode, var2HashLocal.get(relHashCode)+1);
					} else {
						var2HashLocal.put(relHashCode, 1);
					}
				}
			}
			//Merge local hashmap with global
			for ( Integer i : var1HashLocal.keySet() ) {
				var1Hash.put(i, var1Hash.getOrDefault(i, 0) + var1HashLocal.get(i));
			}
			for ( Integer i : var2HashLocal.keySet() ) {
				var2Hash.put(i, var2Hash.getOrDefault(i, 0) + var2HashLocal.get(i));
			}
		}
	}
	
	/**
	 * @param flag
	 * if flag == 0, indirect relationship
	 * if flag == 1, direct relationship
	 */
	public void loadAssocDataMultiThread(int flag, String path) {
		File corpus = new File(path);
		int count = 0;
		ArrayList<File> currFileBatch = new ArrayList<>();
		int check = 0;
		for( File dir: corpus.listFiles() ) {
			count++;
			if ( count % 100 == 0)
			{
				System.out.println("Processed " + count + " files");
			}
			check++;
			if ( check < n0Thread ) {
				currFileBatch.add(dir);
			}
			if ( currFileBatch.size() == n0Thread ) {
				check = 0;
				ExecutorService executor = Executors.newFixedThreadPool(n0Thread);
				File file = new File (dir.getCanonicalPath() + "/assoc.txt");
				Set<LoadOneFile> running = new HashSet<>();
			}
			
			//Each dir is a function
			for ( File file: dir.listFiles() ) {
				LoadOneFile onefile = new LoadOneFile(flag, file);
				executor.execute(onefile);
			}
		}
		
		// Wait until all threads are finish
		executor.shutdown();
		try {
			if (!executor.awaitTermination(7, TimeUnit.DAYS))
				executor.shutdownNow();
		} catch (Exception e) {
			System.out.println("Waiting error");
			executor.shutdownNow();
		}
		System.out.println("FINISHED all threads for assocation mining");
	}
}
