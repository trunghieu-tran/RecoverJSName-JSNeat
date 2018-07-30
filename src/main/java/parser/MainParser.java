package parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.apache.commons.io.FileUtils;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.IRFactory;
import org.mozilla.javascript.ast.AstRoot;

import singleVarResolution.Edge;
import singleVarResolution.StarGraph;
import singleVarResolution.StarGraphToPrint;
/**
 * @author Mike Tran
 * Main program. 
 * Generate file lists from data into training and testing.
 * Parse each file to build corpus.
 */
public class MainParser {
	static String all = "../Data/";
	//	static String all = "F:\\Study\\Research\\RecoverJsName\\Data\\0xsky\\xblog\\xblogroot\\admin\\js\\admin.js";
	//	static String all = ".\\resources\\_test";
	//	static String output = "../Data/_Output";
	//	static String output = ".\\resources/parsedData";
	//	static String testSetDir = ".\\resources/parsedData/testSet";
	static String assocTrainDir = "../AssocData";
	static String assocTestDir = "../AssocTestData";
	static String trainSetDir = "../TrainSet";
	static String testSetDir = "../TestSet";
	static String bakerDir = "../BakerData";
	public static String sgTrainDir = "../StarGraphData";
	public static String sgTestDir = "../StarGraphTestData";
	static String trainTMDir = "../TrainTM";
	static String fileList = "../FileList";
	//	ArrayList<File> testSet = new ArrayList<>();
	//	ArrayList<File> trainSet = new ArrayList<>();
	int countFile; 
	public HashSet<Integer> fileHashSet = new HashSet<>();
	public HashSet<StarGraphToPrint> sgSet = new HashSet<>();

	public static void main(String[] args) throws Exception {
		MainParser demo = new MainParser();
		//demo.generateFileList("../CheckDupData");
		//demo.generateTestSetListJSNice("../JSNiceData");
//		demo.parseForest("");
		demo.parseAssociation("");
		//demo.parseBaker();
		//		demo.parseTestSet();
		//		demo.parseTrainSetTM();
		//		demo.parseTestSetTM();
	}

	public void parseAssociation(String flag) throws IOException {
		String fileType = "", output = "";
		if ( flag.equals("train")) {
			fileType = "gitTrainFileList.txt";
			output = assocTrainDir;
		} else if ( flag.equals("test")) {
			fileType = "testFileList.txt";
			output = assocTestDir;
		} else {
			fileType = "test.txt";
			output = "../debugAssoc";
		}
		File fileListing = new File(fileList + "/" + fileType);
		if ( fileListing.exists() )
		{
			int count = 0;
			List<String> lines = FileUtils.readLines(fileListing, "UTF-8");
			for ( String str: lines)
			{
				count++;
				//if ( count > 10 ) break;
				if ( count % 500 == 0 ) {
					System.out.println("Processed " + count + " files");
				}
				try
				{
					//System.out.println(str);
					CompilerEnvirons env = new CompilerEnvirons();
					env.setRecoverFromErrors(true);
					FileReader strReader = new FileReader(str);
					IRFactory factory = new IRFactory(env, new JSErrorReporter());
					//String path = str.substring(str.indexOf("Data") + 4, str.lastIndexOf(".js"));
					String path = str.substring(str.indexOf("Data") + 5, str.lastIndexOf(".js"));
					String projectName = path.substring(0, path.indexOf("/"));
					String fileName = path.substring(path.lastIndexOf("/")+1);
					path = "/" + projectName + "_" + fileName; 
					path = output + path;
					//					path = "../TestRun" + path;
					AssociateVisitor ascVis = new AssociateVisitor(path);
					AstRoot rootNode = factory.parse(strReader, null, 0);
					rootNode.visit(ascVis);
				}

				catch (Exception e)
				{
					//System.out.println("Exception :" + e.getMessage());
					//e.printStackTrace();
					continue;
				}
			}
		}
	}

