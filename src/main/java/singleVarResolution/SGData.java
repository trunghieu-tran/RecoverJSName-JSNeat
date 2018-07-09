package singleVarResolution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

import parser.MainParser;

public class SGData {
	public HashSet<StarGraph> sgSet = new HashSet<>();
	private int numOfFunction = -1;
	public void getData(String path, int numOfFunction) {
		this.numOfFunction = numOfFunction;

		MainParser main = new MainParser();
		//Get data directly from parser
		if ( path.isEmpty() ) {
			try {
				main.parseTrainSetForest();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sgSet = main.sgSet;
		}
		//Read data from previous parse
		else { 
			sgSet = readDataFromFile(path);
		}
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

}
