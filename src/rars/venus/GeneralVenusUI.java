package rars.venus;

import rars.Globals;
import rars.Settings;
import rars.riscv.InstructionSet;
import rars.riscv.dump.DumpFormatLoader;
import rars.simulator.Simulator;
import rars.venus.registers.*;
import rars.venus.run.*;
import rars.venus.settings.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.net.URL;

/*
Copyright (c) 2003-2006,  Siva Chowdeswar Nandipati

Developed by Siva Chowdeswar Nandipati (sivachow@ualberta.ca)

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

/**
 * Top level container for Venus GUI.
 *
 * @author Sanderson and Team JSpim
 **/

	  /* Heavily modified by Pete Sanderson, July 2004, to incorporate JSPIMMenu and JSPIMToolbar
       * not as subclasses of JMenuBar and JToolBar, but as instances of them.  They are both
		* here primarily so both can share the Action objects.
		*/

public class GeneralVenusUI extends JFrame {
    GeneralVenusUI mainUI;

    private GeneralMainPane mainPane;
    private GeneralRegistersPane registersPane;
    private RegistersWindow registersTab;
    private FloatingPointWindow fpTab;
    private ControlAndStatusWindow csrTab;
    private JSplitPane splitter, horizonSplitter;
    JPanel north;

    private int frameState; // see windowActivated() and windowDeactivated()

    // PLEASE PUT THESE TWO (& THEIR METHODS) SOMEWHERE THEY BELONG, NOT HERE
    private boolean reset = true; // registers/memory reset for execution
    private boolean started = false;  // started execution

    /**
     * Constructor for the Class. Sets up a window object for the UI
     *
     * @param s Name of the window to be created.
     **/

    public GeneralVenusUI(String s) {
        super(s);
        mainUI = this;
        double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        double mainWidthPct = (screenWidth < 1000.0) ? 0.67 : 0.73;
        double mainHeightPct = (screenWidth < 1000.0) ? 0.60 : 0.65;
        double registersWidthPct = (screenWidth < 1000.0) ? 0.18 : 0.22;
        double registersHeightPct = (screenWidth < 1000.0) ? 0.72 : 0.80;
        Dimension mainPanePreferredSize = new Dimension((int) (screenWidth * mainWidthPct), (int) (screenHeight * mainHeightPct));
        Dimension registersPanePreferredSize = new Dimension((int) (screenWidth * registersWidthPct), (int) (screenHeight * registersHeightPct));
        Globals.initialize(true);

        //  image courtesy of NASA/JPL.
        URL im = this.getClass().getResource(Globals.imagesPath + "RISC-V.png");
        if (im == null) {
            System.out.println("Internal Error: images folder or file not found");
            System.exit(0);
        }
        Image mars = Toolkit.getDefaultToolkit().getImage(im);
        this.setIconImage(mars);
        // Everything in frame will be arranged on JPanel "center", which is only frame component.
        // "center" has BorderLayout and 2 major components:
        //   -- panel (jp) on North with 2 components
        //      1. toolbar
        //      2. run speed slider.
        //   -- split pane (horizonSplitter) in center with 2 components side-by-side
        //      1. split pane (splitter) with 2 components stacked
        //         a. main pane, with 2 tabs (edit, execute)
        //         b. messages pane with 2 tabs (rars, run I/O)
        //      2. registers pane with 3 tabs (register file, coproc 0, coproc 1)
        // I should probably run this breakdown out to full detail.  The components are created
        // roughly in bottom-up order; some are created in component constructors and thus are
        // not visible here.

        registersTab = new RegistersWindow("Not GUI");
        fpTab = new FloatingPointWindow();
        csrTab = new ControlAndStatusWindow();
        registersPane = new GeneralRegistersPane(mainUI, registersTab, fpTab, csrTab);
        registersPane.setPreferredSize(registersPanePreferredSize);

        mainPane = new GeneralMainPane(mainUI, registersTab, fpTab, csrTab);
        mainPane.setPreferredSize(mainPanePreferredSize);
        try {
            mainPane.getExecutePane().getTextSegmentWindow().setMaximum(true);
        } catch (PropertyVetoException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        horizonSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainPane, registersPane);
        horizonSplitter.setOneTouchExpandable(true);
        horizonSplitter.resetToPreferredSizes();

        JPanel jp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel center = new JPanel(new BorderLayout());
        center.add(jp, BorderLayout.NORTH);
        center.add(horizonSplitter);
        this.add(center);

        // This is invoked when opening the app.  It will set the app to
        // appear at full screen size.
        this.addWindowListener(
                new WindowAdapter() {
                    public void windowOpened(WindowEvent e) {
                        mainUI.pack();
                    }
                });

        this.pack();
    }

   
    /**
     * To set whether the register values are reset.
     *
     * @param b Boolean true if the register values have been reset.
     **/

    public void setReset(boolean b) {
        reset = b;
    }

    /**
     * To set whether MIPS program execution has started.
     *
     * @param b true if the MIPS program execution has started.
     **/

    public void setStarted(boolean b) {
        started = b;
    }

    /**
     * To find out whether the register values are reset.
     *
     * @return Boolean true if the register values have been reset.
     **/

    public boolean getReset() {
        return reset;
    }

    /**
     * To find out whether MIPS program is currently executing.
     *
     * @return true if MIPS program is currently executing.
     **/
    public boolean getStarted() {
        return started;
    }

    /**
     * Get reference to messages pane associated with this GUI.
     *
     * @return MessagesPane object associated with the GUI.
     **/

    public GeneralMainPane getMainPane() {
        return mainPane;
    }

    /**
     * Get reference to registers pane associated with this GUI.
     *
     * @return RegistersPane object associated with the GUI.
     **/

    public GeneralRegistersPane getRegistersPane() {
        return registersPane;
    }


    private ImageIcon loadIcon(String name) {
        return new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource(Globals.imagesPath + name)));
    }

    private KeyStroke makeShortcut(int key) {
        return KeyStroke.getKeyStroke(key, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    }
}
