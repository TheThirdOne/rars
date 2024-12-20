package rars.simulator;

import rars.SimulationException;
import rars.venus.run.RunSpeedPanel;

/**
 * Object provided to Observers of the Simulator.
 * They are notified at important phases of the runtime simulator,
 * such as start and stop of simulation.
 *
 * @author Pete Sanderson
 * @version January 2009
 */

public class SimulatorNotice {
    private int action;
    private int maxSteps;
    private Simulator.Reason reason;
    private boolean done;
    private SimulationException exception;
    private double runSpeed;
    private int programCounter;
    public static final int SIMULATOR_START = 0;
    public static final int SIMULATOR_STOP = 1;

    /**
     * Constructor will be called only within this package, so assume
     * address and length are in valid ranges.
     */
    public SimulatorNotice(int action, int maxSteps, double runSpeed, int programCounter, Simulator.Reason reason, SimulationException se, boolean done) {
        this.action = action;
        this.maxSteps = maxSteps;
        this.runSpeed = runSpeed;
        this.programCounter = programCounter;
        this.reason = reason;
        this.exception = se;
        this.done = done;
    }

    public int getAction() {
        return this.action;
    }

    public int getMaxSteps() {
        return this.maxSteps;
    }

    public double getRunSpeed() {
        return this.runSpeed;
    }

    public int getProgramCounter() {
        return this.programCounter;
    }

    public Simulator.Reason getReason() {
        return this.reason;
    }

    public SimulationException getException() {
        return this.exception;
    }

    public boolean getDone() {
        return this.done;
    }

    /**
     * String representation indicates access type, address and length in bytes
     */
    public String toString() {
        return ((this.getAction() == SIMULATOR_START) ? "START " : "STOP  ") +
                "Max Steps " + this.maxSteps + " " +
                "Speed " + ((this.runSpeed == RunSpeedPanel.UNLIMITED_SPEED) ? "unlimited " : "" + this.runSpeed + " inst/sec") +
                "Prog Ctr " + this.programCounter;
    }
}