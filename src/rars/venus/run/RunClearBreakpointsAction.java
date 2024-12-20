package rars.venus.run;

import rars.Globals;
import rars.venus.GuiAction;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.ActionEvent;

/**
 * Action class for the Run menu item to clear execution breakpoints that have been set.
 * It is a listener and is notified whenever a breakpoint is added or removed, thus will
 * set its enabled status true or false depending on whether breakpoints remain after that action.
 */
public class RunClearBreakpointsAction extends GuiAction implements TableModelListener {

    /**
     * Create the object and register with text segment window as a listener on its table model.
     * The table model has not been created yet, so text segment window will hang onto this
     * registration info and transfer it to the table model upon creation (which happens with
     * each successful assembly).
     */
    public RunClearBreakpointsAction(String name, Icon icon, String descrip,
                                     Integer mnemonic, KeyStroke accel) {
        super(name, icon, descrip, mnemonic, accel);
        Globals.getGui().getMainPane().getExecutePane().getTextSegmentWindow().registerTableModelListener(this);
    }

    /**
     * When this option is selected, tell text segment window to clear breakpoints in its table model.
     */
    public void actionPerformed(ActionEvent e) {
        Globals.getGui().getMainPane().getExecutePane().getTextSegmentWindow().clearAllBreakpoints();
    }

    /**
     * Required TableModelListener method.  This is response upon editing of text segment table
     * model.  The only editable column is breakpoints so this method is called only when user
     * adds or removes a breakpoint.  Gets new breakpoint count and sets enabled status
     * accordingly.
     */
    public void tableChanged(TableModelEvent e) {
        setEnabled(
                Globals.getGui().getMainPane().getExecutePane().getTextSegmentWindow().getBreakpointCount() > 0);
    }

}