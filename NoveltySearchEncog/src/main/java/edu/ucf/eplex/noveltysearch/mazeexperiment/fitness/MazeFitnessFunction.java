package edu.ucf.eplex.noveltysearch.mazeexperiment.fitness;

import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.neat.NEATNetwork;

import edu.ucf.eplex.noveltysearch.mazeexperiment.components.Environment;
import edu.ucf.eplex.noveltysearch.mazeexperiment.configuration.MazeParameters;
import edu.ucf.eplex.noveltysearch.mazeexperiment.util.LoggingUtil;

public class MazeFitnessFunction
{

  private NEATNetwork network;

  public MazeFitnessFunction(NEATNetwork network)
  {
    this.network = network;
  }

  public double scoreTrial(boolean isLogging, int... generation)
  {

    double fitness = 0;
    int curTimestep;

    // Initialize the environment
    Environment environment = new Environment();

    for (curTimestep = 0; curTimestep < MazeParameters.TIMESTEPS
        && environment.isGoalReached() == false; curTimestep++)
    {
      // Run one time step
      fitness += executeTimestep(environment);

      if (isLogging)
      {
        LoggingUtil.writeTrialBehavior(
            generation[0],
            curTimestep,
            environment.getDistanceToTarget(),
            environment.getNavigator());
      }
    }

    // Calculate the fitness based on the final distance to the target
    fitness = Environment.MAX_DISTANCE_TO_TARGET
        - environment.getDistanceToTarget();

    return fitness;
  }

  private double executeTimestep(Environment environment)
  {

    double timestepFitness = 0;

    // Generate ANN inputs
    double[] annInputs = environment.getRawANNInputs();

    // Active the network and retrieve the output
    double[] networkOutput = network.compute(new BasicMLData(annInputs))
        .getData();

    // Convert the ANN outputs to changes in the navigator's angular velocity
    // and speed
    environment
        .translateAndApplyANNOutputs(networkOutput[0], networkOutput[1]);

    // Run a single navigator timestep in the environment
    environment.runTimeStep();

    // Compute the new distance to the target (if the distance is less than 1,
    // we've solved the maze anyways but don't want to divide by 0 or
    // artificially inflate the fitness)
    double distanceToTarget = Math.max(environment.getDistanceToTarget(), 1);

    // Calculate the fitness as the quotient of the minimum distance to the
    // target to be considered solved and the distance to the target. If the
    // fitness is 1 or less, then the maze has been solved
    timestepFitness = Environment.MIN_SUCCESS_DISTANCE / distanceToTarget;

    return timestepFitness;
  }
}
