package edu.ucf.eplex.noveltysearch.mazeexperiment.components;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;

public class Character
{

  private double heading;
  private double speed;
  private double angularVelocity;
  private double radius;
  private MazePoint location;

  private List<Radar> radars;
  private List<RangeFinder> rangeFinders;

  protected static final double MIN_SPEED = -3.0;
  protected static final double MAX_SPEED = 3.0;
  protected static final double MIN_ANGULAR_VELOCITY = -3.0;
  protected static final double MAX_ANGULAR_VELOCITY = 3.0;

  @SuppressWarnings("serial")
  public Character()
  {

    // Initialize character parameters
    this.heading = 0.0F;
    this.speed = 0.0F;
    this.angularVelocity = 0.0F;
    this.radius = 0.0F;

    // Initialize range finders
    this.rangeFinders = new ArrayList<RangeFinder>()
    {
      {
        add(new RangeFinder(-180.0F, 100.0F, 0.0F));
        add(new RangeFinder(-90.0F, 100.0F, 0.0F));
        add(new RangeFinder(-45.0F, 100.0F, 0.0F));
        add(new RangeFinder(-0.0F, 100.0F, 0.0F));
        add(new RangeFinder(45.0F, 100.0F, 0.0F));
        add(new RangeFinder(90.0F, 100.0F, 0.0F));
      }
    };

    // Initialize radars
    this.radars = new ArrayList<Radar>()
    {
      {
        add(new Radar(45.0F, 135.0F, 0.0F));
        add(new Radar(135.0F, 225.0F, 0.0F));
        add(new Radar(225.0F, 315.0F, 0.0F));
        add(new Radar(315.0F, 405.0F, 0.0F));
      }
    };
  }

  /**
   * Determine whether a collision will occur based on the wall locations and
   * the newly proposed navigator location.
   * 
   * @param newLocation
   *        The proposed new navigator location.
   * @param walls
   *        The walls in the maze.
   * @return Boolean value indicating whether the move will result in a
   *         collision.
   */
  private boolean isCollision(MazePoint newLocation, List<MazeLine> walls)
  {

    boolean doesCollide = false;

    // Iterate through all of the walls, determining if traversal to the newly
    // proposed location will result in a collision
    for (MazeLine wall : walls)
    {

      // If the distance between the wall and new location is less than the
      // radius of the navigator itself, then a collision will occur
      if (wall.getDistanceToPoint(newLocation) < this.radius)
      {
        doesCollide = true;
        break;
      }
    }

    return doesCollide;
  }

  /**
   * Move the navigator to the new location (one time step of simulated
   * runtime).
   * 
   * @param walls
   *        The walls in the maze.
   */
  public void moveToNewLocation(List<MazeLine> walls)
  {

    // Compute angular velocity components
    double angularVelocityX = Math.cos(Math.toRadians(this.getHeading())
        * this.getSpeed());
    double angularVelocityY = Math.sin(Math.toRadians(this.getHeading())
        * this.getSpeed());

    // Set the new heading by incrementing by the angular velocity
    this.setHeading(this.heading + this.angularVelocity);

    // If the navigator's resulting heading is greater than 360 degrees, it has
    // performed more than a complete rotation, so subtract 360 to have a valid
    // heading
    if (this.heading > 360)
    {
      this.setHeading(this.getHeading() - 360);
    }
    // On the other hand, if the heading is negative, the same has happened but
    // in the other direction. So add 360 degrees
    else if (this.heading < 0)
    {
      this.setHeading(this.getHeading() + 360);
    }

    // Determine the new location, incremented by the X and Y component
    // velocities
    MazePoint newLocation = new MazePoint(angularVelocityX
        + this.location.getX(), angularVelocityY + this.location.getY());

    // Set the navigator to that new location only if that doesn't result in a
    // wall collision
    if (isCollision(newLocation, walls) == false)
    {
      this.location = new MazePoint(newLocation.getX(), newLocation.getY());
    }
  }

