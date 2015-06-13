package edu.ucf.eplex.encoglearning;

import org.encog.app.analyst.wizard.PredictionType;
import org.encog.examples.neural.predict.sunspot.PredictSunspot;
import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizeArray;
import org.encog.util.arrayutil.NormalizedField;

public class NormalizeTest {

	private static void normalizeNumber() {

		NormalizedField fuelStats = new NormalizedField(
				NormalizationAction.Normalize, "fuel", 200, 0, -0.9, 0.9);

		double n = fuelStats.normalize(120);

		System.out.println(n);

		double f = fuelStats.deNormalize(n);

		System.out.println(f);
	}

	private static void normalizeArray() {

		NormalizeArray norm = new NormalizeArray();

		norm.setNormalizedHigh(1);
		norm.setNormalizedLow(-1);

		double[] normalizedSunspots = norm.process(PredictSunspot.SUNSPOTS);

		for (double sunspot : normalizedSunspots) {
			System.out.println(sunspot);
		}
	}

	public static void main(String[] args) {

		normalizeNumber();

		normalizeArray();
	}
}
