package edu.ucf.eplex.encoglearning;

import org.encog.mathutil.Equilateral;

public class EquilateralTest {

	public static void main(String[] args) {
		Equilateral eq = new Equilateral(4, 1, -1);

		for (int i = 0; i < 4; i++) {

			StringBuilder line = new StringBuilder();
			line.append(i);
			line.append(':');

			double[] d = eq.encode(i);

			for (int j = 0; j < d.length; j++) {
				if (j > 0) {
					line.append(',');
				}

				line.append(d[j]);
			}

			System.out.println(line.toString());
		}
	}
}
