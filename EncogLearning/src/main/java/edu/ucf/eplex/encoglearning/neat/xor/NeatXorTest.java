package edu.ucf.eplex.encoglearning.neat.xor;

import org.encog.Encog;
import org.encog.ml.CalculateScore;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.ea.opp.CompoundOperator;
import org.encog.ml.ea.opp.selection.TruncationSelection;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.neural.hyperneat.HyperNEATCODEC;
import org.encog.neural.neat.NEATCODEC;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.opp.NEATCrossover;
import org.encog.neural.neat.training.opp.NEATMutateAddLink;
import org.encog.neural.neat.training.opp.NEATMutateAddNode;
import org.encog.neural.neat.training.opp.NEATMutateRemoveLink;
import org.encog.neural.neat.training.opp.NEATMutateWeights;
import org.encog.neural.neat.training.opp.links.MutatePerturbLinkWeight;
import org.encog.neural.neat.training.opp.links.MutateResetLinkWeight;
import org.encog.neural.neat.training.opp.links.SelectFixed;
import org.encog.neural.neat.training.opp.links.SelectProportion;
import org.encog.neural.neat.training.species.OriginalNEATSpeciation;
import org.encog.neural.networks.training.TrainingSetScore;
import org.encog.util.simple.EncogUtility;

public class NeatXorTest
{

  public static double XOR_INPUT[][] =
  {
      {
          0.0, 0.0
      },
      {
          0.0, 1.0
      },
      {
          1.0, 0.0
      },
      {
          1.0, 1.0
      }
  };

  public static double XOR_IDEAL[][] =
  {
      {
        0.0
      },
      {
        1.0
      },
      {
        1.0
      },
      {
        0.0
      }
  };

  public static void main(String[] args)
  {

    // Create the training set using the given input matrix and ideal output
    // (single column matrix or vector)
    MLDataSet xorTrainingSet = new BasicMLDataSet(XOR_INPUT, XOR_IDEAL);

    // Create the NEAT population (2 input neurons, 1 output neuron, and a
    // population of 500 initial networks)
    NEATPopulation population = new NEATPopulation(2, 1, 100);

    // Increase the initial connection density to cause initial networks to
    // start out with more connections
    population.setInitialConnectionDensity(1.0);

    // Create the initial population
    population.reset();

    // Create a scoring method
    CalculateScore score = new TrainingSetScore(xorTrainingSet);

    // Construct the NEAT trainer
    TrainEA neatTrainer = constructNEATTrainer(population, score);
    neatTrainer.setThreadCount(1);

    do
    {
      neatTrainer.iteration();
      System.out.println(String.format(
          "Training Epoch: #%d, Error: %f, Species: %d, Population Size: %d",
          neatTrainer.getIteration(),
          neatTrainer.getError(),
          population.getSpecies().size(),
          population.getPopulationSize()));
    }
    while (neatTrainer.getError() > 0.01);

    // Get the best performing network
    NEATNetwork bestNetwork = (NEATNetwork) neatTrainer.getCODEC().decode(
        neatTrainer.getBestGenome());

    // Test the neural network
    System.out.println("Winning neural network results:");
    EncogUtility.evaluate(bestNetwork, xorTrainingSet);

    Encog.getInstance().shutdown();
  }

  private static TrainEA constructNEATTrainer(
      final NEATPopulation population,
      final CalculateScore calculateScore)
  {
    final TrainEA result = new TrainEA(population, calculateScore);
    result.setSpeciation(new OriginalNEATSpeciation());

    result.setSelection(new TruncationSelection(result, 0.3));
    final CompoundOperator weightMutation = new CompoundOperator();
    weightMutation.getComponents().add(
        0.1125,
        new NEATMutateWeights(new SelectFixed(1), new MutatePerturbLinkWeight(
            0.02)));
    weightMutation.getComponents().add(
        0.1125,
        new NEATMutateWeights(new SelectFixed(2), new MutatePerturbLinkWeight(
            0.02)));
    weightMutation.getComponents().add(
        0.1125,
        new NEATMutateWeights(new SelectFixed(3), new MutatePerturbLinkWeight(
            0.02)));
    weightMutation.getComponents().add(
        0.1125,
        new NEATMutateWeights(
            new SelectProportion(0.02),
            new MutatePerturbLinkWeight(0.02)));
    weightMutation.getComponents().add(
        0.1125,
        new NEATMutateWeights(new SelectFixed(1), new MutatePerturbLinkWeight(
            1)));
    weightMutation.getComponents().add(
        0.1125,
        new NEATMutateWeights(new SelectFixed(2), new MutatePerturbLinkWeight(
            1)));
    weightMutation.getComponents().add(
        0.1125,
        new NEATMutateWeights(new SelectFixed(3), new MutatePerturbLinkWeight(
            1)));
    weightMutation.getComponents().add(
        0.1125,
        new NEATMutateWeights(
            new SelectProportion(0.02),
            new MutatePerturbLinkWeight(1)));
    weightMutation.getComponents()
        .add(
            0.03,
            new NEATMutateWeights(
                new SelectFixed(1),
                new MutateResetLinkWeight()));
    weightMutation.getComponents()
        .add(
            0.03,
            new NEATMutateWeights(
                new SelectFixed(2),
                new MutateResetLinkWeight()));
    weightMutation.getComponents()
        .add(
            0.03,
            new NEATMutateWeights(
                new SelectFixed(3),
                new MutateResetLinkWeight()));
    weightMutation.getComponents().add(
        0.01,
        new NEATMutateWeights(
            new SelectProportion(0.02),
            new MutateResetLinkWeight()));
    weightMutation.getComponents().finalizeStructure();

    result.setChampMutation(weightMutation);
    result.addOperation(0.5, new NEATCrossover());
    result.addOperation(0.494, weightMutation);
    result.addOperation(0.005, new NEATMutateAddNode());
    result.addOperation(0.05, new NEATMutateAddLink());
    result.addOperation(0.005, new NEATMutateRemoveLink());
    result.getOperators().finalizeStructure();

    if (population.isHyperNEAT())
    {
      result.setCODEC(new HyperNEATCODEC());
    }
    else
    {
      result.setCODEC(new NEATCODEC());
    }

    return result;
  }
}
