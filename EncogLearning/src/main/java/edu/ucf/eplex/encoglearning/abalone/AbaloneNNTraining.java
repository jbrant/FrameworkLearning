package edu.ucf.eplex.encoglearning.abalone;

import java.io.File;
import java.util.Arrays;

import org.encog.ConsoleStatusReportable;
import org.encog.Encog;
import org.encog.app.analyst.AnalystFileFormat;
import org.encog.app.analyst.EncogAnalyst;
import org.encog.app.analyst.csv.normalize.AnalystNormalizeCSV;
import org.encog.app.analyst.script.normalize.AnalystField;
import org.encog.app.analyst.wizard.AnalystWizard;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.mathutil.Equilateral;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.VersatileMLDataSet;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.ml.data.versatile.columns.ColumnType;
import org.encog.ml.data.versatile.sources.CSVDataSource;
import org.encog.ml.data.versatile.sources.VersatileDataSource;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.model.EncogModel;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.util.csv.CSVFormat;
import org.encog.util.csv.ReadCSV;
import org.encog.util.simple.EncogUtility;

public class AbaloneNNTraining {

	private final static String DATA_FILENAME = "src/main/resources/Abalone/abalone.data";
	private final static String EGA_FILENAME = "src/main/resources/Abalone/abalone.ega";
	private final static char DATA_FILE_DELIMITER = ',';
	private static EncogAnalyst ANALYST = new EncogAnalyst();

	private static File normalizeInputFile() {

		// Get file handle
		File inputFile = new File(DATA_FILENAME);

		// Create new file
		File normalizedFile = new File(String.format("%s.normalized",
				DATA_FILENAME));

		// Only normalize the file if it hasn't already been done
		if (normalizedFile.exists() == false) {

			// Create the wizard and analyze the file
			AnalystWizard wizard = new AnalystWizard(ANALYST);
			wizard.wizard(inputFile, false, AnalystFileFormat.DECPNT_COMMA);

			// Instantiate normalizer
			final AnalystNormalizeCSV norm = new AnalystNormalizeCSV();

			// Analyze the source file
			norm.analyze(inputFile, false, CSVFormat.ENGLISH, ANALYST);

			// Set input headings
			norm.setInputHeadings(new String[] { "Sex", "Shell Length",
					"Shell Diameter", "Shell Height", "Total Abalone Weight",
					"Shucked Weight", "Viscera Weight", "Shell Weight", "Rings" });

			// Ensure that there are no headers in the output
			norm.setProduceOutputHeaders(true);

			// Normalize to the output file
			norm.normalize(normalizedFile);

			// Save the ega file
			ANALYST.save(EGA_FILENAME);
		}
		// Otherwise, load the already written EGA file
		else {
			ANALYST.load(EGA_FILENAME);
		}

		return normalizedFile;
	}

	private static VersatileMLDataSet prepareDataset() {

		// Create data source
		VersatileDataSource abaloneDataSource = new CSVDataSource(new File(
				DATA_FILENAME), false, DATA_FILE_DELIMITER);

		// Create a dataset from the data source
		VersatileMLDataSet abaloneDataset = new VersatileMLDataSet(
				abaloneDataSource);

		// Define sex column
		ColumnDefinition sexColumn = abaloneDataset.defineSourceColumn("Sex",
				0, ColumnType.nominal);
		sexColumn.defineClass(new String[] { "M", "F" });

		// Define continuous columns
		abaloneDataset.defineSourceColumn("Shell Length", 1,
				ColumnType.continuous);
		abaloneDataset.defineSourceColumn("Shell Diameter", 2,
				ColumnType.continuous);
		abaloneDataset.defineSourceColumn("Shell Height", 3,
				ColumnType.continuous);
		abaloneDataset.defineSourceColumn("Total Abalone Weight", 4,
				ColumnType.continuous);
		abaloneDataset.defineSourceColumn("Shucked Weight", 5,
				ColumnType.continuous);
		abaloneDataset.defineSourceColumn("Viscera Weight", 6,
				ColumnType.continuous);
		abaloneDataset.defineSourceColumn("Shell Weight", 7,
				ColumnType.continuous);

		// Define predicted column
		ColumnDefinition numRingsColumn = abaloneDataset.defineSourceColumn(
				"Rings", 8, ColumnType.continuous);

		// Analyze the dataset (calculate sum, mean, and standard deviation)
		abaloneDataset.analyze();

		// Setup dataset with number of rings as the predicted value (which
		// indicates its age in 1.5 year increments)
		abaloneDataset.defineSingleOutputOthersInput(numRingsColumn);

		return abaloneDataset;
	}

	private static EncogModel prepareModel(VersatileMLDataSet dataset,
			String mlMethod) {

		// Define the model and set the training method
		EncogModel abaloneModel = new EncogModel(dataset);
		abaloneModel.selectMethod(dataset, mlMethod);

		// Send all output to the console
		abaloneModel.setReport(new ConsoleStatusReportable());

		// Normalize the data based on the selected model
		dataset.normalize();

		// Hold back 30% of the data for final validation
		abaloneModel.holdBackValidation(0.3, true, 1001);

		// Select default training type for given dataset
		abaloneModel.selectTrainingType(dataset);

		return abaloneModel;
	}

