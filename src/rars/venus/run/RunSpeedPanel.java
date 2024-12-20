package rars.venus.run;

import rars.Globals;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Class for the Run speed slider control.  One is created and can be obtained using
 * getInstance().
 *
 * @author Pete Sanderson
 * @version August 2005
 */

public class RunSpeedPanel extends JPanel {
    /**
     * Constant that represents unlimited run speed.  Compare with return value of
     * getRunSpeed() to determine if set to unlimited.  At the unlimited setting, the GUI
     * will not attempt to update register and memory contents as each instruction
     * is executed.  This is the only possible value for command-line use of Mars.
     */
    public final static double UNLIMITED_SPEED = 40;

    private final static int SPEED_INDEX_MIN = 0;
    private final static int SPEED_INDEX_MAX = 40;
    private final static int SPEED_INDEX_INIT = 40;
    private static final int SPEED_INDEX_INTERACTION_LIMIT = 35;
    private double[] speedTable = {
            .05, .1, .2, .3, .4, .5, 1, 2, 3, 4, 5,      // 0-10
            6, 7, 8, 9, 10, 11, 12, 13, 14, 15,      // 11-20
            16, 17, 18, 19, 20, 21, 22, 23, 24, 25,      // 21-30
            26, 27, 28, 29, 30, UNLIMITED_SPEED, UNLIMITED_SPEED, // 31-37
            UNLIMITED_SPEED, UNLIMITED_SPEED, UNLIMITED_SPEED // 38-40
    };
    private JLabel sliderLabel = null;
    private JSlider runSpeedSlider = null;
    private static RunSpeedPanel runSpeedPanel = null;
    private volatile int runSpeedIndex = SPEED_INDEX_MAX;

    /**
     * Retrieve the run speed panel object
     *
     * @return the run speed panel
     */

    public static RunSpeedPanel getInstance() {
        if (runSpeedPanel == null) {
            runSpeedPanel = new RunSpeedPanel();
            Globals.runSpeedPanelExists = true; // DPS 24 July 2008 (needed for standalone tools)
        }
        return runSpeedPanel;
    }

    /*
     * private constructor (this is a singleton class)
     */
    private RunSpeedPanel() {
        super(new BorderLayout());
        runSpeedSlider = new JSlider(JSlider.HORIZONTAL, SPEED_INDEX_MIN, SPEED_INDEX_MAX, SPEED_INDEX_INIT);
        runSpeedSlider.setSize(new Dimension(100, (int) runSpeedSlider.getSize().getHeight()));
        runSpeedSlider.setMaximumSize(runSpeedSlider.getSize());
        runSpeedSlider.setMajorTickSpacing(5);
        runSpeedSlider.setPaintTicks(true); //Create the label table
        runSpeedSlider.addChangeListener(new RunSpeedListener());
        sliderLabel = new JLabel(setLabel(runSpeedIndex));
        sliderLabel.setHorizontalAlignment(JLabel.CENTER);
        sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(sliderLabel, BorderLayout.NORTH);
        this.add(runSpeedSlider, BorderLayout.CENTER);
        this.setToolTipText("Simulation speed for \"Go\".  At " +
                ((int) speedTable[SPEED_INDEX_INTERACTION_LIMIT]) + " inst/sec or less, tables updated " +
                "after each instruction.");
    }

    /**
     * returns current run speed setting, in instructions/second.  Unlimited speed
     * setting is equal to RunSpeedPanel.UNLIMITED_SPEED
     *
     * @return run speed setting in instructions/second.
     */

    public double getRunSpeed() {
        return speedTable[runSpeedIndex];
    }

    /*
     * set label wording depending on current speed setting
     */
    private String setLabel(int index) {
        String result = "Run speed ";
        if (index <= SPEED_INDEX_INTERACTION_LIMIT) {
            if (speedTable[index] < 1) {
                result += speedTable[index];
            } else {
                result += ((int) speedTable[index]);
            }
            result += " inst/sec";
        } else {
            result += ("at max (no interaction)");
        }
        return result;
    }

   	
   	/*
        *  Both revises label as user slides and updates current index when sliding stops.
   	 */

    private class RunSpeedListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) {
                runSpeedIndex = (int) source.getValue();
            } else {
                sliderLabel.setText(setLabel(source.getValue()));
            }
        }
    }
}