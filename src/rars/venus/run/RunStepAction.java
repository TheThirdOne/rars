package rars.venus.run;

import rars.Globals;
import rars.Settings;
import rars.SimulationException;
import rars.riscv.hardware.RegisterFile;
import rars.simulator.ProgramArgumentList;
import rars.simulator.Simulator;
import rars.simulator.SimulatorNotice;
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
 * Action  for the Run -> Step menu item
 */
public class RunStepAction extends GuiAction {

    private VenusUI mainUI;

    public RunStepAction(String name, Icon icon, String descrip,
                         Integer mnemonic, KeyStroke accel, VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel);
        mainUI = gui;
    }

    /**
     * perform next simulated instruction step.
     */
    public void actionPerformed(ActionEvent e) {
        String name = this.getValue(Action.NAME).toString();
        if (!mainUI.onStartedSimulation(name)) {
            return;
        }

        // Setup callback for after step finishes
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
        Globals.program.startSimulation(1, null);
    }
}
