package edu.ucf.eplex.noveltysearch.mazeexperiment.components;

import javafx.geometry.Point2D;

public class MazePoint extends Point2D
{

  public MazePoint(double arg0, double arg1)
  {
    super(arg0, arg1);
  }

  /**
   * Rotates the current (projected) point to be in line with the navigator's
   * heading and scaled by the current location.
   * 
   * @param heading
   *        The heading of the navigator.
   * @param currentLocation
   *        The current location of the navigator.
   * @return The newly rotated projected point.
   */
  public MazePoint rotate(double heading, MazePoint currentLocation)
  {

    double x, y;

    // Get equivalent heading in radians
    double radianHeading = Math.toRadians(heading);

    // Subtract current location from the projected point
    x = getX() - currentLocation.getX();
    y = getY() - currentLocation.getY();

    // Rotate point by given angle in radians
    x = Math.cos(radianHeading) * x - Math.sin(radianHeading) * y;
    y = Math.sin(radianHeading) * x + Math.cos(radianHeading) * y;

    // Lastly, increment the point by the current location point
    x += currentLocation.getX();
    y += currentLocation.getY();

    return new MazePoint(x, y);
  }

  /**
   * Calculates the angle of the vector between the origin/navigator and this
   * point. In practice, this is used to determine the angle between the
   * navigator and the target (goal) for firing the appropriate pie-slice radar
   * on the navigator.
   * 
   * @return The angle produced by the vector between the origin and this point
   *         (the target/goal).
   */
  public double angle()
  {

    double vectorAngle = 0;

    // If the X coordinate is 0, the vector will lie directly above or below
    // the point (i.e. 90 or 270 degrees respectively)
    if (getX() == 0)
    {
      if (getY() > 0)
      {
        vectorAngle = 90;
      }
      else
      {
        vectorAngle = 270;
      }
    }
    // Otherwise, compute the arctangent to get the vector
    else
    {
      vectorAngle = (double) Math.toRadians(Math.atan(getY() / getX()));

      if (getX() <= 0)
      {
        vectorAngle += 180;
      }
    }

    return vectorAngle;
  }

}
