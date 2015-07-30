package edu.ucf.eplex.noveltysearch.mazeexperiment.components;

import java.awt.geom.Line2D;

import javafx.scene.shape.Line;

public class MazeLine extends Line
{

  private MazePoint startPoint;
  private MazePoint endPoint;

  /**
   * MazeLine constructor.
   * 
   * @param startPoint
   *        The starting point.
   * @param endPoint
   *        The ending point.
   */
  public MazeLine(MazePoint startPoint, MazePoint endPoint)
  {
    super(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint
        .getY());

    this.startPoint = startPoint;
    this.endPoint = endPoint;
  }

  /**
   * MazeLine constructor.
   * 
   * @param startX
   *        The X-coordinate of the starting point.
   * @param startY
   *        The Y-coordinate of the starting point.
   * @param endX
   *        The X-coordinate of the ending point.
   * @param endY
   *        The Y-coordinate of the ending point.
   */
  public MazeLine(double startX, double startY, double endX, double endY)
  {
    super(startX, startY, endX, endY);
  }

  /**
   * Computes the midpoint of the line segment.
   * 
   * @return The midpoint of the line segment.
   */
  public MazePoint getMidpoint()
  {
    return new MazePoint(
        (getStartX() + getEndX()) / 2,
        (getStartY() + getEndY()) / 2);
  }

  /**
   * Calculates the point-of-intersection for a given comparison line. The
   * equation for line intersection can be found at:
   * https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection
   * 
   * @param compareLine
   *        The line against which to compute a possible intersection.
   * @return The point of intersection between the two line segments or "null"
   *         if they don't intersect.
   */
  public MazePoint getIntersectionPoint(MazeLine compareLine)
  {

    MazePoint intersectionPoint = null;

    // Calculate determinants denominator
    double denom = (getStartX() - getEndX())
        * (compareLine.getStartY() - compareLine.getEndY())
        - (getStartY() - getEndY())
        * (compareLine.getStartX() - compareLine.getEndX());

    // Calculate the intersection point only if the determinant denominator is
    // not 0; otherwise, the lines are parallel
    if (denom != 0)
    {

      // Calculate the determinants
      double xDeterminant = ((getStartX() * getEndY() - getStartY()
          * getEndX())
          * (compareLine.getStartX() - compareLine.getEndX()) - (getStartX() - getEndX())
          * (compareLine.getStartX() * compareLine.getEndY() - compareLine
              .getStartY() * compareLine.getEndX()))
          / denom;
      double yDeterminant = ((getStartX() * getEndY() - getStartY()
          * getEndX())
          * (compareLine.getStartY() - compareLine.getEndY()) - (getStartY() - getEndY())
          * (compareLine.getStartX() * compareLine.getEndY() - compareLine
              .getStartY() * compareLine.getEndX()))
          / denom;

      // Ensure that the intersection point actually lies within both line
      // segments
      if (xDeterminant >= Math.min(getStartX(), getEndX())
          && xDeterminant <= Math.max(getStartX(), getEndX())
          && (xDeterminant >= Math.min(
              compareLine.getStartX(),
              compareLine.getEndX()) && (xDeterminant <= Math.max(
              compareLine.getStartX(),
              compareLine.getEndX()))))
      {
        intersectionPoint = new MazePoint(xDeterminant, yDeterminant);
      }
    }

    return intersectionPoint;
  }

  /**
   * Calculates the distance between the shortest possible distance between the
   * given point and the line segment.
   * 
   * @param point
   *        The starting point for the distance calculation.
   * @return The shortest distance between the given point and line segment.
   */
  public double getDistanceToPoint(MazePoint point)
  {

    // Calculate the shortest distance between the given point and the line
    // segment
    return Line2D.ptLineDist(
        getStartX(),
        getStartY(),
        getEndX(),
        getEndY(),
        point.getX(),
        point.getY());
  }

  public double getDistanceToPointOld(MazePoint point)
  {

    double finalDistance = 0;

    double numerator = (point.getX() - getStartX())
        * (getEndX() - getStartX()) + (point.getY() - getStartY())
        * (getEndY() - getStartY());
    double denominator = getLength();

    denominator *= denominator;

    double result = numerator / denominator;

    if (result < 0 || result > 1)
    {
      double startPointDistance = startPoint.distance(point);
      double endPointdistance = endPoint.distance(point);

      if (startPointDistance < endPointdistance)
      {
        finalDistance = startPointDistance;
      }
      else
      {
        finalDistance = endPointdistance;
      }
    }
    else
    {
      MazePoint tempPoint = new MazePoint(getStartX() + result
          * (getEndX() - getStartX()), getStartY() + result
          * (getEndY() - getStartY()));
      finalDistance = tempPoint.distance(point);
    }

    return finalDistance;
  }

  /**
   * Computes the length of the line segment.
   * 
   * @return The length of the line segment.
   */
  public double getLength()
  {
    return startPoint.distance(endPoint);
  }
}
