package parser;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.IRFactory;
import org.mozilla.javascript.ast.AstRoot;

public class MainParser {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		String filePath = "../Data/";
//		String filePath = "F:\\Study\\Research\\RecoverJsName\\Data\\0xsky\\xblog\\xblogroot\\admin\\js\\admin.js";
//		String filePath = ".\\resources\\_test";
		MainParser demo = new MainParser();
		demo.parseJS(filePath);
	}
	
	public void parseJS (String filePath) throws Exception
	{
		CompilerEnvirons env = new CompilerEnvirons();
		env.setRecoverFromErrors(true);
		MyVisitor myVisitor = new MyVisitor();
		File dir = new File(filePath);
		ArrayList<File> files = new ArrayList<>();
		searchDir(dir, files);
		//File[] files = dir.listFiles();
		for ( File file : files )
		{
			System.out.println(file.getAbsolutePath());
			FileReader strReader = new FileReader(file.getAbsolutePath());
			IRFactory factory = new IRFactory(env, new JSErrorReporter());
			try
			{
			AstRoot rootNode = factory.parse(strReader, null, 0);
			rootNode.visit(myVisitor);
			}
			catch (Exception e)
			{
				System.out.println("Exception at " + e);
				continue;
			}
		}
		myVisitor.print();
		myVisitor.printToFile();
	}

	private void searchDir(File dir, ArrayList<File> files) {
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
