package rars.venus.run;

import rars.AssemblyException;
import rars.Globals;
import rars.riscv.hardware.*;
import rars.util.SystemIO;
import rars.venus.ExecutePane;
import rars.venus.FileStatus;
import rars.venus.GuiAction;
import rars.venus.VenusUI;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Action  for the Run -> Reset menu item
 */
public class RunResetAction extends GuiAction {
    private VenusUI mainUI;

    public RunResetAction(String name, Icon icon, String descrip,
                          Integer mnemonic, KeyStroke accel, VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel);
        mainUI = gui;
    }

    /**
     * reset GUI components and MIPS resources
     */
    public void actionPerformed(ActionEvent e) {
        RunGoAction.resetMaxSteps();
        String name = this.getValue(Action.NAME).toString();
        ExecutePane executePane = mainUI.getMainPane().getExecutePane();
        // The difficult part here is resetting the data segment.  Two approaches are:
        // 1. After each assembly, get a deep copy of the Globals.memory array
        //    containing data segment.  Then replace it upon reset.
        // 2. Simply re-assemble the program upon reset, and the assembler will
        //    build a new data segment.  Reset can only be done after a successful
        //    assembly, so there is "no" chance of assembler error.
        // I am choosing the second approach although it will slow down the reset
        // operation.  The first approach requires additional Memory class methods.
        try {
            Globals.program.assemble(RunAssembleAction.getProgramsToAssemble(),
                    RunAssembleAction.getExtendedAssemblerEnabled(),
                    RunAssembleAction.getWarningsAreErrors());
        } catch (AssemblyException pe) {
            // Should not be possible
            mainUI.getMessagesPane().postMessage(
                    //pe.errors().generateErrorReport());
                    "Unable to reset.  Please close file then re-open and re-assemble.\n");
            return;
        }

        RegisterFile.resetRegisters();
        FloatingPointRegisterFile.resetRegisters();
        ControlAndStatusRegisterFile.resetRegisters();
        InterruptController.reset();
        executePane.getRegistersWindow().updateRegisters();
        executePane.getRegistersWindow().clearHighlighting();
        executePane.getFloatingPointWindow().updateRegisters();
        executePane.getFloatingPointWindow().clearHighlighting();
        executePane.getControlAndStatusWindow().updateRegisters();
        executePane.getControlAndStatusWindow().clearHighlighting();
        executePane.getDataSegmentWindow().highlightCellForAddress(Memory.dataBaseAddress);
        executePane.getDataSegmentWindow().clearHighlighting();
        executePane.getTextSegmentWindow().resetModifiedSourceCode();
        executePane.getTextSegmentWindow().setCodeHighlighting(true);
        executePane.getTextSegmentWindow().highlightStepAtPC();
        mainUI.getRegistersPane().setSelectedComponent(executePane.getRegistersWindow());
        FileStatus.set(FileStatus.RUNNABLE);
        mainUI.setReset(true);
        mainUI.setStarted(false);

        // Aug. 24, 2005 Ken Vollmar
        SystemIO.resetFiles();  // Ensure that I/O "file descriptors" are initialized for a new program run

        mainUI.getMessagesPane().postMessage(
                "\n" + name + ": reset completed.\n\n");
    }
}