	public void generateFileList (String filePath) throws Exception
	{
		File trainFileList = new File(fileList + "/dupTrainFileList.txt");
		FileWriter fwTrainList = new FileWriter(trainFileList);
		PrintWriter pwTrainList = new PrintWriter(fwTrainList);

		File testFileList = new File(fileList + "/dupTestFileList.txt");
		FileWriter fwTestList = new FileWriter(testFileList);
		PrintWriter pwTestList = new PrintWriter(fwTestList);

		File dir = new File(filePath);
		ArrayList<File> files = new ArrayList<>();
		searchDir(dir, files);
		//File[] files = dir.listFiles();
		int i = 1;

		for ( File file : files )
		{
			if ( i < 10 ) {
				//trainSet.add(file);
				pwTrainList.println(file.getCanonicalPath());
				i++;
			}
			else {
				i = 1;
				//testSet.add(file);
				pwTestList.println(file.getCanonicalPath());
			}
		}
		//myVisitor.print();
		//myVisitor.printToFile(output);
		pwTrainList.close();
		pwTestList.close();
	}

	public void generateTestSetListJSNice (String filePath) throws Exception
	{
		File testFileList = new File(fileList + "/testJSNiceList.txt");
		FileWriter fwTestList = new FileWriter(testFileList);
		PrintWriter pwTestList = new PrintWriter(fwTestList);

		File dir = new File(filePath);
		ArrayList<File> files = new ArrayList<>();
		searchDir(dir, files);
		//File[] files = dir.listFiles();
		for ( File file : files )
		{
			pwTestList.println(file.getCanonicalPath());
		}
		pwTestList.close();
	}

	public void parseBaker() throws Exception
	{
		File trainFileList = new File(fileList + "/trainFileList.txt");
		//		File trainFileList = new File(fileList + "/test.txt");
		CompilerEnvirons env = new CompilerEnvirons();
		env.setRecoverFromErrors(true);
		BakerVisitor myVisitor = new BakerVisitor();
		int count = 0;
		if ( trainFileList.exists() )
		{
			List<String> lines = FileUtils.readLines(trainFileList, "UTF-8");
			for ( String str: lines)
			{
				count++;
				if ( count > 100 ) break;
				System.out.println(str);
				try
				{
					FileReader strReader = new FileReader(str);
					IRFactory factory = new IRFactory(env, new JSErrorReporter());
					AstRoot rootNode = factory.parse(strReader, null, 0);
					rootNode.visit(myVisitor);
				}
				catch (Exception e)
				{
					System.out.println(e.getMessage());
					continue;
				}
			}
			myVisitor.printToFile(bakerDir);
			//myVisitor.printToFile(trainSetDir);
		}
	}

	public void parseForest(String flag) throws IOException {
		int countProbFile = 0;
		int count = 0;
		String fileType = "", sgPath = "", outputDir ="";
		if ( flag.equals("train")) {
			fileType = "gitTrainFileList.txt";
			sgPath = "../GitTrainData";
			outputDir = "../GitTrainData";
		} else if ( flag.equals("test")) {
			fileType = "gitTestFileList.txt";
			sgPath = "../GitTestData";
			outputDir = "../GitTestData";
		} else if ( flag.equals("jsnice")) {
			fileType = "testJSNiceList.txt";
			sgPath = "../JSNiceTestSet";
			outputDir = "";
		} else {
			fileType = "test.txt";
			sgPath = "../TestRun";
			outputDir = "../TestRun";
		}
		File fileListing = new File(fileList + "/" + fileType);
		if ( fileListing.exists() )
		{
			List<String> lines = FileUtils.readLines(fileListing, "UTF-8");
			for ( String str: lines)
			{
				count++;
				//if ( count > 10 ) break;
				if ( count % 500 == 0) {
					System.out.println("Processed " + count + " files");
				}
				try
				{
					CompilerEnvirons env = new CompilerEnvirons();
					env.setRecoverFromErrors(true);
					FileReader strReader = new FileReader(str);
					IRFactory factory = new IRFactory(env, new JSErrorReporter());
					String path = str.substring(str.indexOf("Data") + 5, str.lastIndexOf(".js"));
					String projectName = "", fileName = "";
					if ( path.indexOf("\\") > 0 ) { //Windows
						projectName = path.substring(0, path.indexOf("\\"));
						fileName = path.substring(path.lastIndexOf("\\")+1);
					} else {  //Linux
						projectName = path.substring(0, path.indexOf("/"));
						fileName = path.substring(path.lastIndexOf("/")+1);
					}
					path = "/" + projectName + "_" + fileName; 
					String sgDir = sgPath + path;
					path = outputDir + path;
					//					path = "../TestRun" + path;
					ForestVisitor myVisitor = new ForestVisitor(path, flag);
					myVisitor.getSgPath(sgDir);
					AstRoot rootNode = factory.parse(strReader, null, 0);
					rootNode.visit(myVisitor);
					sgSet.addAll(myVisitor.getStarForest());
				}

				catch (Exception e)
				{
					countProbFile++;
					System.out.println("File " + str + " can't be parsed");
					e.printStackTrace();
					continue;
				}
			}
		}
		//		for ( StarGraph sg: sgSet )
		//		{
		//			System.out.println(sg.getVarName());
		//			for( Edge e: sg.getEdges() ) {
		//				System.out.println(e.toString());
		//			}
		//		}
		System.out.println("Finish parsing. Success: " + (count - countProbFile) + " .Fail:  " + countProbFile);
	}

