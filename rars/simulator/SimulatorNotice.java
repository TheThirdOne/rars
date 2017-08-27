package rars.simulator;

/*
Copyright (c) 2003-2009,  Pete Sanderson and Kenneth Vollmar

Developed by Pete Sanderson (psanderson@otterbein.edu)
and Kenneth Vollmar (kenvollmar@missouristate.edu)

Permission is hereby granted, free of charge, to any person obtaining 
a copy of this software and associated documentation files (the 
"Software"), to deal in the Software without restriction, including 
without limitation the rights to use, copy, modify, merge, publish, 
distribute, sublicense, and/or sell copies of the Software, and to 
permit persons to whom the Software is furnished to do so, subject 
to the following conditions:

The above copyright notice and this permission notice shall be 
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(MIT license, http://www.opensource.org/licenses/mit-license.html)
 */

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