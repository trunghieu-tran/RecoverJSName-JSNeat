package singleVarResolution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import association.AssociationTokenVarVar;
import association.AssociationVarFunctionName;
import association.AssociationVarVar;
import mainRecover.FunctionInfo;
import parser.MainParser;
import utils.Constants;
import utils.FileIO;

public class SGData {
	private static int numberOfThread = Constants.numberOfThread;
	private static boolean enableFuncNameEdge = Constants.enableFuncNameEdge;

	public HashSet<StarGraph> sgSet = new HashSet<>();
	public HashSet<StarGraph> sgSetTesting = new HashSet<>();
	public HashSet<FunctionInfo> testFunctionSet = new HashSet<>();
	public HashMap<Integer, HashSet<StarGraph>> mapEdgeToGraphs = new HashMap<>();
	public HashMap<String, Integer> mapTrainFunctionName = new HashMap<>();
	public HashMap<String, Integer> mapTestFunctionName = new HashMap<>();
	public Set<String> nameSet = new HashSet<>();

	public AssociationVarFunctionName varFuncAssociation = new AssociationVarFunctionName();
	public AssociationVarVar varVarAssociation = new AssociationVarVar();
	public AssociationTokenVarVar tokenVarVarAssociation = new AssociationTokenVarVar();

	ArrayList<File[]> fileList = new ArrayList<>();

	int numOfFunction = -1;
	int numOfTestFunction = -1;
	boolean usingCacheFileList = false;
	int nTrainSg = 0;

	private String getFunctionName(String str) {
		String[] tmp = str.split("_");
		return tmp[tmp.length - 1];
	}

