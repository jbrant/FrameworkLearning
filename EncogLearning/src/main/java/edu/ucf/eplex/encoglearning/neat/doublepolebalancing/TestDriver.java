package edu.ucf.eplex.encoglearning.neat.doublepolebalancing;

import org.encog.ml.CalculateScore;
import org.encog.ml.ea.opp.CompoundOperator;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.neural.neat.NEATCODEC;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.NEATUtil;
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

import edu.ucf.eplex.encoglearning.neat.doublepolebalancing.fitness.PoleBalancingFitnessFunction;
import edu.ucf.eplex.encoglearning.neat.doublepolebalancing.fitness.PoleBalancingScore;
import edu.ucf.eplex.encoglearning.neat.doublepolebalancing.simulator.SimulationParameters;

public class TestDriver
{

  public static NEATPopulation createNeatPopulation()
  {

    NEATPopulation population = new NEATPopulation(7, 1, 200);
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
    result.addOperation(0.75, weightMutation);
    result.addOperation(0.1, new NEATMutateAddNode());
    result.addOperation(0.1, new NEATMutateAddLink());
    result.addOperation(0.01, new NEATMutateRemoveLink());
    result.getOperators().finalizeStructure();

    result.setCODEC(new NEATCODEC());

    return result;
  }

  public static void main(String[] args)
  {

    TrainEA train = NEATUtil.constructNEATTrainer(
        createNeatPopulation(),
        new PoleBalancingScore());
    train.setThreadCount(1);

    int epoch = 1;

    for (int i = 0; i < 100
        && train.getError() < (SimulationParameters.NUM_TRIALS * SimulationParameters.TIMESTEPS); i++)
    {
      train.iteration();

      System.out.println(String.format(
          "Epoch #%d, Score: %f, Links: %d",
          epoch,
          train.getError(),
          ((NEATNetwork) train.getCODEC().decode(train.getBestGenome()))
              .getLinks().length));

      epoch++;
    }

    train.finishTraining();    

    NEATNetwork winningNetwork = ((NEATNetwork) train.getCODEC().decode(
        train.getBestGenome()));
    PoleBalancingFitnessFunction pbFit = new PoleBalancingFitnessFunction(
        winningNetwork);
    
    // Initialize the display
    pbFit.enableDisplay();
    
    pbFit.scoreTrial();
  }
}
