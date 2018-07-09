package singleVarResolution;

import java.io.IOException;
import java.util.HashSet;

import parser.MainParser;

public class SGData {
	public HashSet<StarGraph> sgSet;
	public void getData(int flag) {
		//Get data directly from parser
		if ( flag == 0 ) {
			MainParser main = new MainParser();
			try {
				main.parseTrainSetForest();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sgSet = main.sgSet;
		}
		//Read data from previous parse
		else if ( flag == 1 ) { 
			sgSet = readDataFromFile();
		}
	}
	private HashSet<StarGraph> readDataFromFile() {
		// TODO Auto-generated method stub
		return null;
	}

}