	private static BasicNetwork createNetwork(int numInputs, int numOutputs) {

		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(null, true, numInputs));
		network.addLayer(new BasicLayer(new ActivationTANH(), true, 20));
		network.addLayer(new BasicLayer(new ActivationTANH(), false, numOutputs));

		network.getStructure().finalizeStructure();
		network.reset();

		return network;
	}

	public static void main(String[] args) {

		String trainingApproach = args[0];

		if (trainingApproach.equalsIgnoreCase("encog-model")) {

			// Prepare the Abalone dataset
			VersatileMLDataSet preparedDataset = prepareDataset();

			// Prep the abalone model
			EncogModel abaloneModel = prepareModel(preparedDataset,
					MLMethodFactory.TYPE_NEAT);

			// Train with 5-fold cross-validation
			MLRegression bestMethod = (MLRegression) abaloneModel
					.crossvalidate(5, true);

			// Display training and validation error
			System.out.println("Training error: "
					+ EncogUtility.calculateRegressionError(bestMethod,
							abaloneModel.getTrainingDataset()));
			System.out.println("Validation error: "
					+ EncogUtility.calculateRegressionError(bestMethod,
							abaloneModel.getValidationDataset()));

			NormalizationHelper helper = preparedDataset.getNormHelper();
			System.out.println(helper.toString());

			System.out.println("Final model: " + bestMethod);

			ReadCSV csv = new ReadCSV(DATA_FILENAME, false,
					CSVFormat.DECIMAL_POINT);
			String[] line = new String[9];
			MLData input = helper.allocateInputVector();

			while (csv.next()) {

				StringBuilder result = new StringBuilder();
				line[0] = csv.get(0);
				line[1] = csv.get(1);
				line[2] = csv.get(2);
				line[3] = csv.get(3);
				line[4] = csv.get(4);
				line[5] = csv.get(5);
				line[6] = csv.get(6);
				line[7] = csv.get(7);
				String correct = csv.get(8);

				helper.normalizeInputVector(line, input.getData(), false);
				MLData output = bestMethod.compute(input);
				String ratingChosen = helper
						.denormalizeOutputVectorToString(output)[0];

				result.append(Arrays.toString(line));
				result.append(" -> predicted: ");
				result.append(ratingChosen);
				result.append("(correct: ");
				result.append(correct);
				result.append(")");

				System.out.println(result.toString());
			}
		}

		else if (trainingApproach.equalsIgnoreCase("basic-training")) {

			File normalizedFile = normalizeInputFile();

			MLDataSet preparedDataset = EncogUtility.loadCSV2Memory(
					normalizedFile.toString(), ANALYST.determineInputCount(),
					ANALYST.determineOutputCount(), true, CSVFormat.ENGLISH,
					false);

			// Build the neural network
			BasicNetwork network = createNetwork(
					preparedDataset.getInputSize(),
					preparedDataset.getIdealSize());

			// Train with resilient propagation
			final ResilientPropagation rPropTrainer = new ResilientPropagation(
					network, preparedDataset);

			int epoch = 1;

			// Iterate through each training epoch
			do {
				rPropTrainer.iteration();
				System.out.println(String.format("Epoch #%d Error: %f", epoch,
						rPropTrainer.getError()));
				epoch++;
			} while (rPropTrainer.getError() > 0.05);

			// Complete the training process
			rPropTrainer.finishTraining();

			int matchingCases = 0;

			// See how well the network was trained
			for (MLDataPair pair : preparedDataset) {

				// Compute the output from the trained network
				MLData testOutput = network.compute(pair.getInput());

				// Get predicted field
				AnalystField predictedField = ANALYST
						.getScript()
						.getNormalize()
						.getNormalizedFields()
						.get(ANALYST.getScript().getNormalize()
								.getNormalizedFields().size() - 1);

				// Create equilateral matrix for decoding
				Equilateral eq = new Equilateral(predictedField.getClasses()
						.size(), predictedField.getNormalizedHigh(),
						predictedField.getNormalizedLow());

				// Convert the predicted and ideal
				String predictedClass = predictedField.getClasses()
						.get(eq.decode(testOutput.getData())).getName();
				String idealClass = predictedField.getClasses()
						.get(eq.decode(pair.getIdealArray())).getName();

				// Print results
				// System.out.println(String.format("Actual: %s, Ideal: %s",
				// predictedClass, idealClass));

				if (predictedClass.equalsIgnoreCase(idealClass)) {
					matchingCases++;
				}
			}

			System.out.println(String.format(
					"Total cases: %s, Matching cases: %s",
					preparedDataset.size(), matchingCases));
		}

		Encog.getInstance().shutdown();
	}
}
