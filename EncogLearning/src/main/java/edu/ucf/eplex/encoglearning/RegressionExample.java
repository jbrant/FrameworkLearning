package edu.ucf.eplex.encoglearning;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.encog.ConsoleStatusReportable;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.VersatileMLDataSet;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.ml.data.versatile.columns.ColumnType;
import org.encog.ml.data.versatile.missing.MeanMissingHandler;
import org.encog.ml.data.versatile.sources.CSVDataSource;
import org.encog.ml.data.versatile.sources.VersatileDataSource;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.model.EncogModel;
import org.encog.util.csv.CSVFormat;
import org.encog.util.csv.ReadCSV;

public class RegressionExample {

	private static final String filename = "auto-mpg.data";

	private static CSVFormat format;
	private static NormalizationHelper helper;

	public static void main(String args[]) throws URISyntaxException {

		VersatileMLDataSet data = prepareData();

		EncogModel model = buildAndTrainModel(data);

		// Cross-validate and fit the model, using 5 combinations of training
		// and validation data
		MLRegression bestMethod = (MLRegression) model.crossvalidate(5, true);

		// Display the training and validation errors
		System.out.println("Training error: "
				+ model.calculateError(bestMethod, model.getTrainingDataset()));
		System.out
				.println("Validation error: "
						+ model.calculateError(bestMethod,
								model.getValidationDataset()));

		// Display normalization parameters
		helper = data.getNormHelper();
		System.out.println(helper.toString());

		// Display the final model
		System.out.println("Final model: " + bestMethod);

		evaluateModel(bestMethod);
	}

	private static VersatileMLDataSet prepareData() throws URISyntaxException {

		// decimal point and space separated
		format = new CSVFormat('.', ' ');

		// Create file reference for given filename
		File inputFile = new File(new URI(RegressionExample.class
				.getClassLoader().getResource(filename).toString()).getPath());

		inputFile.canRead();

		// create a data source pointed to the file
		VersatileDataSource source = new CSVDataSource(inputFile, false, format);

		// create a new dataset based on the above data source
		VersatileMLDataSet data = new VersatileMLDataSet(source);

		// Set the format of the dataset
		data.getNormHelper().setFormat(format);

		// define MPG column
		ColumnDefinition columnMPG = data.defineSourceColumn("mpg", 0,
				ColumnType.continuous);

		// define cylinders column
		ColumnDefinition columnClyinders = data.defineSourceColumn("cylinders",
				1, ColumnType.ordinal);
		// predefine cylinders ordinals so that order is known
		columnClyinders.defineClass(new String[] { "3", "4", "5", "6", "8" });

		// define displacement column
		data.defineSourceColumn("displacement", 2, ColumnType.continuous);

		// define horsepower column
		ColumnDefinition columnHorsepower = data.defineSourceColumn(
				"horsepower", 3, ColumnType.continuous);

		// define weight column
		data.defineSourceColumn("weight", 4, ColumnType.continuous);

		// define acceleration column
		data.defineSourceColumn("acceleration", 5, ColumnType.continuous);

		// define model-year column
		ColumnDefinition columnModelYear = data.defineSourceColumn(
				"model_year", 6, ColumnType.ordinal);
		// predefine model-year ordinals so that order is known
		columnModelYear.defineClass(new String[] { "70", "71", "72", "73",
				"74", "75", "76", "77", "78", "79", "80", "81", "82" });

		// define origin column
		data.defineSourceColumn("origin", 7, ColumnType.nominal);

		// define how missing values are represented
		data.getNormHelper().defineUnknownValue("?");
		data.getNormHelper().defineMissingHandler(columnHorsepower,
				new MeanMissingHandler());

		// Analyze the data, determining the min/max/mean/standard deviation of
		// every column
		data.analyze();

		// Map the prediction column to the output of the model, and all other
		// columns to the input
		data.defineSingleOutputOthersInput(columnMPG);

		// Initialize the new Encog model with the cars dataset
		return data;
	}

	private static EncogModel buildAndTrainModel(VersatileMLDataSet data) {

		EncogModel model = new EncogModel(data);

		// Set the model to use feedforward neural network as the ML type
		model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);

		// Send any output to the console
		model.setReport(new ConsoleStatusReportable());

		// Normalize the data (Encog will automatically determine the correct
		// normalization type based on the model chosen)
		data.normalize();

		// hold back 30% of data for final validation set
		model.holdBackValidation(0.3, true, 1001);

		// Automatically select the appropriate training type based on the data
		model.selectTrainingType(data);

		return model;
	}

	private static void evaluateModel(MLRegression bestMethod)
			throws URISyntaxException {

		// Read in the dataset
		ReadCSV csv = new ReadCSV(new URI(RegressionExample.class
				.getClassLoader().getResource(filename).toString()).getPath(),
				false, format);

		// There are 7 columns in every line
		String[] line = new String[7];

		// Allocate an input vector that's large enough to hold a single line of
		// data
		MLData input = helper.allocateInputVector();

		// Loop through the input file, evaluating the accuracy of the model on
		// predicting each
		while (csv.next()) {

			// Extract all of the individual cells in the current line
			StringBuilder result = new StringBuilder();
			line[0] = csv.get(1);
			line[1] = csv.get(2);
			line[2] = csv.get(3);
			line[3] = csv.get(4);
			line[4] = csv.get(5);
			line[5] = csv.get(6);
			line[6] = csv.get(7);

			// Get the correct MPG value
			String correct = csv.get(0);

			// Normalize the array into the appropriate ranges
			helper.normalizeInputVector(line, ((BasicMLData) input).getData(),
					false);

			// Run the input line through the regression model
			MLData output = bestMethod.compute(input);

			// Denormalize prediction result
			String mpgPredicted = helper
					.denormalizeOutputVectorToString(output)[0];

			// Output the input values and the actual vs ideal result
			result.append(Arrays.toString(line));
			result.append(String.format(" -> predicted: %s (correct: %s)",
					mpgPredicted, correct));
			System.out.println(result.toString());
		}
	}
}
