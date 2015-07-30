package edu.ucf.eplex.encoglearning.neat.doublepolebalancing.fitness;

import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.neat.NEATNetwork;
import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizedField;

import edu.ucf.eplex.encoglearning.neat.doublepolebalancing.gui.PoleBalanceDisplay;
import edu.ucf.eplex.encoglearning.neat.doublepolebalancing.simulator.SimulationParameters;

public class PoleBalancingFitnessFunction
{

  private NEATNetwork network;

  private NormalizedField cartPosition;
  private NormalizedField cartVelocity;
  private NormalizedField longPoleAngle;
  private NormalizedField longPoleAngularVelocity;
  private NormalizedField shortPoleAngle;
  private NormalizedField shortPoleAngularVelocity;

  private PoleBalanceDisplay display;

  public PoleBalancingFitnessFunction(NEATNetwork network)
  {
    this.network = network;

    this.cartPosition = new NormalizedField(
        NormalizationAction.Normalize,
        "cartPosition",
        SimulationParameters.TRACK_LENGTH,
        0,
        1,
        0);
    this.cartVelocity = new NormalizedField(
        NormalizationAction.Normalize,
        "cartVelocity",
        2,
        -2,
        1,
        0);
    this.longPoleAngle = new NormalizedField(
        NormalizationAction.Normalize,
        "longPoleAngle",
        Math.PI / 2,
        -(Math.PI / 2),
        1,
        0);
    this.longPoleAngularVelocity = new NormalizedField(
        NormalizationAction.Normalize,
        "longPoleAngularVelocity",
        2,
        -2,
        1,
        0);
    this.shortPoleAngle = new NormalizedField(
        NormalizationAction.Normalize,
        "shortPoleAngle",
        Math.PI / 2,
        -(Math.PI / 2),
        1,
        0);
    this.shortPoleAngularVelocity = new NormalizedField(
        NormalizationAction.Normalize,
        "shortPoleAngularVelocity",
        2,
        -2,
        1,
        0);
  }

  public double scoreTrial()
  {

    double fitness = 0;

    double trackLengthHalfed = SimulationParameters.TRACK_LENGTH / 2;

    // Initialize state variables
    double[] state = initNewState();

    // Run the simulation for each timestep
    int currentTimestep;
    for (currentTimestep = 0; currentTimestep < SimulationParameters.TIMESTEPS; currentTimestep++)
    {

      // Probably need to rework this normalization
      double[] networkInput = new double[7];
      networkInput[0] = state[0] / trackLengthHalfed;
      networkInput[1] = state[1] / 0.75;
      networkInput[2] = state[2]
          / SimulationParameters.ANGLE_BALANCED_THRESHOLD;
      networkInput[3] = state[3];
      networkInput[4] = state[4]
          / SimulationParameters.ANGLE_BALANCED_THRESHOLD;
      networkInput[5] = state[5];
      networkInput[6] = 1; // bias

      // Construct input data from input vector
      MLData inputData = new BasicMLData(networkInput);

      // Activate the network.
      double networkOutput = this.network.compute(inputData).getData(0);

      // Apply the output force to the system (updating state vector)
      performAction(networkOutput, state);

      if (display != null)
      {
        display.step(currentTimestep, state[0], new double[]
        {
            state[2], state[4]
        });
      }

      // Check for failure state. Has the cart run off the ends of the track or
      // has the pole angle gone beyond the threshold.
      if ((state[0] < -trackLengthHalfed) || (state[0] > trackLengthHalfed)
          || (state[2] > SimulationParameters.ANGLE_BALANCED_THRESHOLD)
          || (state[2] < -SimulationParameters.ANGLE_BALANCED_THRESHOLD)
          || (state[4] > SimulationParameters.ANGLE_BALANCED_THRESHOLD)
          || (state[4] < -SimulationParameters.ANGLE_BALANCED_THRESHOLD))
      {
        // System.out.println("Why are you not hitting this breakpoint?????");
        break;
      }
    }

    fitness = currentTimestep;

    return fitness;
  }

  /**
   * The following mappings for state:
   * 
   * [0] - Cart Position (meters).
   * 
   * [1] - Cart velocity (m/s).
   * 
   * [2] - Long pole angle (radians)
   * 
   * [3] - Long pole angular velocity (radians/sec).
   * 
   * [4] - Short pole angle (radians)
   * 
   * [5] - Short pole angular velocity (radians/sec).
   * 
   * @return array of inputs.
   */
  private double[] initNewState()
  {
    double[] state = new double[6];

    // Set starting cart position, cart velocity, long pole angular velocity,
    // and short pole angular velocity to 0
    state[0] = state[1] = state[3] = state[5] = 0;

    // Set starting angles of long and short poles
    state[2] = SimulationParameters.LONG_POLE_STARTING_ANGLE;
    state[4] = SimulationParameters.SHORT_POLE_STARTING_ANGLE;

    return state;
  }

