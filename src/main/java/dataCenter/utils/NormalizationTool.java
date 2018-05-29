package dataCenter.utils;
import java.util.logging.Logger;

/**
 * @author Harry Tran on 5/28/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class NormalizationTool {
	private final static Logger LOGGER = Logger.getLogger(NormalizationTool.class.getName());
	public static void normalizeCFMatrix(String filename, String output) {
		String data = FileIO.readStringFromFile(filename);
		StringBuilder res = new StringBuilder();
		String[] parts = data.split("\\n");
		double sum = 0;
		for (String part: parts) {
			String[] tmp = part.split(" ");
			try {
				double tt = Double.parseDouble(tmp[2]);
				sum += tt;
			} catch (Exception e) {
				LOGGER.info(e.getMessage());
			}
		}

		for (String part: parts) {
			String[] tmp = part.split(" ");
			try {
				double tt = Double.parseDouble(tmp[2]);
				res.append(tmp[0]).append(" ").append(tmp[1]).append(" ").append(tt / sum).append("\n");
			} catch (Exception e) {
				LOGGER.info(e.getMessage());
			}
		}
		FileIO.writeStringToFile(output, res.toString());
	}
}
