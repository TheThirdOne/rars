package rars.venus.run;

import rars.Globals;
import rars.riscv.hardware.ControlAndStatusRegisterFile;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.hardware.Memory;
import rars.riscv.hardware.RegisterFile;
import rars.venus.ExecutePane;
import rars.venus.FileStatus;
import rars.venus.GuiAction;
import rars.venus.VenusUI;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Action  for the Run -> Backstep menu item
 */
public class RunBackstepAction extends GuiAction {

    private String name;
    private ExecutePane executePane;
    private VenusUI mainUI;

    public RunBackstepAction(String name, Icon icon, String descrip,
                             Integer mnemonic, KeyStroke accel, VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel);
        mainUI = gui;
    }

    /**
     * perform next simulated instruction step.
     */
    public void actionPerformed(ActionEvent e) {
        name = this.getValue(Action.NAME).toString();
        executePane = mainUI.getMainPane().getExecutePane();
        boolean done = false;
        if (!FileStatus.isAssembled()) {
            // note: this should never occur since backstepping is only enabled after successful assembly.
            JOptionPane.showMessageDialog(mainUI, "The program must be assembled before it can be run.");
            return;
        }
        mainUI.setStarted(true);
        mainUI.getMessagesPane().selectRunMessageTab();
        executePane.getTextSegmentWindow().setCodeHighlighting(true);

        if (Globals.getSettings().getBackSteppingEnabled()) {
            Memory.getInstance().addObserver(executePane.getDataSegmentWindow());
            RegisterFile.addRegistersObserver(executePane.getRegistersWindow());
            ControlAndStatusRegisterFile.addRegistersObserver(executePane.getControlAndStatusWindow());
            FloatingPointRegisterFile.addRegistersObserver(executePane.getFloatingPointWindow());
            Globals.program.getBackStepper().backStep();
            Memory.getInstance().deleteObserver(executePane.getDataSegmentWindow());
            RegisterFile.deleteRegistersObserver(executePane.getRegistersWindow());
            executePane.getRegistersWindow().updateRegisters();
            executePane.getFloatingPointWindow().updateRegisters();
            executePane.getControlAndStatusWindow().updateRegisters();
            executePane.getDataSegmentWindow().updateValues();
            executePane.getRegistersWindow().clearHighlighting();
            executePane.getFloatingPointWindow().clearHighlighting();
            executePane.getControlAndStatusWindow().clearHighlighting();
            executePane.getDataSegmentWindow().clearHighlighting();
            executePane.getTextSegmentWindow().highlightStepAtPC(); // Argument aded 25 June 2007
            Globals.getGui().getRegistersPane().setSelectedComponent(executePane.getRegistersWindow());
            FileStatus.set(FileStatus.RUNNABLE);
            // if we've backed all the way, disable the button
            //    if (Globals.program.getBackStepper().empty()) {
            //     ((AbstractAction)((AbstractButton)e.getSource()).getAction()).setEnabled(false);
            //}
         /*
         if (pe !=null) {
            RunGoAction.resetMaxSteps();
            mainUI.getMessagesPane().postMessage(
                                pe.errors().generateErrorReport());
            mainUI.getMessagesPane().postMessage(
                                "\n"+name+": execution terminated with errors.\n\n");
            mainUI.getRegistersPane().setSelectedComponent(executePane.getControlAndStatusWindow());
            FileStatus.set(FileStatus.TERMINATED); // should be redundant.
         					executePane.getTextSegmentWindow().setCodeHighlighting(true);
         	executePane.getTextSegmentWindow().unhighlightAllSteps();
            executePane.getTextSegmentWindow().highlightStepAtAddress(RegisterFile.getProgramCounter()-4);
         }
         */
            mainUI.setReset(false);
        }
    }
}