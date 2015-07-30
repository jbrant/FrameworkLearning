package edu.ucf.eplex.encoglearning.neat.doublepolebalancing.simulator;

public class SimulationParameters
{

  // Angular measurements
  public static final double THIRTYSIX_DEGREES = Math.PI / 5.0;
  private static final double ONE_DEGREE = Math.PI / 180.0;

  // Experimental parameters
  public final static double TRACK_LENGTH = 4.8;
  public final static int TIMESTEPS = 10000;
  public final static int NUM_TRIALS = 10;
  public final static boolean INPUT_VELOCITY_GIVEN = true;

  // Pole parameters
  public final static double ANGLE_BALANCED_THRESHOLD = THIRTYSIX_DEGREES;
  public final static double LONG_POLE_LENGTH = 0.5 / 2;
  public final static double LONG_POLE_STARTING_ANGLE = ONE_DEGREE;
  public final static double LONG_POLE_MASS = 0.1;
  public final static double SHORT_POLE_LENGTH = 0.05 / 2;
  public final static double SHORT_POLE_STARTING_ANGLE = 0;
  public final static double SHORT_POLE_MASS = 0.01;

  // Physical constants
  public static final double GRAVITY = -9.8;
  public static final double CART_MASS = 1.0;
  public static final double FORCE_MAGNITUDE = 10.0;

  // State update parameters
  public static final double TIME_DELTA = 0.01;
  public static final double CART_FRICTION_COEFF = 0.0005;
  public static final double POLE_FRICTION_COEFF = 0.000002;
}
