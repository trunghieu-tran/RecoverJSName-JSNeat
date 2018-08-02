package utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Harry Tran on 7/29/18.
 * @project RecoverJSName
 * @email trunghieu.tran@utdallas.edu
 * @organization UTDallas
 */
public class Normalization {
	public static void normalize(ArrayList<Double> data) {
		double vmax = 0;
		for (int i = 0; i < data.size(); ++i) {
			if (data.get(i) > 1) data.set(i, 1.0);
			if (data.get(i) < 0) data.set(i, 0.0);
			vmax = Math.max(vmax, data.get(i));
		}

		if (vmax > 0) {
			for (int i = 0; i < data.size(); ++i)
				data.set(i, data.get(i) / vmax);
		}
	}

	public static HashMap<Double, Double> normalizeMap(ArrayList<Double> data) {
		double vmax = 0;
		for (int i = 0; i < data.size(); ++i) {
			if (data.get(i) > 1) data.set(i, 1.0);
			if (data.get(i) < 0) data.set(i, 0.0);
			vmax = Math.max(vmax, data.get(i));
		}

		HashMap<Double, Double> res = new HashMap<>();
		for (Double aData : data) {
			double tmp = aData;
			if (vmax > 0)
				tmp = aData / vmax;
			res.put(aData, tmp);
		}
		res.put(0.0, 0.0);
		return res;
	}
}
