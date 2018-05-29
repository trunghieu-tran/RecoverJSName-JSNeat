package parser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.IRFactory;
import org.mozilla.javascript.ast.AstRoot;

public class MainParser {
	static String all = "../Data/";
//	static String all = "F:\\Study\\Research\\RecoverJsName\\Data\\0xsky\\xblog\\xblogroot\\admin\\js\\admin.js";
//	static String all = ".\\resources\\_test";
//	static String output = "../Data/_Output";
	static String output = ".\\resources/parsedData";
	static String testSetDir = ".\\resources/parsedData/testSet";
	public static void main(String[] args) throws Exception {
		MainParser demo = new MainParser();
		demo.generateDictionary(all);
		//demo.parseTestSet(testSet);
	}
	
	public void generateDictionary (String filePath) throws Exception
	{
		CompilerEnvirons env = new CompilerEnvirons();
		env.setRecoverFromErrors(true);
		MyVisitor myVisitor = new MyVisitor();
		File dir = new File(filePath);
		ArrayList<File> files = new ArrayList<>();
		searchDir(dir, files);
		//File[] files = dir.listFiles();
		int i = 1;
		ArrayList<File> testSet = new ArrayList<>();
		ArrayList<File> trainSet = new ArrayList<>();
		for ( File file : files )
		{
			if ( i < 10 ) {
//				trainSet.add(file);
//				System.out.println(file.getAbsolutePath());
//				FileReader strReader = new FileReader(file.getAbsolutePath());
//				IRFactory factory = new IRFactory(env, new JSErrorReporter());
//				try
//				{
//					AstRoot rootNode = factory.parse(strReader, null, 0);
//					rootNode.visit(myVisitor);
//				}
//				catch (Exception e)
//				{
//					System.out.println("Exception at " + e);
//					continue;
//				}
				i++;
			}
			else {
				i = 1;
				testSet.add(file);
			}
		}
		//myVisitor.print();
		//myVisitor.printToFile(output);
		
		//Generate Test set
		File testFileList = new File(testSetDir + "/fileList.txt");
		FileWriter fwTestList = new FileWriter(testFileList);
	    PrintWriter pwTestList = new PrintWriter(fwTestList);
	    for (File file : testSet)
	    {
	    	pwTestList.println(file.getCanonicalPath());
	    }
	    pwTestList.close();
	}

	public void parseTestSet (String filePath) throws Exception
	{
		
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
			else if ( file.getName().contains(".js") )
			{
				if ( !file.getName().startsWith("._"))
				{
					files.add(file);
				}
			}
		}
	}	
}
