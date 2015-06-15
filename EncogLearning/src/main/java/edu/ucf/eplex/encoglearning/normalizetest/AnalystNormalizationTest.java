package edu.ucf.eplex.encoglearning.normalizetest;

import java.io.File;
import java.util.Arrays;

import org.encog.app.analyst.AnalystFileFormat;
import org.encog.app.analyst.EncogAnalyst;
import org.encog.app.analyst.csv.normalize.AnalystNormalizeCSV;
import org.encog.app.analyst.script.normalize.AnalystField;
import org.encog.app.analyst.wizard.AnalystWizard;
import org.encog.mathutil.Equilateral;
import org.encog.ml.data.MLDataSet;
import org.encog.util.csv.CSVFormat;
import org.encog.util.simple.EncogUtility;

public class AnalystNormalizationTest {

	private final static String DATA_FILENAME = "src/main/resources/normalize/analyst-norm-test.data";
	private final static String NORMALIZED_FILENAME = "src/main/resources/normalize/analyst-norm-test.normalized.data";
	private static EncogAnalyst ANALYST = new EncogAnalyst();

	public static void main(String[] args) {

		File inputFile = new File(DATA_FILENAME);
		File outputFile = new File(NORMALIZED_FILENAME);

		AnalystWizard wizard = new AnalystWizard(ANALYST);
		wizard.wizard(inputFile, false, AnalystFileFormat.DECPNT_COMMA);

		AnalystNormalizeCSV normCSV = new AnalystNormalizeCSV();
		normCSV.analyze(inputFile, false, CSVFormat.ENGLISH, ANALYST);
		normCSV.setProduceOutputHeaders(true);
		normCSV.normalize(outputFile);

		// MLDataSet normDataAnalyst = ANALYST.getUtility().loadCSV(
		// NORMALIZED_FILENAME);

		MLDataSet normData = EncogUtility.loadCSV2Memory(NORMALIZED_FILENAME,
				ANALYST.determineInputCount(), ANALYST.determineOutputCount(),
				true, CSVFormat.ENGLISH, false);

		AnalystField field1 = ANALYST.getScript().getNormalize()
				.getNormalizedFields().get(0);

		Equilateral eq = new Equilateral(field1.getClasses().size(),
				field1.getNormalizedHigh(), field1.getNormalizedLow());
		int eqIndex = eq.decode(Arrays.copyOfRange(normData.get(2)
				.getInputArray(), 0, 3));

		String className = ANALYST.getScript().getNormalize()
				.getNormalizedFields().get(0).getClasses().get(eqIndex)
				.getName();

		ANALYST.getScript().getNormalize().getNormalizedFields().get(1)
				.deNormalize(normData.get(0).getInput().getData(3));

	}
}
