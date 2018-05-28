package dataCenter.utils;
import java.io.*;

/**
 * @author Harry Tran on 5/23/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class FileIO {
	public static String readStringFromFile(String inputFile) {
		try {
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(inputFile));
			byte[] bytes = new byte[(int) new File(inputFile).length()];
			in.read(bytes);
			in.close();
			return new String(bytes);
		}
		catch (Exception e) {
			//e.printStackTrace();
			return null;
		}
	}
	public static void writeStringToFile(String outputFile, String data) {
		BufferedWriter writer = null;
		try
		{
			writer = new BufferedWriter( new FileWriter(outputFile));
			writer.write(data);

		}
		catch ( IOException e)
		{
			System.out.println("Writing error!!!");
		}
		finally
		{
			try
			{
				if ( writer != null)
					writer.close( );
			}
			catch ( IOException e)
			{
				System.out.println("Closing file error!!!");
			}
		}
	}
}
