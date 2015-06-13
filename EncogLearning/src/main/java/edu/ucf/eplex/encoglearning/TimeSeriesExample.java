package edu.ucf.eplex.encoglearning;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.encog.ConsoleStatusReportable;
import org.encog.Encog;
import org.encog.mathutil.error.ErrorCalculation;
import org.encog.mathutil.error.ErrorCalculationMode;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.VersatileMLDataSet;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.ml.data.versatile.columns.ColumnType;
import org.encog.ml.data.versatile.sources.CSVDataSource;
import org.encog.ml.data.versatile.sources.VersatileDataSource;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.model.EncogModel;
import org.encog.util.arrayutil.VectorWindow;
import org.encog.util.csv.CSVFormat;
import org.encog.util.csv.ReadCSV;

public class TimeSeriesExample {

	private static String filename = "spot_num.txt";

	private static final int WINDOW_SIZE = 3;

	public static void main(String[] args) throws URISyntaxException {

		// Set error calculation mode to RMS
		ErrorCalculation.setMode(ErrorCalculationMode.RMS);

		// Reference the data file
		File inputFile = new File(new URI(RegressionExample.class
				.getClassLoader().getResource(filename).toString()).getPath());

		// Define the format of the data file
		CSVFormat format = new CSVFormat('.', ' ');

		// Initialize a data source pointing to the input file
		VersatileDataSource source = new CSVDataSource(inputFile, true, format);

		// Create the data set and set the appropriate format on it
		VersatileMLDataSet data = new VersatileMLDataSet(source);
		data.getNormHelper().setFormat(format);

		// Define the sun spot number and deviation columns
		ColumnDefinition columnSSN = data.defineSourceColumn("SSN",
				ColumnType.continuous);
		ColumnDefinition columnDEV = data.defineSourceColumn("DEV",
				ColumnType.continuous);

		// Analyze the data, determining the min/max/mean/sd of every column
		data.analyze();

		// Setup the inputs/output using the SSN and DEV as input and the SSN as
		// output (since this is time series, we can have the same column as
		// both an input and an output)
		data.defineInput(columnSSN);
		data.defineInput(columnDEV);
		data.defineOutput(columnSSN);

		// Initialize the model with the data set and set it up to use a feed
		// forward neural network
		EncogModel model = new EncogModel(data);
		model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);

		// Pipe all output to console
		model.setReport(new ConsoleStatusReportable());

		// Normalize the data (normalization method is automatically selected)
		data.normalize();

		// Set time series lead/lag window size
		data.setLeadWindowSize(1);
		data.setLagWindowSize(WINDOW_SIZE);

		// Hold back 30% of data for final validation set
		model.holdBackValidation(0.3, false, 1001);

		// Automatically select the appropriate training type based on the data
		model.selectTrainingType(data);

		// Run 5-fold cross-validated training and return the best method
		MLRegression bestMethod = (MLRegression) model.crossvalidate(5, false);

		// Display the training and validation errors
		System.out.println("Training error: "
				+ model.calculateError(bestMethod, model.getTrainingDataset()));
		System.out
				.println("Validation error: "
						+ model.calculateError(bestMethod,
								model.getValidationDataset()));

		// Display normalization parameters
		NormalizationHelper helper = data.getNormHelper();
		System.out.println(helper.toString());

		// Display the final model
		System.out.println("Final model: " + bestMethod);

		// Read in the test data
		ReadCSV csv = new ReadCSV(inputFile, true, format);
		String[] line = new String[2];

		// Create a vector to hold each time slice
		double[] slice = new double[2];
		VectorWindow window = new VectorWindow(WINDOW_SIZE + 1);
		MLData input = helper.allocateInputVector(WINDOW_SIZE + 1);

		// Upper bound the number of iterations at 100
		int stopAfter = 100;

		// Loop through every line in the data file until we reach 100
		// iterations
		while (csv.next() && stopAfter > 0) {

			StringBuilder result = new StringBuilder();

			// Extract the SSN and DEV for the current line
			line[0] = csv.get(2);
			line[1] = csv.get(3);

			// Normalize the input vector
			helper.normalizeInputVector(line, slice, false);

			// Build window if we have enough "lag" time slices
			if (window.isReady()) {

				window.copyWindow(input.getData(), 0);

				// Get the correct (ideal) prediction
				String correct = csv.get(2);

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

			// Add the normalized slice to the window
			window.add(slice);

			// Decrement the stop after count
			stopAfter--;
		}

		// Shutdown Encog
		Encog.getInstance().shutdown();
	}
}
