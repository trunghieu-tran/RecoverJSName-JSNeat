package association;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utils.Tokenization;

public class TokenVarNameMiner {
	ConcurrentHashMap<Integer, Integer> token2Hash = new ConcurrentHashMap<>();
	ConcurrentHashMap<Integer, Integer> token1Hash = new ConcurrentHashMap<>();
	int n0Thread = 8;
	public static void main(String[] args) {
		String path = "../debugAssoc";
		TokenVarNameMiner tokenVar = new TokenVarNameMiner();
		try {
			tokenVar.loadAssocDataMultiThread(0, path);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//am.loadAssocDataSingleThread(0, path);
		tokenVar.writeHashAssoc("../HashAssocData");
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

	public void writeHashAssoc(String path) {
		File fileHash1 = new File(path + "/token1.txt");
		File fileHash2 = new File(path + "/token2.txt");
		
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
		
		for(Integer i : token1Hash.keySet()) {
			pw1.println(i + " " + token1Hash.get(i));
			//System.out.println(i + " " + var1Hash.get(i));
		}
		for(Integer i : token2Hash.keySet()) {
			pw2.println(i + " " + token2Hash.get(i));
			//System.out.println(i + " " + var2Hash.get(i));
		}
		pw1.close();
		pw2.close();
	}

	public class LoadOneFile implements Runnable {
		int flag;
		File file;
		boolean isLoaded = false;
		HashMap<Integer, Integer> token2HashLocal = new HashMap<>();
		HashMap<Integer, Integer> token1HashLocal = new HashMap<>();
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
							ArrayList<String> tokens = Tokenization.tokenize(subs[0]);
							for ( String token: tokens ) {
								int tokenHashCode = token.hashCode();
								token1HashLocal.put(tokenHashCode, 
										token1HashLocal.getOrDefault(tokenHashCode, 0) +1);
								varList.add(subs[0]);
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
					ArrayList<String> tokensI = Tokenization.tokenize(al.get(i));
					ArrayList<String> tokensJ = Tokenization.tokenize(al.get(j));
					for ( String tokenI : tokensI ) {
						for ( String tokenJ : tokensJ ) {
							int token2HashCode = (tokenI + tokenJ).hashCode();
							token2HashLocal.put(token2HashCode, 
									token2HashLocal.getOrDefault(token2HashCode, 0) +1);
						}
					}
					
				}
			}
			isLoaded = true;
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
			for (Integer i : o.token1HashLocal.keySet()) {
				token1Hash.put(i, token1Hash.getOrDefault(i, 0) + o.token1HashLocal.get(i));
			}
			for (Integer i : o.token2HashLocal.keySet()) {
				token2Hash.put(i, token2Hash.getOrDefault(i, 0) + o.token2HashLocal.get(i));
			}
		}
		// clean currFileBach
		currFileBatch.clear();
	}
}
