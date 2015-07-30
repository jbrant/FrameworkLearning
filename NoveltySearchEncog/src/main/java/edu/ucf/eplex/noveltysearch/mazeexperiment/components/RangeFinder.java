package edu.ucf.eplex.noveltysearch.mazeexperiment.components;

public class RangeFinder
{

  private double angle;
  private double range;
  private double output;

  public RangeFinder(double angle, double range, double output)
  {
    this.angle = angle;
    this.range = range;
    this.output = output;
  }

  public double getAngle()
  {
    return angle;
  }

  public void setAngle(double angle)
  {
    this.angle = angle;
  }

  public double getRange()
  {
    return range;
  }

  public void setRange(double range)
  {
    this.range = range;
  }

  public double getOutput()
  {
    return output;
  }

  public void setOutput(double output)
  {
    this.output = output;
  }

  @Override
  public String toString()
  {
    return "RangeFinder [angle=" + angle + ", range=" + range + ", output="
        + output + "]";
  }

}
