package core;

import javafx.util.Pair;

import java.util.HashMap;

/**
 * @author Harry Tran on 7/6/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class testPair {
	public static void main(String[] args) {
		HashMap<Pair<Integer, Integer>, Integer> map = new HashMap<>();
		map.put(new Pair<>(2,3), 10);

		System.out.println(map.getOrDefault(new Pair<>(2,3), -1));
	}
}
