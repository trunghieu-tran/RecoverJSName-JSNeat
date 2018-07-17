package singleVarResolution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import mainRecover.FunctionInfo;
import parser.MainParser;

public class SGData {
	private static final int numberOfThread = 20;

	public HashSet<StarGraph> sgSet = new HashSet<>();
	public HashSet<FunctionInfo> testFunctionSet = new HashSet<>();
	public HashMap<Integer, HashSet<StarGraph>> mapEdgeToGraphs = new HashMap<>();
	private int numOfFunction = -1;
	private int numOfTestFunction = -1;

	public void getTestDataJSNice(String sgDir, int numOfTest) throws IOException {
		this.numOfTestFunction = numOfTest;
		int cnt = 0;
		int cntSg = 0;
//		int cTotal = 0;
		//Structure of root: root --> Dir (Function) --> File (var-name)
		File root = new File(sgDir);
		for ( File dir : root.listFiles())
		{
//			++cTotal;
//			if (cTotal % 10 != 0) continue;

			FunctionInfo fi = new FunctionInfo(dir.getCanonicalPath());
			for (File f : dir.listFiles()) {
				//for each file name = variable Name
				String path = f.getCanonicalPath();
//				System.out.println(path);
				String functionName = "", varName = "";

//				if (path.indexOf("\\") != -1) {
//					functionName = path.substring(path.indexOf("Data") + 5, path.lastIndexOf("\\"));
//					varName = path.substring(path.lastIndexOf("\\") + 1, path.indexOf(".txt"));
//				} else {
//					functionName = path.substring(path.indexOf("Data") + 5, path.lastIndexOf("/"));
//					varName = path.substring(path.lastIndexOf("/") + 1, path.indexOf(".txt"));
//				}

				if ( path.indexOf("\\") != -1) {
					functionName = path.substring(path.indexOf("JSNiceTestSet")+14, path.lastIndexOf("\\"));
					varName = path.substring(path.lastIndexOf("\\")+1, path.indexOf(".txt"));
				} else {
					functionName = path.substring(path.indexOf("JSNiceTestSet")+14, path.lastIndexOf("/"));
					varName = path.substring(path.lastIndexOf("/")+1, path.indexOf(".txt"));
				}

				int hashCode = Objects.hash(functionName);
				//read file content --> edges
				BufferedReader br = new BufferedReader(new FileReader(f));
				String st;
				HashSet<Edge> edges = new HashSet<>();
				while ((st = br.readLine()) != null) {
					String[] subs = st.split(" ");
					Edge e = new Edge(subs[0], subs[1], Integer.valueOf(subs[2]));
					edges.add(e);
				}
				StarGraph sg = new StarGraph(edges, varName + "-" + hashCode);
				if (!edges.isEmpty()) {
					fi.addSG(sg);
				}
				br.close();
			}
			this.testFunctionSet.add(fi);
			cntSg += fi.getStarGraphsList().size();
			if (++cnt == numOfTestFunction) break;
		}
		System.out.println("Number of Testing function = " + Integer.toString(testFunctionSet.size()));
		System.out.println("Number of Stargraph in testing functions = " + Integer.toString(cntSg));
	}
	public void getTestData(String sgDir, int numOfTest) throws IOException {
		this.numOfTestFunction = numOfTest;
		int cnt = 0;
		int cntSg = 0;
		int cTotal = 1;
		//Structure of root: root --> Dir (Function) --> File (var-name)
		File root = new File(sgDir);
		for ( File dir : root.listFiles())
		{
			++cTotal;
			if (cTotal % 900 != 0) continue;

			FunctionInfo fi = new FunctionInfo(dir.getCanonicalPath());
			for (File f : dir.listFiles()) {
				//for each file name = variable Name
				String path = f.getCanonicalPath();
//				System.out.println(path);
				String functionName = "", varName = "";

				if (path.indexOf("\\") != -1) {
					functionName = path.substring(path.indexOf("Data") + 5, path.lastIndexOf("\\"));
					varName = path.substring(path.lastIndexOf("\\") + 1, path.indexOf(".txt"));
				} else {
					functionName = path.substring(path.indexOf("Data") + 5, path.lastIndexOf("/"));
					varName = path.substring(path.lastIndexOf("/") + 1, path.indexOf(".txt"));
				}


				int hashCode = Objects.hash(functionName);
				//read file content --> edges
				BufferedReader br = new BufferedReader(new FileReader(f));
				String st;
				HashSet<Edge> edges = new HashSet<>();
				while ((st = br.readLine()) != null) {
					String[] subs = st.split(" ");
					Edge e = new Edge(subs[0], subs[1], Integer.valueOf(subs[2]));
					edges.add(e);
				}
				StarGraph sg = new StarGraph(edges, varName + "-" + hashCode);
				if (!edges.isEmpty()) {
					fi.addSG(sg);
				}
				br.close();
			}
			this.testFunctionSet.add(fi);
			cntSg += fi.getStarGraphsList().size();
			if (++cnt == numOfTestFunction) break;
		}
		System.out.println("Number of Testing function = " + Integer.toString(testFunctionSet.size()));
		System.out.println("Number of Stargraph in testing functions = " + Integer.toString(cntSg));
	}
	