	private String getDirFromFile(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf("/"));
	}

	private String getFileName(String str) {
		String[] tmp = str.split("_");
		for (int i = 1; i < tmp.length - 1; ++i)
			if (tmp[i].length() > 0) return tmp[i];
		return "";
	}
	public void getTestData(String sgDir, int numOfTest) throws IOException {
		this.numOfTestFunction = numOfTest;
		if (Constants.usingDataFromTraining)
			getTestDataFromTraining();
		else
			getTestDataFromTesting(sgDir);
	}

	public void getTrainingData(String path, int numOfFunction, String trainFileList, boolean usingCacheFileList) {
		System.out.println("Started getData training");
		this.numOfFunction = numOfFunction;
		this.usingCacheFileList = usingCacheFileList;

		if (Constants.usingDataFromTraining)
			getTrainingDataFor10Folds(path, trainFileList);
		else
			readDataFromFile_MultiThread(path, trainFileList);
	}

	private void getTestDataFromTesting(String sgDir) throws IOException{
		int cnt = 0;
		int cntSg = 0;
		//Structure of root: root --> Dir (Function) --> File (var-name)
		File root = new File(sgDir);

		for ( File dir : root.listFiles())
		{
			FunctionInfo fi = new FunctionInfo(dir.getCanonicalPath());
			File[] sgFiles = dir.listFiles();
			if (sgFiles == null || sgFiles.length >= 20) continue;
			for (File f : sgFiles) {
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

				String functionN = getFunctionName(functionName);
				if (!Objects.equals(functionN, "")) {
					mapTestFunctionName.put(functionN, mapTestFunctionName.getOrDefault(functionN, 0) + 1);
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

				// ADD function name
				if (enableFuncNameEdge) {
					if (!Objects.equals(functionN, "")) {
						Edge funcE = new Edge(functionN, "varFunction", 1);
						edges.add(funcE);
					}
				}

				fi.setFuncName(functionN);

				StarGraph sg = new StarGraph(edges, varName + "-" + hashCode);
				if (!edges.isEmpty()) {
					fi.addSG(sg);
					sgSetTesting.add(sg);
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

	private void getTrainingFileList(String sgDir, String trainFileList) {
		ArrayList<File> currFile = new ArrayList<>();
		File root = new File(sgDir);

		if (!usingCacheFileList) {
			int stc = 200000;
			for (File dir : root.listFiles()) {
				fileList.add(dir.listFiles());

				nTrainSg += fileList.get(fileList.size() - 1).length;

				if (nTrainSg > stc) {
					System.out.print(" >>> " + Integer.toString(nTrainSg));
					stc += 200000;
				}
			}
		} else {
			String fileListStr = FileIO.readStringFromFile(trainFileList);
			String[] filesSplit = fileListStr.split("\n");
			int last = 1;
			int stc = 200000;
			for (String line : filesSplit) {
				try {
					String[] p = line.split(" ");
					File f = new File(p[0]);
					int numOfFunc = Integer.parseInt(p[1]);

					if (numOfFunc != last) {
						File[] currF = currFile.toArray(new File[currFile.size()]);
						fileList.add(currF);

						currFile = new ArrayList<>();
						currFile.add(f);
						last = numOfFunc;
					} else {
						currFile.add(f);
					}

					if (++nTrainSg > stc) {
						System.out.print(" >>> " + Integer.toString(nTrainSg));
						stc += 200000;
					}
				} catch (Exception e) {

				}
			}
			if (currFile.size() > 0) {
				File[] currF = currFile.toArray(new File[currFile.size()]);
				fileList.add(currF);
			}
		}
		System.out.println();
		System.out.println(nTrainSg);
		System.out.println(fileList.size());
	}

	private void addInfoVarVarAssociation(File[] dir) {
		// for var-var association
		for (int i = 0; i < dir.length; ++i)
			for (int j = i + 1; j < dir.length; ++j) {
				try {
					String[] tmp1 = dir[i].getName().split("\\.");
					String[] tmp2 = dir[j].getName().split("\\.");
					if (Constants.usingTokenizedVarName)
						tokenVarVarAssociation.addInfo(tmp1[0], tmp2[0], "");
					else
						varVarAssociation.addInfo(tmp1[0], tmp2[0], "");
				} catch (Exception e) {

				}
			}
	}
	private void getTrainingDataFor10Folds(String sgDir, String trainFileList){
		System.out.println("getTrainingDataFor10Folds");
		int cnt = 0;
		ExecutorService executor = Executors.newFixedThreadPool(numberOfThread);
		ArrayList<ReadingGraph> rgs = new ArrayList<>();
		StringBuilder sbFileList = new StringBuilder();

		getTrainingFileList(sgDir, trainFileList);

		int cTotal = 0;
		int cTotalFile = Constants.startIndexInTraining;

		String lastFile = "";
		for (File[] dir : fileList)
		{
			++cTotal;
			if (dir.length == 0 || dir.length >= 20) continue;
			try {
				String currFile = getFileNameFromFilePath(dir[0].getCanonicalPath());
				if (!currFile.equals(lastFile)) {
					++cTotalFile;
					lastFile = currFile;
				}
				if (cTotalFile % 10 == 0) continue;

				addInfoVarVarAssociation(dir);

				for (File f : dir) {
					try {
						if (!usingCacheFileList) {
							sbFileList.append(f.getCanonicalPath()).append(" ").append(cTotal).append("\n");
						}

						ReadingGraph rg = new ReadingGraph(f);
						executor.execute(rg);
						rgs.add(rg);

						if (++cnt % 100000 == 0)
							System.out.print(" >>> [" + Integer.toString(cnt) + "/" + Integer.toString(nTrainSg) + "]");

						if (cnt == numOfFunction)
							break;
					}
					catch (Exception e) {

					}
				}
				if (cnt == numOfFunction)
					break;
			} catch (IOException e) {
				continue;
			}

		}
		System.out.println();
		if (!usingCacheFileList) {
			FileIO.writeStringToFile(trainFileList, sbFileList.toString());
			System.out.println("DONE Generated training fileList at : " + trainFileList);
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

		// Merge data
		for (ReadingGraph rg : rgs) {
			if (rg.sg == null) continue;
			sgSet.add(rg.sg);
			for (String key : rg.mapTrainFunctionNameLocal.keySet()) {
				mapTrainFunctionName.put(key, mapTrainFunctionName.getOrDefault(key, 0) + 1);
			}
			varFuncAssociation.mergeDataFrom(rg.varFuncAssociationLocal);
			nameSet.add(rg.sg.getVarName());
		}

		// Re-check set
		long sumEdge = 0;
		System.out.println("nTraining File = " + Integer.toString(cTotalFile * 9 / 10));
		System.out.println("nFunction size = " + Integer.toString(cTotal));
		System.out.println("nName unique = " + Integer.toString(nameSet.size()));
		System.out.println("nStargraph  = " + Integer.toString(sgSet.size()));
		for (StarGraph sg : sgSet)
			sumEdge += sg.getSizeGraph();
		System.out.println("Total edges = " + Long.toString(sumEdge));
	}

	private void getTestDataFromTraining() {
		int cnt = 0;
		int cntSg = 0;
		String lastFile = "";
		int cTotalFile = Constants.startIndexInTraining;
		for (File[] dir : fileList) {
			if (dir.length == 0 || dir.length >= 20) continue;
			try {
				String currFile = getFileNameFromFilePath(dir[0].getCanonicalPath());
				if (!currFile.equals(lastFile)) {
					++cTotalFile;
					lastFile = currFile;
				}
				if (cTotalFile % 10 != 0) continue;

				FunctionInfo fi = new FunctionInfo(getDirFromFile(dir[0].getCanonicalPath()));

				for (File f : dir) {
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

					String functionN = getFunctionName(functionName);
					if (!Objects.equals(functionN, "")) {
						mapTestFunctionName.put(functionN, mapTestFunctionName.getOrDefault(functionN, 0) + 1);
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

					// ADD function name
					if (enableFuncNameEdge) {
						if (!Objects.equals(functionN, "")) {
							Edge funcE = new Edge(functionN, "varFunction", 1);
							edges.add(funcE);
						}
					}

					fi.setFuncName(functionN);

					StarGraph sg = new StarGraph(edges, varName + "-" + hashCode);
					if (!edges.isEmpty()) {
						fi.addSG(sg);
						sgSetTesting.add(sg);
					}
					br.close();
				}
				this.testFunctionSet.add(fi);
				cntSg += fi.getStarGraphsList().size();
				if (++cnt == numOfTestFunction) break;

			} catch (IOException e) {

			}
		}
		System.out.println("Number of Testing function = " + Integer.toString(testFunctionSet.size()));
		System.out.println("Number of Stargraph in testing functions = " + Integer.toString(cntSg));
	}



	public class ReadingGraph implements Runnable {
		File f;
		StarGraph sg;
		HashMap<String, Integer> mapTrainFunctionNameLocal = new HashMap<>();
		AssociationVarFunctionName varFuncAssociationLocal = new AssociationVarFunctionName();

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

				String functionN = getFunctionName(functionName);
				if (!Objects.equals(functionN, "")) {
					mapTrainFunctionNameLocal.put(functionN, mapTrainFunctionNameLocal.getOrDefault(functionN, 0) + 1);
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

				// ADD function name
				if (enableFuncNameEdge) {
					if (!Objects.equals(functionN, "")) {
						Edge funcE = new Edge(functionN, "varFunction", 1);
						edges.add(funcE);
					}
				}
				// Add association between varname and functionname
				varFuncAssociationLocal.addInfo(varName, functionN, Constants.usingTokenizedFunctionName);

				sg = new StarGraph(edges, varName + "-" + hashCode);
				br.close();
			} catch (Exception e) {
//				e.printStackTrace();
			}
		}
	}

	private void readDataFromFile_MultiThread(String sgDir, String trainFileList) {
		System.out.println("readDataFromFile_MultiThread");
		int cnt = 0;
		ExecutorService executor = Executors.newFixedThreadPool(numberOfThread);
		ArrayList<ReadingGraph> rgs = new ArrayList<>();
		StringBuilder sbFileList = new StringBuilder();

		int cTotal = 0;

		getTrainingFileList(sgDir, trainFileList);

		for (File[] dir : fileList)
		{
			++cTotal;

			addInfoVarVarAssociation(dir);

			for (File f : dir) {
				try {
					if (!usingCacheFileList) {
						sbFileList.append(f.getCanonicalPath()).append(" ").append(cTotal).append("\n");
					}

					ReadingGraph rg = new ReadingGraph(f);
					executor.execute(rg);
					rgs.add(rg);

					if (++cnt % 100000 == 0)
						System.out.print(" >>> [" + Integer.toString(cnt) + "/" + Integer.toString(nTrainSg) + "]");

					if (cnt == numOfFunction)
						break;
				}
				catch (Exception e) {

				}
			}
			if (cnt == numOfFunction)
				break;
		}
		System.out.println();
		if (!usingCacheFileList) {
			FileIO.writeStringToFile(trainFileList, sbFileList.toString());
			System.out.println("DONE Generated training fileList at : " + trainFileList);
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

		// Merge data
		for (ReadingGraph rg : rgs) {
			if (rg.sg == null) continue;
			sgSet.add(rg.sg);
			for (String key : rg.mapTrainFunctionNameLocal.keySet()) {
				mapTrainFunctionName.put(key, mapTrainFunctionName.getOrDefault(key, 0) + 1);
			}
			varFuncAssociation.mergeDataFrom(rg.varFuncAssociationLocal);
			nameSet.add(rg.sg.getVarName());
		}

		// Re-check set
		long sumEdge = 0;
		System.out.println("nFunction size = " + Integer.toString(cTotal));
		System.out.println("nName unique = " + Integer.toString(nameSet.size()));
		System.out.println("nStargraph  = " + Integer.toString(sgSet.size()));
		for (StarGraph sg : sgSet)
			sumEdge += sg.getSizeGraph();
		System.out.println("Total edges = " + Long.toString(sumEdge));
	}

	private String getFileNameFromFilePath(String fileName) {
		try {
			String[] tmp = fileName.split("/");
			String[] tmp2 = tmp[5].split("_");
			return tmp2[1];
		} catch (Exception e) {
			return "";
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

	public String Analyzing_TrainingSet(HashSet<StarGraph> sgSet) {
		StringBuilder sb = new StringBuilder();
		sb.append("Number of Training Stargraphs : ").append(sgSet.size()).append("\n");
		sb.append("nName unique in Training data = ").append(nameSet.size()).append("\n");

		int totalEdges = 0;
		int[] countSGbyNumofEdge = new int[12];
		HashMap<String, Integer> relMapEdge = new HashMap<>();
		HashMap<String, Integer> relMapGraph = new HashMap<>();
		HashSet<Integer> setEdge = new HashSet<>();
		for (StarGraph sg : sgSet) {
			if (sg.getSizeGraph() <= 10)  countSGbyNumofEdge[sg.getSizeGraph()]++;
			else countSGbyNumofEdge[11]++;

			totalEdges += sg.getSizeGraph();
			Set<String> setRel = new HashSet<>();
			for (Edge e : sg.getEdges()) {
				relMapEdge.put(e.getRel(), relMapEdge.getOrDefault(e.getRel(), 0) + 1);
				setRel.add(e.getRel());
				setEdge.add(e.hashCode);
			}
			for (String str : setRel)
				relMapGraph.put(str, relMapGraph.getOrDefault(str, 0) + 1);
		}
		sb.append("Number of edges : ").append(totalEdges).append("\n");
		sb.append("Number of unique edges : ").append(setEdge.size()).append("\n").append("\n");


		for (int i = 1; i <= 10; ++i)
			sb.append("Number of stargraph ").append(i).append(" edges : ").append(countSGbyNumofEdge[i]).append("\n");
		sb.append("Number of stargraph ").append("> 10 edges : ").append("\n").append("\n");

		sb.append("Relations by Edges: ").append("\n");
		for (String key : relMapEdge.keySet()) {
			sb.append(key).append(" ").append(relMapEdge.get(key)).append("\n");
		}
		sb.append("\n").append("Relations by Graphs: ").append("\n");
		for (String key : relMapGraph.keySet()) {
			sb.append(key).append(" ").append(relMapGraph.get(key)).append("\n");
		}
		return sb.toString();
	}
}
