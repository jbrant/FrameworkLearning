package edu.ucf.eplex.noveltysearch.mazeexperiment.score;

import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import org.encog.neural.neat.NEATNetwork;

import edu.ucf.eplex.noveltysearch.mazeexperiment.fitness.MazeFitnessFunction;

public class MazeFitnessScore implements CalculateScore
{

  public double calculateScore(MLMethod network)
  {

    // Initialize the fitness function
    MazeFitnessFunction fitnessFunction = new MazeFitnessFunction(
        (NEATNetwork) network);

    // Return the final fitness for the trial
    return fitnessFunction.scoreTrial(false);
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
