/*
 * Encog(tm) Java Examples v3.3 http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-examples
 * 
 * Copyright 2008-2014 Heaton Research, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 * 
 * For more information on Heaton Research copyrights, licenses and trademarks
 * visit: http://www.heatonresearch.com/copyright
 */
package edu.ucf.eplex.encoglearning.neat.lunarneat;

import org.encog.Encog;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.ml.ea.train.basic.BasicEA;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.NEATUtil;

/**
 * A lunar lander game where the neural network learns to land a space craft.
 * The neural network learns the proper amount of thrust to land softly and
 * conserve fuel.
 * 
 * This example is unique because it uses supervised training, yet does not
 * have expected values. For this it can use genetic algorithms or simulated
 * annealing.
 */
public class LunarLander
{

  public static NEATPopulation createNeatPopulation()
  {

    NEATPopulation population = new NEATPopulation(3, 1, 200);    
    population.setInitialConnectionDensity(1.0);
    population.reset();

    return population;
  }

  public static void main(String args[])
  {

    EvolutionaryAlgorithm train = NEATUtil.constructNEATTrainer(
        createNeatPopulation(),
        new PilotScore());
    //((BasicEA) train).setThreadCount(1);

    int epoch = 1;

    for (int i = 0; i < 500000; i++)
    {
      train.iteration();
      System.out.println(String.format(
          "Epoch #%d, Score: %f, Links: %d",
          epoch,
          train.getError(),
          ((NEATNetwork) train.getCODEC().decode(train.getBestGenome()))
              .getLinks().length));
      epoch++;
    }
    train.finishTraining();

    System.out.println("\nHow the winning network landed:");
    // BasicNetwork network = (BasicNetwork) train.getMethod();
    // NeuralPilot pilot = new NeuralPilot(network, true);
    // System.out.println(pilot.scorePilot());

    Encog.getInstance().shutdown();
  }
}