	public void getData(String path, int numOfFunction) {
		System.out.println("getData sgData");
		this.numOfFunction = numOfFunction;

		MainParser main = new MainParser();
		//Get data directly from parser
		if ( path.isEmpty() ) {
//			try {
//				main.parseTrainSetForest();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			sgSet = main.sgSet;
		}
		//Read data from previous parse
		else { 
//			sgSet = readDataFromFile(path);
			readDataFromFile_MultiThread2(path);
		}
	}

	public class ReadingGraph implements Runnable {
		File f;
		StarGraph sg;
		public ReadingGraph(File f) {
			this.f = f;
		}
		public void run() {
			try {
				//for each file name = variable Name
				String path = f.getCanonicalPath();
				String functionName = "", varName = "";
				if (path.indexOf("\\") != -1) {
					functionName = path.substring(path.indexOf("Data") + 5, path.lastIndexOf("\\"));
					varName = path.substring(path.lastIndexOf("\\") + 1, path.indexOf(".txt"));
				} else {
					functionName = path.substring(path.indexOf("Data") + 5, path.lastIndexOf("/"));
					varName = path.substring(path.lastIndexOf("/") + 1, path.indexOf(".txt"));
				}

				int hashCode = Objects.hash(functionName);
				//read file content --> edges
				BufferedReader br = new BufferedReader(new FileReader(f));
				String st;
				HashSet<Edge> edges = new HashSet<>();
				while ((st = br.readLine()) != null) {
					String[] subs = st.split(" ");
					Edge e = new Edge(subs[0], subs[1], Integer.valueOf(subs[2]));
					edges.add(e);
				}
				// TODO - hashcode can be empty
				sg = new StarGraph(edges, varName + "-" + hashCode);
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void readDataFromFile_MultiThread2(String sgDir) {
		System.out.println("readDataFromFile_MultiThread");
		int cnt = 0;
		ExecutorService executor = Executors.newFixedThreadPool(numberOfThread);
		ArrayList<ReadingGraph> rgs = new ArrayList<>();

		File root = new File(sgDir);
		int cTotal = 1;
		for ( File dir : root.listFiles())
		{
			++cTotal;
			if (cTotal % 10 == 0) continue;

 			for (File f : dir.listFiles()) {
			    try {
				    ReadingGraph rg = new ReadingGraph(f);
				    executor.execute(rg);
				    rgs.add(rg);

				    if (++cnt % 100000 == 0)
					    System.out.println("[" + Integer.toString(cnt) + "/" + Integer.toString(7973823) + "] >>> LOADED: " + f.getCanonicalPath());

				    if (cnt == numOfFunction)
					    break;
			    } catch (IOException e) {
				    e.printStackTrace();
			    }
			}
			if (cnt == numOfFunction)
				break;
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
		System.out.println("FINISHED all threads for corpus loading");

		for (ReadingGraph rg : rgs) {
			sgSet.add(rg.sg);
		}

		// Re-check set
		long sumEdge = 0;
		System.out.println("nFunction size = " + Integer.toString(cTotal));
		System.out.println("SgSET size = " + Integer.toString(sgSet.size()));
		for (StarGraph sg : sgSet)
			sumEdge += sg.getSizeGraph();
		System.out.println("Total edges = " + Long.toString(sumEdge));
	}

	private void readDataFromFile_MultiThread(String sgDir) {
		System.out.println("readDataFromFile_MultiThread");
		File dir = new File(sgDir);
		ArrayList<File> files = new ArrayList<>();
		searchDir(dir, files);
		System.out.println("nFiles = " + Integer.toString(files.size()));
		int cnt = 0;
		ExecutorService executor = Executors.newFixedThreadPool(numberOfThread);
		ArrayList<ReadingGraph> rgs = new ArrayList<>();
		for( File f: files ) {
			try {
				ReadingGraph rg = new ReadingGraph(f);
				executor.execute(rg);
				rgs.add(rg);

				if (++cnt % 10000 == 0)
					System.out.println("[" + Integer.toString(cnt) + "/" + Integer.toString(files.size()) + "] >>> LOADED: " + f.getCanonicalPath());

				if (cnt == numOfFunction)
					break;
			} catch (IOException e) {
				e.printStackTrace();
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
		System.out.println("FINISHED all threads for corpus loading");

		for (ReadingGraph rg : rgs) {
			sgSet.add(rg.sg);
		}

		// Re-check set
		long sumEdge = 0;
		System.out.println("SgSET size = " + Integer.toString(sgSet.size()));
		for (StarGraph sg : sgSet)
			sumEdge += sg.getSizeGraph();
		System.out.println("Total edges = " + Long.toString(sumEdge));
	}

	private HashSet<StarGraph> readDataFromFile(String sgDir) {
		//Find all files
		File dir = new File(sgDir);
		ArrayList<File> files = new ArrayList<>();
		searchDir(dir, files);
		int cnt = 0;
		for( File f: files )
		{
			String path = "";
			try {
				//for each file name = variable Name
				path = f.getCanonicalPath();
				String functionName = "", varName = "";
				if ( path.indexOf("\\") != -1) {
					functionName = path.substring(path.indexOf("Data")+5, path.lastIndexOf("\\"));
					varName = path.substring(path.lastIndexOf("\\")+1, path.indexOf(".txt"));
				} else {
					functionName = path.substring(path.indexOf("Data")+5, path.lastIndexOf("/"));
					varName = path.substring(path.lastIndexOf("/")+1, path.indexOf(".txt"));
				}

				int hashCode = Objects.hash(functionName);
				//read file content --> edges
				BufferedReader br = new BufferedReader(new FileReader(f));
				String st;
				HashSet<Edge> edges = new HashSet<>();
				while ((st = br.readLine()) != null) {
					String[] subs = st.split(" ");
					Edge e = new Edge(subs[0], subs[1], Integer.valueOf(subs[2]));
					edges.add(e);
				}
				// TODO - hashcode can be empty
				StarGraph sg = new StarGraph(edges, varName + "-" + hashCode);
				sgSet.add(sg);
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (++cnt % 10000 == 0)
			System.out.println("[" + Integer.toString(cnt) + "/" + Integer.toString(files.size()) + "] >>> LOADED: " + path);

			if (cnt == numOfFunction)
				break;
		}

//		for ( StarGraph sg: sgSet )
//		{
//			System.out.println(sg.getVarName());
//			for( Edge e: sg.getEdges() ) {
//				System.out.println(e.toString());
//			}
//		}
		System.out.println("DONE loading corpus data!!!!");
		return sgSet;
	}
	
	public void searchDir(File dir, ArrayList<File> files) {
		if ( dir.isFile() )
		{
			files.add(dir);
			return;
		}
		for ( File file : dir.listFiles() )
		{
			if ( file.isDirectory() )
			{
				searchDir(file,files);
			}
			else 
			{
				files.add(file);
			}
		}
	}	

	public void IndexingGraphByEdges() {
		for (StarGraph sg : sgSet) {
			for (Edge e : sg.getEdges()) {
				if (mapEdgeToGraphs.containsKey(e.hashCode))
					mapEdgeToGraphs.get(e.hashCode).add(sg);
				else {
					HashSet<StarGraph> tmp = new HashSet<>();
					tmp.add(sg);
					mapEdgeToGraphs.put(e.hashCode, tmp);
				}
			}
		}
		System.out.println("Size of mapEdgeToGraph = " + Integer.toString(mapEdgeToGraphs.size()));
	}
}
