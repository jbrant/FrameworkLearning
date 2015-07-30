package edu.ucf.eplex.noveltysearch.mazeexperiment.gui;

import edu.ucf.eplex.noveltysearch.mazeexperiment.configuration.MazeParameters;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

public class MazeScene extends Application
{

  public static void main(String[] args)
  {
    launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception
  {

    // Set the title
    stage.setTitle("Novelty Maze");

    Group root = new Group();

    root.getChildren().addAll(MazeParameters.LINES);

    stage.setScene(new Scene(root));

    // Display the stage
    stage.show();
  }

}
