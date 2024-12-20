package rars.venus.run;

import rars.Globals;
import rars.Settings;
import rars.SimulationException;
import rars.riscv.hardware.RegisterFile;
import rars.simulator.ProgramArgumentList;
import rars.simulator.Simulator;
import rars.simulator.SimulatorNotice;
import rars.util.SystemIO;
import rars.venus.ExecutePane;
import rars.venus.FileStatus;
import rars.venus.GuiAction;
import rars.venus.VenusUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;

/**
 * Action class for the Run -> Go menu item (and toolbar icon)
 */
public class RunGoAction extends GuiAction {

    public static int defaultMaxSteps = -1; // "forever", formerly 10000000; // 10 million
    public static int maxSteps = defaultMaxSteps;
    private VenusUI mainUI;

    public RunGoAction(String name, Icon icon, String descrip,
                       Integer mnemonic, KeyStroke accel, VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel);
        mainUI = gui;
    }

    /**
     * Action to take when GO is selected -- run the MIPS program!
     */
    public void actionPerformed(ActionEvent e) {
        String name = this.getValue(Action.NAME).toString();
        if (!mainUI.onStartedSimulation(name)) {
            return;
        }
        mainUI.setMenuState(FileStatus.RUNNING);

        // Setup cleanup procedures for the simulation
        final Observer stopListener =
                new Observer() {
                    public void update(Observable o, Object simulator) {
                        SimulatorNotice notice = ((SimulatorNotice) simulator);
                        if (notice.getAction() != SimulatorNotice.SIMULATOR_STOP) return;
                        EventQueue.invokeLater(() -> mainUI.onStoppedSimulation(name, notice));
                        o.deleteObserver(this);
                    }
                };
        Simulator.getInstance().addObserver(stopListener);

        ExecutePane executePane = mainUI.getMainPane().getExecutePane();
        int[] breakPoints = executePane.getTextSegmentWindow().getSortedBreakPointsArray();
        Globals.program.startSimulation(maxSteps, breakPoints);
    }

    /**
     * Reset max steps limit to default value at termination of a simulated execution.
     */

    public static void resetMaxSteps() {
        maxSteps = defaultMaxSteps;
    }
}