	public void parseTestSet() throws Exception
	{
		//File to record test set
		File testFileList = new File(testSetDir + "/fileList.txt");
		//		File testFileList = new File(testSetDir + "/test.txt");
		if ( testFileList.exists() )
		{
			List<String> lines = FileUtils.readLines(testFileList, "UTF-8");
			for ( String str: lines)
			{
				try
				{
					CompilerEnvirons env = new CompilerEnvirons();
					env.setRecoverFromErrors(true);
					FileReader strReader = new FileReader(str);
					IRFactory factory = new IRFactory(env, new JSErrorReporter());
					//String path = str.substring(str.indexOf("Data") + 4, str.lastIndexOf(".js"));
					String path = str.substring(str.indexOf("Data") + 5, str.lastIndexOf(".js"));
					String projectName = "", fileName = "";
					if ( path.indexOf("\\") > 0 ) { //Windows
						projectName = path.substring(0, path.indexOf("\\"));
						fileName = path.substring(path.lastIndexOf("\\")+1);
					} else {  //Linux
						projectName = path.substring(0, path.indexOf("/"));
						fileName = path.substring(path.lastIndexOf("/")+1);
					}
					
					path = "/" + projectName + "_" + fileName; 
					path = testSetDir + path;
					System.out.println(path);
					TestSetVisitor myVisitor = new TestSetVisitor(path);
					AstRoot rootNode = factory.parse(strReader, null, 0);
					rootNode.visit(myVisitor);
				}
				catch (Exception e)
				{
					System.out.println("Exception at " + e);
					continue;
				}
			}
		}
	}

	//For Topic Model training module
	public void parseTrainSetTM() throws Exception
	{
		//		File trainFileList = new File(trainSetDir + "/test.txt");
		File trainFileList = new File(trainSetDir + "/fileList.txt");
		if ( trainFileList.exists() )
		{
			CompilerEnvirons env = new CompilerEnvirons();
			env.setRecoverFromErrors(true);

			List<String> lines = FileUtils.readLines(trainFileList, "UTF-8");
			for ( String str: lines)
			{
				System.out.println(str);
				try
				{
					TMTrainSetVisitor myVisitor = new TMTrainSetVisitor();
					FileReader strReader = new FileReader(str);
					IRFactory factory = new IRFactory(env, new JSErrorReporter());
					AstRoot rootNode = factory.parse(strReader, null, 0);
					//Should separate into visitor for each function
					rootNode.visit(myVisitor);
					//					myVisitor.print();
					myVisitor.printToFile(trainTMDir);
				}
				catch (Exception e)
				{
					System.out.println("Exception at " + e);
					continue;
				}
			}
			//myVisitor.printToFile(trainSetDir);
		}
	}

