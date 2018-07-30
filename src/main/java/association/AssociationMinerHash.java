package association;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class AssociationMinerHash {
	ConcurrentHashMap<Integer, Integer> var3Hash = new ConcurrentHashMap<>();
	ConcurrentHashMap<Integer, Integer> var2Hash = new ConcurrentHashMap<>();
	ConcurrentHashMap<Integer, Integer> var1Hash = new ConcurrentHashMap<>();
	int n0Thread = 8;
	public static void main(String[] args) {
		String path = "../debugAssoc";
		AssociationMinerHash am = new AssociationMinerHash();
		try {
			am.loadAssocDataMultiThread(0, path);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//am.loadAssocDataSingleThread(0, path);
		am.writeHashAssoc("../HashAssocData");
//		ArrayList<String> al = new ArrayList<>();
//		al.add("a");
//		al.add("b");
//		al.add("c");
//		al.add("d");
//		ArrayList<HashSet<String>> result = getSubsets(al,3);
//		for(HashSet<String> set: result) {
//			for ( String s: set) {
//				System.out.print(s + " ");
//			}
//			System.out.println();
//		}
	}

	

	private static void getSubsets(ArrayList<String> superSet, int k, int idx, HashSet<String> current,ArrayList<HashSet<String>> solution) {
	    //successful stop clause
	    if (current.size() == k) {
	        solution.add(new HashSet<>(current));
	        return;
	    }
	    //unsuccessful stop clause
	    if (idx == superSet.size()) return;
	    String x = superSet.get(idx);
	    current.add(x);
	    //"guess" x is in the subset
	    getSubsets(superSet, k, idx+1, current, solution);
	    current.remove(x);
	    //"guess" x is not in the subset
	    getSubsets(superSet, k, idx+1, current, solution);
	}

	public static ArrayList<HashSet<String>> getSubsets(ArrayList<String> superSet, int k) {
		ArrayList<HashSet<String>> res = new ArrayList<>();
	    getSubsets(superSet, k, 0, new HashSet<String>(), res);
	    return res;
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
		File fileHash3 = new File(path + "/hash3.txt");
		
		FileWriter fileWriter1 = null;
		FileWriter fileWriter2 = null;
		FileWriter fileWriter3 = null;
		try {
			fileWriter1 = new FileWriter(fileHash1);
			fileWriter2 = new FileWriter(fileHash2);
			fileWriter3 = new FileWriter(fileHash3);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintWriter pw1 = new PrintWriter(fileWriter1);
		PrintWriter pw2 = new PrintWriter(fileWriter2);
		PrintWriter pw3 = new PrintWriter(fileWriter3);
		
		for(Integer i : var1Hash.keySet()) {
			pw1.println(i + " " + var1Hash.get(i));
			//System.out.println(i + " " + var1Hash.get(i));
		}
		for(Integer i : var2Hash.keySet()) {
			pw2.println(i + " " + var2Hash.get(i));
			//System.out.println(i + " " + var2Hash.get(i));
		}
		for(Integer i : var3Hash.keySet()) {
			pw3.println(i + " " + var3Hash.get(i));
			//System.out.println(i + " " + var3Hash.get(i));
		}
		pw1.close();
		pw2.close();
		pw3.close();
	}

	public class LoadOneFile implements Runnable {
		int flag;
		File file;
		boolean isLoaded = false;
		HashMap<Integer, Integer> var2HashLocal = new HashMap<>();
		HashMap<Integer, Integer> var1HashLocal = new HashMap<>();
		HashMap<Integer, Integer> var3HashLocal = new HashMap<>();
		public LoadOneFile(int flag, File f) {
			this.flag = flag;
			file = f;
		}
		@Override
		public void run() {
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
							var1HashLocal.put(varHashCode, 
									var1HashLocal.getOrDefault(varHashCode, 0) +1);
							varList.add(subs[0]);
						}
					}
					else if ( flag == 1 ) {
						if ( subs.length == 3 ) {
							//hash2
							int var2HashCode = st.replace(" ", "").hashCode();
							var2HashLocal.put(var2HashCode, 
									var2HashLocal.getOrDefault(var2HashCode, 0) +1);

							//hash1.l
							int varHashCode1 = (subs[0]+subs[2]).hashCode();
							var1HashLocal.put(varHashCode1, 
									var1HashLocal.getOrDefault(varHashCode1, 0) +1);

							//hash1.2
							int varHashCode2 = (subs[1]+subs[2]).hashCode();
							var1HashLocal.put(varHashCode2, 
									var1HashLocal.getOrDefault(varHashCode2, 0) +1);
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
					int var2HashCode = (al.get(i) + al.get(j)).hashCode();
					var2HashLocal.put(var2HashCode, 
							var2HashLocal.getOrDefault(var2HashCode, 0) +1);
				}
			}
			addTriplets(var3HashLocal, al);
			isLoaded = true;
		}
		
		/**
		 * In the same function = has relationship
		 * @param map3
		 * @param al
		 */
		private void addTriplets(HashMap<Integer, Integer> map3, ArrayList<String> al) {
			ArrayList<HashSet<String>> subset3 = getSubsets(al,  3);
			for (int i = 0; i < subset3.size(); i++) {
				for ( String s: subset3.get(i) ) {
					System.out.print(s + " ");
				}
				System.out.println();
			}
			//Put all triplets into var3hash
			for( HashSet<String> set: subset3) {
				String seq = "";
				for ( String s: set ) {
					seq += s;
				}
				int tripletHashCode = seq.hashCode();
				map3.put(tripletHashCode, 
						map3.getOrDefault(tripletHashCode, 0) +1 );
			}
		}
	}

	private boolean isDone(Set<LoadOneFile> running) {
		for (LoadOneFile lf : running)
			if (!lf.isLoaded) return false;
		return true;
	}

	/**
	 * @param flag
	 * if flag == 0, indirect relationship
	 * if flag == 1, direct relationship
	 */
	public void loadAssocDataMultiThread(int flag, String path) throws Exception{
		File corpus = new File(path);
		ArrayList<File> currFileBatch = new ArrayList<>();
		File[] dirs = corpus.listFiles();
		if (dirs == null) return;
		ArrayList<File> allFiles = new ArrayList<>();

		int countDir = 0;
		// Get all files from all directories and put into allFiles
		for( File dir: corpus.listFiles()) {
			countDir++;
			if ( countDir % 500 == 0 ) {
				System.out.println("Loaded " + countDir + " files");
			}
			File[] tmp = dir.listFiles();
			if (tmp == null) continue;
			allFiles.addAll(Arrays.asList(tmp));
		}

		// Divide files into parts of n0Thread-files
		int countFile = 0;
		for( File file: allFiles) {
			currFileBatch.add(file);
			countFile++;
			if ( countFile % 500 == 0 ) {
				System.out.println("Processed " + countFile + " files");
			}
			if ( currFileBatch.size() == n0Thread ) {
				processingFiles(flag, currFileBatch, n0Thread);
				//System.out.println("LOADED " + n0Thread + " Files");
			}
		}
		// Handle the remaining files
		if ( ! currFileBatch.isEmpty() ) {
			System.out.println("LOADED " + currFileBatch.size() + " Files");
			processingFiles(flag, currFileBatch, currFileBatch.size());
		}

		// Wait until all threads are finish

		System.out.println("FINISHED all threads for assocation mining");
	}

	private void processingFiles(int flag, ArrayList<File> currFileBatch, int numThread) {
		ExecutorService executor = Executors.newFixedThreadPool(numThread);
		Set<LoadOneFile> running = new HashSet<>();
		for (File f : currFileBatch) {
			LoadOneFile onefile = new LoadOneFile(flag, f);
			executor.execute(onefile);
			running.add(onefile);
		}

		// Shutdown all current threads
		executor.shutdown();
		try {
			while (!executor.isTerminated()) {
				if (isDone(running)) {
					executor.shutdownNow();
					//System.out.println("\n Terminated threads manually");
					break;
				}
			}
		} catch (Exception e) {
			System.out.println("Waiting error");
			executor.shutdownNow();
		}

		//Merge local hash map with global
		for (LoadOneFile o : running) {
			for (Integer i : o.var1HashLocal.keySet()) {
				var1Hash.put(i, var1Hash.getOrDefault(i, 0) + o.var1HashLocal.get(i));
			}
			for (Integer i : o.var2HashLocal.keySet()) {
				var2Hash.put(i, var2Hash.getOrDefault(i, 0) + o.var2HashLocal.get(i));
			}
			for (Integer i : o.var3HashLocal.keySet()) {
				var3Hash.put(i, var3Hash.getOrDefault(i, 0) + o.var3HashLocal.get(i));
			}
		}
		// clean currFileBach
		currFileBatch.clear();
	}
}