  /**
   * Update range finders based on new trajectory so that they do not penetrate
   * walls.
   * 
   * @param walls
   *        The list of walls in the environment.
   */
  public void updateRangeFinders(List<MazeLine> walls)
  {

    for (RangeFinder rangeFinder : this.rangeFinders)
    {

      // Convert range finder angle to radians
      double radianAngle = Math.toRadians(rangeFinder.getAngle());

      // Project a point from navigator location outwards
      MazePoint projectedPoint = new MazePoint(
          this.location.getX() + Math.cos(radianAngle)
              * rangeFinder.getRange(),
          this.location.getY() + Math.sin(radianAngle)
              * rangeFinder.getRange());

      // Rotate the point based on the navigator's heading
      projectedPoint = projectedPoint.rotate(this.heading, this.location);

      // Create a line segment from the navigator's current location to the
      // projected point
      MazeLine projectedLine = new MazeLine(this.location, projectedPoint);

      // Initialize the range to the maximum range of the range finder sensor
      double adjustedRange = rangeFinder.getRange();

      // Iterate through all the walls to see if we're going to hit a wall
      for (MazeLine wall : walls)
      {

        // Get intersection point between wall and projected trajectory (if one
        // exists)
        MazePoint wallIntersect = wall.getIntersectionPoint(projectedLine);

        // If trajectory does intersect with a wall, adjust the range as the
        // range finder cannot penetrate walls
        if (wallIntersect != null)
        {

          // Get the distance from the wall
          double wallRange = wallIntersect.distance(this.location);

          // If the adjusted range is shorter than the maximum range finder
          // range, adopt the shorter range
          if (wallRange < rangeFinder.getRange())
          {
            adjustedRange = wallRange;
          }
        }
      }

      // Update the range finder range (which may have been adjusted so as to
      // not penetrate walls)
      rangeFinder.setOutput(adjustedRange);
    }
  }

  /**
   * Update pie-slice radars to fire if the target is within their
   * field-of-view.
   * 
   * @param goalLocation
   *        The goal location point.
   */
  public void updateRadars(final MazePoint goalLocation)
  {

    MazePoint target = goalLocation;

    // Rotate the target with respect to the heading of the navigator
    target = target.rotate(-this.heading, this.location);

    // Offset by the navigator's current location
    target = new MazePoint(target.getX() - this.location.getX(), target.getY()
        - this.location.getY());

    // Get the angle between the navigator and the target
    double navigatorTargetAngle = target.angle();

    for (Radar radar : this.radars)
    {

      // Initialize the radar output to 0 by default (meaning that the vector
      // formed between the target and the navigator does not fall within the
      // radar field-of-view)
      radar.setOutput(0);

      // If the angle falls within the field-of-view of the radar, activate the
      // radar output
      if (navigatorTargetAngle >= radar.getMinFieldOfViewAngle()
          && navigatorTargetAngle < radar.getMaxFieldOfViewAngle())
      {
        radar.setOutput(1);
      }
      // Otherwise, add 360 and perform the same comparison to handle negative
      // angles
      else if (navigatorTargetAngle + 360 >= radar.getMinFieldOfViewAngle()
          && navigatorTargetAngle + 360 < radar.getMaxFieldOfViewAngle())
      {
        radar.setOutput(1);
      }
    }
  }

  public double getHeading()
  {
    return heading;
  }

  public void setHeading(double heading)
  {
    this.heading = heading;
  }

  public double getSpeed()
  {
    return speed;
  }

  public void setSpeed(double speed)
  {
    this.speed = speed;
  }

  public double getAngularVelocity()
  {
    return angularVelocity;
  }

  public void setAngularVelocity(double angularVelocity)
  {
    this.angularVelocity = angularVelocity;
  }

  public Point2D getLocation()
  {
    return location;
  }

  public void setLocation(MazePoint location)
  {
    this.location = location;
  }

  public List<Radar> getRadars()
  {
    return radars;
  }

  public void setRadars(List<Radar> radars)
  {
    this.radars = radars;
  }

  public Radar getRadar(int radarIndex)
  {
    return this.radars.get(radarIndex);
  }

  public List<RangeFinder> getRangeFinders()
  {
    return rangeFinders;
  }

  public void setRangeFinders(List<RangeFinder> rangeFinders)
  {
    this.rangeFinders = rangeFinders;
  }

  public RangeFinder getRangeFinder(int rangeFinderIndex)
  {
    return this.rangeFinders.get(rangeFinderIndex);
  }

  @Override
  public String toString()
  {
    return "Character [heading=" + heading + ", speed=" + speed
        + ", angularVelocity=" + angularVelocity + ", point=" + location + "]";
  }
}
