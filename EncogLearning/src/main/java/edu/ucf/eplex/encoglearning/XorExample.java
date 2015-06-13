package edu.ucf.eplex.encoglearning;

import org.encog.Encog;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

public class XorExample {

	/**
	 * The input necessary for XOR.
	 */
	public static double XOR_INPUT[][] = { { 0.0, 0.0 }, { 1.0, 0.0 },
			{ 0.0, 1.0 }, { 1.0, 1.0 } };

	/**
	 * The ideal data necessary for XOR.
	 */
	public static double XOR_IDEAL[][] = { { 0.0 }, { 1.0 }, { 1.0 }, { 0.0 } };

	public static void main(final String args[]) {

		// create a neural network, without using a factory
		BasicNetwork network = new BasicNetwork();

		// Input layer
		network.addLayer(new BasicLayer(null, true, 2));

		// Hidden layer
		network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 3));

		// Output layer
		network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 1));

		network.getStructure().finalizeStructure();
		network.reset();

		// Create training data
		MLDataSet trainingSet = new BasicMLDataSet(XOR_INPUT, XOR_IDEAL);

		// Train the neural network
		final ResilientPropagation train = new ResilientPropagation(network,
				trainingSet);

		int epoch = 1;

		do {
			train.iteration();
			System.out.println(String.format("Epoch #%d, Error: %f", epoch,
					train.getError()));
			epoch++;
		} while (train.getError() > 0.01);

		train.finishTraining();

		// Test the neural network
		System.out.println("Neural Network Results:");

		for (MLDataPair pair : trainingSet) {

			final MLData output = network.compute(pair.getInput());
			System.out.println(String.format("%f, %f, actual = %f, ideal = %f",
					pair.getInput().getData(0), pair.getInput().getData(1),
					output.getData(0), pair.getIdeal().getData(0)));
		}

		Encog.getInstance().shutdown();
	}
}
