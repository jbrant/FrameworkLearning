package edu.ucf.eplex.noveltysearch.mazeexperiment.configuration;

import java.util.ArrayList;
import java.util.List;

import edu.ucf.eplex.noveltysearch.mazeexperiment.components.MazeLine;
import edu.ucf.eplex.noveltysearch.mazeexperiment.components.MazePoint;

public class HardMazeParameters
{
  // TODO: All of this needs to be moved out to a configuration file

  public static final int LINE_COUNT = 13;

  public static final MazePoint INITIAL_POSITION = new MazePoint(36, 184);

  public static final int INITIAL_HEADING = 0;

  public static final MazePoint GOAL_LOCATION = new MazePoint(31, 20);

  public static final int TIMESTEPS = 400;

  @SuppressWarnings("serial")
  public static final List<MazeLine> LINES = new ArrayList<MazeLine>()
  {
    {
      add(new MazeLine(41, 5, 3, 8));
      add(new MazeLine(3, 8, 4, 49));
      add(new MazeLine(4, 49, 57, 53));
      add(new MazeLine(4, 49, 7, 202));
      add(new MazeLine(7, 202, 195, 198));
      add(new MazeLine(195, 198, 186, 8));
      add(new MazeLine(186, 8, 39, 5));
      add(new MazeLine(56, 54, 56, 157));
      add(new MazeLine(57, 106, 158, 162));
      add(new MazeLine(77, 201, 108, 164));
      add(new MazeLine(6, 80, 33, 121));
      add(new MazeLine(192, 146, 87, 91));
      add(new MazeLine(56, 55, 133, 30));
    }
  };
}
