package edu.ucf.eplex.noveltysearch.mazeexperiment;

import org.encog.ml.CalculateScore;
import org.encog.ml.ea.opp.CompoundOperator;
import org.encog.ml.ea.train.basic.TrainEA;
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

import edu.ucf.eplex.noveltysearch.mazeexperiment.fitness.MazeFitnessFunction;
import edu.ucf.eplex.noveltysearch.mazeexperiment.score.MazeFitnessScore;
import edu.ucf.eplex.noveltysearch.mazeexperiment.util.LoggingUtil;

public class HardMazeExperiment
{

  private static NEATPopulation createNeatPopulation()
  {

    NEATPopulation population = new NEATPopulation(11, 2, 50);
    population.setInitialConnectionDensity(1);
    population.reset();

    return population;
  }

  public static TrainEA constructCustomNEATTrainer(
      final NEATPopulation population,
      final CalculateScore calculateScore)
  {
    final TrainEA result = new TrainEA(population, calculateScore);
    result.setSpeciation(new OriginalNEATSpeciation());

    // result.setSelection(new TruncationSelection(result, 0.3));
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
    result.addOperation(0.6, weightMutation);
    result.addOperation(0.001, new NEATMutateAddNode());
    result.addOperation(0.1, new NEATMutateAddLink());
    result.addOperation(0.01, new NEATMutateRemoveLink());
    result.getOperators().finalizeStructure();

    result.setCODEC(new NEATCODEC());

    return result;
  }

  public static void main(String[] args)
  {

    // Initialize file logger
    LoggingUtil.configureLogger("output.csv");

    TrainEA train = constructCustomNEATTrainer(
        createNeatPopulation(),
        new MazeFitnessScore());
    // train.setThreadCount(1);

    int epoch = 1;

    for (int i = 0; i < 100; i++)
    {

      train.iteration();

      NEATNetwork bestGenerationNetwork = ((NEATNetwork) train.getCODEC()
          .decode(train.getBestGenome()));

      System.out.println(String.format(
          "Epoch #%d, Score: %f, Links: %d",
          epoch,
          train.getError(),
          bestGenerationNetwork.getLinks().length));

      MazeFitnessFunction genFitnessFunction = new MazeFitnessFunction(
          bestGenerationNetwork);
      genFitnessFunction.scoreTrial(true, epoch);

      epoch++;
    }

    train.finishTraining();
    LoggingUtil.closeLogger();

    NEATNetwork winningNetwork = ((NEATNetwork) train.getCODEC().decode(
        train.getBestGenome()));

    MazeFitnessFunction fitnessFunction = new MazeFitnessFunction(
        winningNetwork);
    fitnessFunction.scoreTrial(false);
  }
}
