package edu.ucf.eplex.noveltysearch.mazeexperiment.components;

import java.util.ArrayList;
import java.util.List;

import edu.ucf.eplex.noveltysearch.mazeexperiment.configuration.MazeParameters;
import javafx.geometry.Point2D;

public class Environment
{

  private Character navigator;
  private List<MazeLine> walls;
  private MazePoint goalLocation;
  private boolean goalReached;

  private static final double ANN_OUTPUT_SCALING_FACTOR = 0.5;

  public static final int MIN_SUCCESS_DISTANCE = 5;
  public static final int MAX_DISTANCE_TO_TARGET = 300;

  public Environment()
  {

    // TODO: Static parameters should read from input file

    // Initialize maze navigator and set initial position and initial heading
    this.navigator = new Character();
    this.navigator.setLocation(MazeParameters.INITIAL_POSITION);
    this.navigator.setHeading(MazeParameters.INITIAL_HEADING);

    // Set goal location
    this.goalLocation = MazeParameters.GOAL_LOCATION;

    // Set walls
    this.walls = MazeParameters.LINES;
  }

  /**
   * Runs a single time step of the simulation. This entails the navigator
   * moving to the new location and updating its range finders and radars to
   * appropriately reflect its new position in the maze.
   */
  public void runTimeStep()
  {

    // If the goal has already been reached, don't run another time step
    if (goalReached)
    {
      return;
    }

    // Move the navigator to the new location
    this.navigator.moveToNewLocation(this.walls);

    // Update the navigator's range finders and radars
    this.navigator.updateRangeFinders(this.walls);
    this.navigator.updateRadars(this.goalLocation);
  }

  /**
   * Collects the output of every range finder and radar and combines it with
   * the constant bias, yielding the inputs to the ANN.
   * 
   * @return Double array of inputs to the ANN.
   */
  public double[] getRawANNInputs()
  {

    int annInputCnt = 0;

    // Create ANN input array with a separate input for each range finder and
    // radar, as well as an additional input for the bias
    double[] annInputs = new double[this.navigator.getRangeFinders().size()
        + this.navigator.getRadars().size() + 1];

    // Set the bias
    annInputs[annInputCnt++] = 1;

    // Get the output of every range finder
    for (RangeFinder rangeFinder : this.navigator.getRangeFinders())
    {
      annInputs[annInputCnt++] = rangeFinder.getOutput()
          / rangeFinder.getRange();
    }

    // Get the output of every radar
    for (Radar radar : this.navigator.getRadars())
    {
      annInputs[annInputCnt++] = radar.getOutput();
    }

    return annInputs;
  }

  /**
   * Translates the ANN outputs to modified navigator angular velocity and
   * speed.
   * 
   * @param rotationQuantity
   *        The ANN output indicating the amount by which to adjust the
   *        rotation. This translates to navigator angular velocity.
   * @param propulsionQuantity
   *        The ANN output indicating the amount by which to adjust the
   *        propulsion. This translates to navigator speed.
   */
  public void translateAndApplyANNOutputs(
      double rotationQuantity,
      double propulsionQuantity)
  {

    // Adjust the angular velocity and speed based on the neural net outputs
    this.navigator.setAngularVelocity(this.navigator.getAngularVelocity()
        + (rotationQuantity - ANN_OUTPUT_SCALING_FACTOR));
    this.navigator.setSpeed(this.navigator.getSpeed()
        + (propulsionQuantity - ANN_OUTPUT_SCALING_FACTOR));

    // Impose navigator speed constraints
    if (this.navigator.getSpeed() > Character.MAX_SPEED)
    {
      this.navigator.setSpeed(Character.MAX_SPEED);
    }
    else if (this.navigator.getSpeed() < Character.MIN_SPEED)
    {
      this.navigator.setSpeed(Character.MIN_SPEED);
    }

    // Impose navigator angular velocity constraints
    if (this.navigator.getAngularVelocity() > Character.MAX_ANGULAR_VELOCITY)
    {
      this.navigator.setAngularVelocity(Character.MAX_ANGULAR_VELOCITY);
    }
    else if (this.navigator.getAngularVelocity() < Character.MIN_ANGULAR_VELOCITY)
    {
      this.navigator.setAngularVelocity(Character.MIN_ANGULAR_VELOCITY);
    }
  }

  /**
   * Computes the distance between the navigator's current location and the
   * target/goal. If the distance is within the minimum distance required to
   * solve the maze, the goal reached flag is set to indicate such resolution.
   * 
   * @return The euclidean distance from the navigator to the target.
   */
  public double getDistanceToTarget()
  {

    // Get the distance to the target based on the navigator's current location
    double distanceToTarget = this.navigator.getLocation().distance(
        goalLocation);

    // If the distance to the target is less than the success distance, set the
    // goal reached flag to true
    if (distanceToTarget < MIN_SUCCESS_DISTANCE)
    {
      this.goalReached = true;
    }

    return distanceToTarget;
  }

  public Character getNavigator()
  {
    return navigator;
  }

  public void setNavigator(Character navigator)
  {
    this.navigator = navigator;
  }

  public List<MazeLine> getWalls()
  {
    return walls;
  }

  public void setWalls(ArrayList<MazeLine> walls)
  {
    this.walls = walls;
  }

  public Point2D getGoalLocation()
  {
    return goalLocation;
  }

  public void setGoalLocation(MazePoint goalLocation)
  {
    this.goalLocation = goalLocation;
  }

  public boolean isGoalReached()
  {
    return goalReached;
  }

}
