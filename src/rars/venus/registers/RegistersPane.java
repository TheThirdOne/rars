package rars.venus.registers;

import rars.venus.VenusUI;

import javax.swing.*;
import java.awt.Color;

/**
 * Contains tabbed areas in the UI to display register contents
 *
 * @author Sanderson
 * @version August 2005
 **/

public class RegistersPane extends JTabbedPane {
    private RegistersWindow regsTab;
    private FloatingPointWindow fpTab;
    private ControlAndStatusWindow csrTab;

    private VenusUI mainUI;

    /**
     * Constructor for the RegistersPane class.
     **/

    public RegistersPane(VenusUI appFrame, RegistersWindow regs, FloatingPointWindow cop1,
                         ControlAndStatusWindow cop0) {
        super();
        this.mainUI = appFrame;

        regsTab = regs;
        fpTab = cop1;
        csrTab = cop0;
        regsTab.setVisible(true);
        fpTab.setVisible(true);
        csrTab.setVisible(true);

        this.addTab("Registers", regsTab);
        this.addTab("Floating Point", fpTab);
        this.addTab("Control and Status", csrTab);

        this.setToolTipTextAt(0, "CPU registers");
        this.setToolTipTextAt(1, "Floating point unit registers");
        this.setToolTipTextAt(2, "Control and Status registers");
    }

    /**
     * Return component containing integer register set.
     *
     * @return integer register window
     */
    public RegistersWindow getRegistersWindow() {
        return regsTab;
    }

    /**
     * Return component containing floating point register set.
     *
     * @return floating point register window
     */
    public FloatingPointWindow getFloatingPointWindow() {
        return fpTab;
    }

    /**
     * Return component containing Control and Status register set.
     *
     * @return exceptions register window
     */
    public ControlAndStatusWindow getControlAndStatusWindow() {
        return csrTab;
    }
}