	public void parseTestSetTM() throws IOException {

		//File to record test set
		File testFileList = new File(testSetDir + "/fileList.txt");
		//		File testFileList = new File(testSetDir + "/test.txt");
		if ( testFileList.exists() )
		{
			List<String> lines = FileUtils.readLines(testFileList, "UTF-8");
			for ( String str: lines)
			{
				try
				{
					CompilerEnvirons env = new CompilerEnvirons();
					env.setRecoverFromErrors(true);
					FileReader strReader = new FileReader(str);
					IRFactory factory = new IRFactory(env, new JSErrorReporter());
					//String path = str.substring(str.indexOf("Data") + 4, str.lastIndexOf(".js"));
					String path = str.substring(str.indexOf("Data") + 5, str.lastIndexOf(".js"));
					String projectName = path.substring(0, path.indexOf("\\"));
					String fileName = path.substring(path.lastIndexOf("\\")+1);
					path = "/" + projectName + "_" + fileName; 
					path = testSetDir + path;
					System.out.println(path);
					TMTestSetVisitor myVisitor = new TMTestSetVisitor();
					AstRoot rootNode = factory.parse(strReader, null, 0);
					rootNode.visit(myVisitor);
					myVisitor.printToFile(path);
				}
				catch (Exception e)
				{
					System.out.println("Exception at " + e);
					continue;
				}
			}
		}
	}

	public void searchDir(File dir, ArrayList<File> files) throws IOException, NoSuchAlgorithmException {
		if ( dir.isFile() )
		{
			files.add(dir);
			return;
		}
		if ( dir == null || dir.listFiles() == null ) {
			return;
		}
		for ( File file : dir.listFiles() )
		{
			if ( file.isDirectory() )
			{
				searchDir(file,files);
			}
			else if ( file.getName().contains(".js") )
			{
				if ( !(file.getName().startsWith("._") || file.getName().contains(".min.js")) )
				{
					int lineNumber = 0;
					LineNumberReader countLineN0;
					try
					{
						FileReader input = new FileReader(file.getCanonicalPath());
						countLineN0 = new LineNumberReader(input);
						while (countLineN0.skip(Long.MAX_VALUE) > 0)
						{
							// Loop just in case the file is > Long.MAX_VALUE or skip() decides to not read the entire file
						}
						lineNumber = countLineN0.getLineNumber() + 1;  // +1 because line index starts at 0
						countLineN0.close();
					}
					
					catch (Exception e) {
						e.printStackTrace();
						continue;
					}
					
					if ( lineNumber > 10 ) {
				        String content = FileUtils.readFileToString(file, "UTF-8");
				        FileUtils.write(file, content, "UTF-8");
						boolean duplicate = false;
						try {
							duplicate = checkFileHash(file);
//							duplicate = checkFileContent(file, files);
						} catch (IOException e) {
							System.out.println("Can't hash the file");
							//e.printStackTrace();
						}
						if ( !duplicate ) {
							files.add(file);
							fileHashSet.add( (calcSHA1(file)).hashCode() );
							countFile++;
							if ( countFile % 500 == 0) {
								System.out.println("Added " + countFile + " files");
							}
						}
					} 
					else {
						try {
							file.delete();
						} catch (Exception e) {
							System.out.println("File can't be deleted");
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private boolean checkFileContent(File file, ArrayList<File> files) throws IOException {
		for ( File f : files ) {
			if ( FileUtils.contentEquals(file, f) ) {
				return true;
			}
		}
		return false;
	}

	private boolean checkFileHash(File file) throws IOException, NoSuchAlgorithmException {
		String hdigest = calcSHA1(file);
		if ( fileHashSet.contains(hdigest.hashCode() ) )
		{
			return true;
		}
		return false;
	}	
	
	/**
	 * Read the file and calculate the SHA-1 checksum
	 */
	private static String calcSHA1(File file) throws FileNotFoundException,
	        IOException, NoSuchAlgorithmException {

	    MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
	    try (InputStream input = new FileInputStream(file)) {

	        byte[] buffer = new byte[8192];
	        int len = input.read(buffer);

	        while (len != -1) {
	            sha1.update(buffer, 0, len);
	            len = input.read(buffer);
	        }

	        return new HexBinaryAdapter().marshal(sha1.digest());
	    }
	}
}