  private void performAction(double output, double[] state)
  {
    int i;
    double[] dydx = new double[6];

    /*--- Apply action to the simulated cart-pole ---*/
    for (i = 0; i < 2; ++i)
    {
      dydx[0] = state[1];
      dydx[2] = state[3];
      dydx[4] = state[5];
      step(output, state, dydx);
      rk4(output, state, dydx, state);
    }
  }

  private void step(double action, double[] st, double[] derivs)
  {
    double force, costheta_1, costheta_2, sintheta_1, sintheta_2, gsintheta_1, gsintheta_2, temp_1, temp_2, ml_1, ml_2, fi_1, fi_2, mi_1, mi_2;

    force = (action - 0.5) * SimulationParameters.FORCE_MAGNITUDE * 2;
    costheta_1 = Math.cos(st[2]);
    sintheta_1 = Math.sin(st[2]);
    gsintheta_1 = SimulationParameters.GRAVITY * sintheta_1;
    costheta_2 = Math.cos(st[4]);
    sintheta_2 = Math.sin(st[4]);
    gsintheta_2 = SimulationParameters.GRAVITY * sintheta_2;

    ml_1 = SimulationParameters.LONG_POLE_LENGTH
        * SimulationParameters.LONG_POLE_MASS;
    ml_2 = SimulationParameters.SHORT_POLE_LENGTH
        * SimulationParameters.SHORT_POLE_MASS;
    temp_1 = SimulationParameters.POLE_FRICTION_COEFF * st[3] / ml_1;
    temp_2 = SimulationParameters.POLE_FRICTION_COEFF * st[5] / ml_2;

    fi_1 = (ml_1 * st[3] * st[3] * sintheta_1)
        + (0.75 * SimulationParameters.LONG_POLE_MASS * costheta_1 * (temp_1 + gsintheta_1));

    fi_2 = (ml_2 * st[5] * st[5] * sintheta_2)
        + (0.75 * SimulationParameters.SHORT_POLE_MASS * costheta_2 * (temp_2 + gsintheta_2));

    mi_1 = SimulationParameters.LONG_POLE_MASS
        * (1 - (0.75 * costheta_1 * costheta_1));
    mi_2 = SimulationParameters.SHORT_POLE_MASS
        * (1 - (0.75 * costheta_2 * costheta_2));

    derivs[1] = (force + fi_1 + fi_2)
        / (mi_1 + mi_2 + SimulationParameters.CART_MASS);
    derivs[3] = -0.75 * (derivs[1] * costheta_1 + gsintheta_1 + temp_1)
        / SimulationParameters.LONG_POLE_LENGTH;
    derivs[5] = -0.75 * (derivs[1] * costheta_2 + gsintheta_2 + temp_2)
        / SimulationParameters.SHORT_POLE_LENGTH;
  }

  private void rk4(double f, double[] y, double[] dydx, double[] yout)
  {
    int i;

    double hh, h6;
    double[] dym = new double[6];
    double[] dyt = new double[6];
    double[] yt = new double[6];

    hh = SimulationParameters.TIME_DELTA * 0.5;
    h6 = SimulationParameters.TIME_DELTA / 6.0;
    for (i = 0; i <= 5; i++)
      yt[i] = y[i] + hh * dydx[i];
    step(f, yt, dyt);
    dyt[0] = yt[1];
    dyt[2] = yt[3];
    dyt[4] = yt[5];
    for (i = 0; i <= 5; i++)
      yt[i] = y[i] + hh * dyt[i];
    step(f, yt, dym);
    dym[0] = yt[1];
    dym[2] = yt[3];
    dym[4] = yt[5];
    for (i = 0; i <= 5; i++)
    {
      yt[i] = y[i] + SimulationParameters.TIME_DELTA * dym[i];
      dym[i] += dyt[i];
    }
    step(f, yt, dyt);
    dyt[0] = yt[1];
    dyt[2] = yt[3];
    dyt[4] = yt[5];
    for (i = 0; i <= 5; i++)
      yout[i] = y[i] + h6 * (dydx[i] + dyt[i] + 2.0 * dym[i]);
  }

  public void enableDisplay()
  {
    display = new PoleBalanceDisplay(
        SimulationParameters.TRACK_LENGTH,
        new double[]
        {
            SimulationParameters.LONG_POLE_LENGTH,
            SimulationParameters.SHORT_POLE_LENGTH
        },
        SimulationParameters.TIMESTEPS);
    display.setVisible(true);
  }
}