package edu.ucf.eplex.noveltysearch.mazeexperiment.components;

public class Radar
{

  private double minFieldOfViewAngle;
  private double maxFieldOfViewAngle;
  private double output;

  public Radar(
      double minFieldOfViewAngle,
      double maxFieldOfViewAngle,
      double output)
  {
    this.minFieldOfViewAngle = minFieldOfViewAngle;
    this.maxFieldOfViewAngle = maxFieldOfViewAngle;
    this.output = output;
  }

  public double getMinFieldOfViewAngle()
  {
    return minFieldOfViewAngle;
  }

  public void setMinFieldOfViewAngle(double minFieldOfViewAngle)
  {
    this.minFieldOfViewAngle = minFieldOfViewAngle;
  }

  public double getMaxFieldOfViewAngle()
  {
    return maxFieldOfViewAngle;
  }

  public void setMaxFieldOfViewAngle(double maxFieldOfViewAngle)
  {
    this.maxFieldOfViewAngle = maxFieldOfViewAngle;
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
    return "Radar [minFieldOfViewAngle=" + minFieldOfViewAngle
        + ", maxFieldOfViewAngle=" + maxFieldOfViewAngle + ", output="
        + output + "]";
  }

}
