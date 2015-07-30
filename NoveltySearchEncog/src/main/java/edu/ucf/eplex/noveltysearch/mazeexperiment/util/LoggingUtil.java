package edu.ucf.eplex.noveltysearch.mazeexperiment.util;

import java.io.FileWriter;
import java.io.IOException;

import com.opencsv.CSVWriter;

import edu.ucf.eplex.noveltysearch.mazeexperiment.components.Character;

public class LoggingUtil
{

  private static CSVWriter WRITER;
  private static boolean HEADER_WRITTEN = false;

  public static void configureLogger(String filename)
  {

    try
    {
      WRITER = new CSVWriter(new FileWriter(filename));
    }
    catch (IOException e)
    {
      System.err.println("Failed to initialize CSV writer.");
      e.printStackTrace();
    }
  }

  private static void writeHeader()
  {

    String[] header = new String[23];

    header[0] = "Generation";
    header[1] = "Trial";
    header[2] = "Location X";
    header[3] = "Location Y";
    header[4] = "Heading";
    header[5] = "Angular Velocity";
    header[6] = "Distance to Target";
    header[7] = "Radar 1 Output";
    header[8] = "Radar 2 Output";
    header[9] = "Radar 3 Output";
    header[10] = "Radar 4 Output";
    header[11] = "RangeFinder 1 Range";
    header[12] = "RangeFinder 1 Output";
    header[13] = "RangeFinder 2 Range";
    header[14] = "RangeFinder 2 Output";
    header[15] = "RangeFinder 3 Range";
    header[16] = "RangeFinder 3 Output";
    header[17] = "RangeFinder 4 Range";
    header[18] = "RangeFinder 4 Output";
    header[19] = "RangeFinder 5 Range";
    header[20] = "RangeFinder 5 Output";
    header[21] = "RangeFinder 6 Range";
    header[22] = "RangeFinder 6 Output";

    WRITER.writeNext(header);
  }

  public static void writeTrialBehavior(
      int generation,
      int trial,
      double distanceToTarget,
      Character navigator)
  {

    if (HEADER_WRITTEN == false)
    {
      writeHeader();
      HEADER_WRITTEN = true;
    }

    String[] currentStatistics = new String[23];

    currentStatistics[0] = String.valueOf(generation);
    currentStatistics[1] = String.valueOf(trial);
    currentStatistics[2] = String.valueOf(navigator.getLocation().getX());
    currentStatistics[3] = String.valueOf(navigator.getLocation().getY());
    currentStatistics[4] = String.valueOf(navigator.getHeading());
    currentStatistics[5] = String.valueOf(navigator.getAngularVelocity());
    currentStatistics[6] = String.valueOf(distanceToTarget);
    currentStatistics[7] = String.valueOf(navigator.getRadar(0).getOutput());
    currentStatistics[8] = String.valueOf(navigator.getRadar(1).getOutput());
    currentStatistics[9] = String.valueOf(navigator.getRadar(2).getOutput());
    currentStatistics[10] = String.valueOf(navigator.getRadar(3).getOutput());
    currentStatistics[11] = String.valueOf(navigator.getRangeFinder(0)
        .getRange());
    currentStatistics[12] = String.valueOf(navigator.getRangeFinder(0)
        .getOutput());
    currentStatistics[13] = String.valueOf(navigator.getRangeFinder(1)
        .getRange());
    currentStatistics[14] = String.valueOf(navigator.getRangeFinder(1)
        .getOutput());
    currentStatistics[15] = String.valueOf(navigator.getRangeFinder(2)
        .getRange());
    currentStatistics[16] = String.valueOf(navigator.getRangeFinder(2)
        .getOutput());
    currentStatistics[17] = String.valueOf(navigator.getRangeFinder(3)
        .getRange());
    currentStatistics[18] = String.valueOf(navigator.getRangeFinder(3)
        .getOutput());
    currentStatistics[19] = String.valueOf(navigator.getRangeFinder(4)
        .getRange());
    currentStatistics[20] = String.valueOf(navigator.getRangeFinder(4)
        .getOutput());
    currentStatistics[21] = String.valueOf(navigator.getRangeFinder(5)
        .getRange());
    currentStatistics[22] = String.valueOf(navigator.getRangeFinder(5)
        .getOutput());

    WRITER.writeNext(currentStatistics);
  }

  public static void closeLogger()
  {
    try
    {
      WRITER.close();
    }
    catch (IOException e)
    {
      System.err.println("Failed to close CSV writer.");
      e.printStackTrace();
    }
  }
}
