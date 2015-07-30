package edu.ucf.eplex.encoglearning.neat.doublepolebalancing.fitness;

import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import org.encog.neural.neat.NEATNetwork;

import edu.ucf.eplex.encoglearning.neat.doublepolebalancing.simulator.SimulationParameters;

public class PoleBalancingScore implements CalculateScore
{

  public double calculateScore(MLMethod network)
  {

    int totalFitness = 0;

    // Initialize the fitness function
    PoleBalancingFitnessFunction pbFitFunc = new PoleBalancingFitnessFunction(
        (NEATNetwork) network);

    for (int cnt = 0; cnt < SimulationParameters.NUM_TRIALS; cnt++)
    {
      totalFitness += pbFitFunc.scoreTrial();
    }

    // Return the scored trial evaluation
    return totalFitness;
  }

  public boolean shouldMinimize()
  {
    return false;
  }

  public boolean requireSingleThreaded()
  {
    return false;
  }

}
