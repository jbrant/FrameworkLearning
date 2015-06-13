package edu.ucf.eplex.encoglearning.cars;

import java.io.File;

import org.encog.ConsoleStatusReportable;
import org.encog.ml.MLClassification;
import org.encog.ml.data.MLData;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.VersatileMLDataSet;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.ml.data.versatile.columns.ColumnType;
import org.encog.ml.data.versatile.sources.CSVDataSource;
import org.encog.ml.data.versatile.sources.VersatileDataSource;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.model.EncogModel;
import org.encog.util.csv.CSVFormat;
import org.encog.util.csv.ReadCSV;
import org.encog.util.simple.EncogUtility;

/**
 * NOTE: This doesn't work very well at all using the classification interface.
 * 
 * @author Jonathan
 *
 */

public class CarAcceptabilityClassification {

	private final static String dataFilename = "src/main/resources/Cars/car.data";

	private static VersatileMLDataSet prepareDataset() {

		// Setup reference to input file
		File carData = new File(dataFilename);

		// Create input file data source
		VersatileDataSource carsDataSource = new CSVDataSource(carData, false,
				',');

		// Create dataset from input data source
		VersatileMLDataSet carsDataset = new VersatileMLDataSet(carsDataSource);

		// Define buying price column
		ColumnDefinition columnBuyingPrice = carsDataset.defineSourceColumn(
				"buyingPriceLevel", 0, ColumnType.ordinal);
		columnBuyingPrice.defineClass(new String[] { "low", "med", "high",
				"vhigh" });

		// Define maintenance price column
		ColumnDefinition columnMaintenancePrice = carsDataset
				.defineSourceColumn("maintenancePriceLevel", 1,
						ColumnType.ordinal);
		columnMaintenancePrice.defineClass(new String[] { "low", "med", "high",
				"vhigh" });

		// Define door count column
		ColumnDefinition columnDoorCount = carsDataset.defineSourceColumn(
				"doorCount", 2, ColumnType.ordinal);
		columnDoorCount.defineClass(new String[] { "2", "3", "4", "5more" });

		// Define person capacity column
		ColumnDefinition columnPersonCapacity = carsDataset.defineSourceColumn(
				"personCapacity", 3, ColumnType.ordinal);
		columnPersonCapacity.defineClass(new String[] { "2", "4", "more" });

		// Define luggage size column
		ColumnDefinition columnLuggageSize = carsDataset.defineSourceColumn(
				"luggageSize", 4, ColumnType.ordinal);
		columnLuggageSize.defineClass(new String[] { "small", "med", "big" });

		// Define safety level column
		ColumnDefinition columnSafetyLevel = carsDataset.defineSourceColumn(
				"safetyLevel", 5, ColumnType.ordinal);
		columnSafetyLevel.defineClass(new String[] { "low", "med", "high" });

		// Define acceptability column (predicted)
		ColumnDefinition predColumnAcceptability = carsDataset
				.defineSourceColumn("acceptable", 6, ColumnType.nominal);
		predColumnAcceptability.defineClass(new String[] { "unacc", "acc",
				"good", "vgood" });

		// Analyze data to determine min/max/std deviation
		carsDataset.analyze();

		// Define the output column as acceptability rating and map everything
		// else to the input
		carsDataset.defineSingleOutputOthersInput(predColumnAcceptability);

		return carsDataset;
	}

	private static EncogModel prepareModel(VersatileMLDataSet dataset) {

		// Define the model and set the training method
		EncogModel carsModel = new EncogModel(dataset);
		carsModel.selectMethod(dataset, MLMethodFactory.TYPE_FEEDFORWARD);

		// Send all output to the console
		carsModel.setReport(new ConsoleStatusReportable());

		// Normalize the data based on the selected model
		dataset.normalize();

		// Hold back 30% of the data for final validation
		carsModel.holdBackValidation(0.3, true, 1001);

		// Select default training type for given dataset
		carsModel.selectTrainingType(dataset);

		return carsModel;
	}

	public static void main(String[] args) {

		// Prep the cars dataset
		VersatileMLDataSet carsDataset = prepareDataset();

		// Prep the cars model
		EncogModel carsModel = prepareModel(carsDataset);

		MLClassification bestMethod = (MLClassification) carsModel
				.crossvalidate(5, true);

		// Display training and validation error
		System.out.println("Training error: "
				+ EncogUtility.calculateClassificationError(bestMethod,
						carsModel.getTrainingDataset()));
		System.out.println("Validation error: "
				+ EncogUtility.calculateClassificationError(bestMethod,
						carsModel.getValidationDataset()));

		NormalizationHelper helper = carsDataset.getNormHelper();
		System.out.println(helper.toString());

		System.out.println("Final model: " + bestMethod);

		ReadCSV csv = new ReadCSV(dataFilename, false, CSVFormat.DECIMAL_POINT);
		String[] line = new String[6];
		MLData input = helper.allocateInputVector();

		while (csv.next()) {

			StringBuilder result = new StringBuilder();
			line[0] = csv.get(0);
			line[1] = csv.get(1);
			line[2] = csv.get(2);
			line[3] = csv.get(3);
			line[4] = csv.get(4);
			line[5] = csv.get(5);
			String correct = csv.get(6);

			helper.normalizeInputVector(line, input.getData(), false);
			int output = bestMethod.classify(input);
		}
	}
}
