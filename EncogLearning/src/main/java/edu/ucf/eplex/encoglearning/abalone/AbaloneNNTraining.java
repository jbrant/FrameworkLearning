package edu.ucf.eplex.encoglearning.abalone;

import java.io.File;

import org.encog.Encog;
import org.encog.app.analyst.AnalystFileFormat;
import org.encog.app.analyst.EncogAnalyst;
import org.encog.app.analyst.csv.normalize.AnalystNormalizeCSV;
import org.encog.app.analyst.wizard.AnalystWizard;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.versatile.VersatileMLDataSet;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.ml.data.versatile.columns.ColumnType;
import org.encog.ml.data.versatile.sources.CSVDataSource;
import org.encog.ml.data.versatile.sources.VersatileDataSource;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.util.csv.CSVFormat;
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

	private static VersatileMLDataSet prepareDataset(File normalizedAbaloneFile) {

		// Create data source
		VersatileDataSource abaloneDataSource = new CSVDataSource(
				normalizedAbaloneFile, false, DATA_FILE_DELIMITER);

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

		// Normalize the dataset
		// abaloneDataset.normalize();

		return abaloneDataset;
	}

	private static BasicNetwork createNetwork(int numInputs, int numOutputs) {

		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(null, true, numInputs));
		// network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 10));
		network.addLayer(new BasicLayer(new ActivationTANH(), false, numOutputs));

		network.getStructure().finalizeStructure();
		network.reset();

		return network;
	}

	public static void main(String[] args) {

		if (args.length > 0) {
			String trainingApproach = args[0];

			if (trainingApproach.equalsIgnoreCase("encog-model")) {
				File normalizedFile = normalizeInputFile();

				// Prepare the Abalone dataset
				VersatileMLDataSet preparedDataset = prepareDataset(normalizedFile);
			}
		}

		else {

			File normalizedFile = normalizeInputFile();

			MLDataSet preparedDataset = EncogUtility.loadCSV2Memory(
					normalizedFile.toString(), 8, 1, true, new CSVFormat('.',
							','), false);

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
			} while (rPropTrainer.getError() > 0.01);

			// Complete the training process
			rPropTrainer.finishTraining();

			// See how well the network was trained
			for (MLDataPair pair : preparedDataset) {

				// Compute the output from the trained network
				MLData testOutput = network.compute(pair.getInput());

								
				
				System.out.println(ANALYST.getScript().getNormalize()
						.getNormalizedFields().get(6)
						.deNormalize(pair.getInput().getData(7)));
				System.out.println(ANALYST.getScript().getNormalize()
						.getNormalizedFields().get(8)
						.deNormalize(pair.getIdeal().getData(0)));
				System.out.println(ANALYST.getScript().getNormalize()
						.getNormalizedFields().get(8)
						.deNormalize(testOutput.getData(0)));

				
				
				// Print results
				System.out.println(String.format("Actual: %f, Ideal: %f",
						testOutput.getData(0), pair.getIdeal().getData(0)));
			}
		}

		Encog.getInstance().shutdown();
	}
}
