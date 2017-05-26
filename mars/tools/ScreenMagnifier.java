package mars.tools;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

	/*
Copyright (c) 2003-2007,  Pete Sanderson and Kenneth Vollmar

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

/**
 * Handy little tool to magnify a selected section of the screen
 * by a given scale and display it.  The screen image snapshot
 * will be of the screen pixels beneath the tool's frame.  The
 * scale can be adjusted.  The image is displayed in the tool's
 * scrollable panel.  You can highlight items on the image using
 * the scribbler (hold down mouse button and move it on the
 * image).  The magnification scale adjustment is on the tool's
 * window, but other settings can be modified on a button-
 * triggered dialog.  It will capture the contents of the
 * underlying MARS graphical user interface, but NOT the
 * contents of other Mars Tools frames.
 *
 * @author Pete Sanderson
 * @version 1.0.
 *          9 July 2007.
 */
public class ScreenMagnifier implements MarsTool {

    public String getName() {
        return "Screen Magnifier";
    }

    public void action() {
        Magnifier mag = new Magnifier();
    }

    // Permits stand-alone execution.

    public static void main(String[] args) {
        new Thread(
                new Runnable() {
                    public void run() {
                        new ScreenMagnifier().action();
                    }
                }).start();
    }


}
/* Technique comes from the Javaworld article "Capture the Screen: Build a 
   screen-capture utility based on Java's Robot class".  By Jeff Friesen, 
	JavaWorld.com, 4/24/06.
   http://www.javaworld.com/javaworld/jw-04-2006/jw-0424-funandgames.html
*/

class Magnifier extends JFrame implements ComponentListener {
    static Robot robot;
    JButton close, capture, settings;
    JSpinner scaleAdjuster;
    JScrollPane view;
    Dimension frameSize;
    Dimension viewSize;
    MagnifierImage magnifierImage;
    ActionListener captureActionListener;
    CaptureModel captureResize, captureMove, captureRescale;
    CaptureModel captureDisplayCenter, captureDisplayUpperleft;
    CaptureModel dialogDisplayCenter;
    ScribblerSettings scribblerSettings;
    static final double SCALE_MINIMUM = 1.0;
    static final double SCALE_MAXIMUM = 4.0;
    static final double SCALE_INCREMENT = 0.5;
    static final double SCALE_DEFAULT = 2.0;
    double scale = SCALE_DEFAULT;
    CaptureDisplayAlignmentStrategy alignment;
    CaptureRectangleStrategy captureLocationSize = new CaptureMagnifierRectangle();
    JFrame frame;
    static final String CAPTURE_TOOLTIP_TEXT = "Capture, scale, and display pixels that lay beneath the Magnifier.";
    static final String SETTINGS_TOOLTIP_TEXT = "Show dialog for changing tool settings.";
    static final String SCALE_TOOLTIP_TEXT = "Magnification scale for captured image.";
    static final String CLOSE_TOOLTIP_TEXT = "Exit the Screen Magnifier.  Changed settings are NOT retained.";

    Magnifier() {
        super("Screen Magnifier 1.0");
        frame = this;
        createSettings();
        // If running withint MARS, set to its icon image; if not fuggetit.
        try {
            this.setIconImage(mars.Globals.getGui().getIconImage());
        } catch (Exception e) {
        }
        getContentPane().setLayout(new BorderLayout());
        // Will capture an image each time frame is moved/resized.
        addComponentListener(this);
        try {
            robot = new Robot();
        } catch (AWTException e) {
        } catch (SecurityException e) {
        }

        close = new JButton("Close");
        close.setToolTipText(CLOSE_TOOLTIP_TEXT);
        close.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setVisible(false);
                    }
                });
        settings = new JButton("Settings...");
        settings.setToolTipText(SETTINGS_TOOLTIP_TEXT);
        settings.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        new SettingsDialog(frame);
                    }
                });
        magnifierImage = new MagnifierImage(this);
        view = new JScrollPane(magnifierImage);
        viewSize = new Dimension(200, 150);
        view.setSize(viewSize);

        capture = new JButton("Capture");
        capture.setToolTipText(CAPTURE_TOOLTIP_TEXT);
        captureActionListener =
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        magnifierImage.setImage(MagnifierImage.getScaledImage(captureScreenSection(captureLocationSize.getCaptureRectangle(getFrameRectangle())), scale));
                        alignment.setScrollBarValue(view.getHorizontalScrollBar());
                        alignment.setScrollBarValue(view.getVerticalScrollBar());
                    }
                };
        JLabel scaleLabel = new JLabel("Scale: ");
        SpinnerModel scaleModel = new SpinnerNumberModel(SCALE_DEFAULT, SCALE_MINIMUM, SCALE_MAXIMUM, SCALE_INCREMENT);
        scaleAdjuster = new JSpinner(scaleModel);
        scaleAdjuster.setToolTipText(SCALE_TOOLTIP_TEXT);
        JSpinner.NumberEditor scaleEditor = new JSpinner.NumberEditor(scaleAdjuster, "0.0");
        scaleEditor.getTextField().setEditable(false);
        scaleAdjuster.setEditor(scaleEditor);
        scaleAdjuster.addChangeListener(
                new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        scale = ((Double) scaleAdjuster.getValue()).doubleValue();
                        if (captureRescale.isEnabled()) {
                            captureActionListener.actionPerformed(
                                    new ActionEvent(frame, 0, "capture"));
                        }
                    }
                });
        JPanel scalePanel = new JPanel();
        scalePanel.add(scaleLabel);
        scalePanel.add(scaleAdjuster);
        capture.addActionListener(captureActionListener);
        Box buttonRow = Box.createHorizontalBox();
        buttonRow.add(Box.createHorizontalStrut(4));
        buttonRow.add(capture);
        buttonRow.add(Box.createHorizontalGlue());
        buttonRow.add(settings);
        buttonRow.add(scalePanel);
        buttonRow.add(Box.createHorizontalGlue());
        buttonRow.add(getHelpButton());
        buttonRow.add(Box.createHorizontalGlue());
        buttonRow.add(close);
        buttonRow.add(Box.createHorizontalStrut(4));
        getContentPane().add(view, BorderLayout.CENTER);
        getContentPane().add(buttonRow, BorderLayout.SOUTH);
        pack();
        setSize(500, 400);
        setLocationRelativeTo(null); // center on screen
        setVisible(true);
        // For some strange reason, the image has to be captured
        // and displayed an extra time for the display justification
        // to be recognized for the scrollbars.  The first capture
        // will justify left-center no matter what (scrollbar
        // positions 0).
        captureActionListener.actionPerformed(
                new ActionEvent(frame, 0, "capture"));
        captureActionListener.actionPerformed(
                new ActionEvent(frame, 0, "capture"));
    }

    /*
     *  Create the default Screen Magnifier tool settings.  These can
     *  all be changed through the Settings dialog but are not persistent
     *  across activations of the tool.
     */
    private void createSettings() {
        // Which events will cause automatic re-capture?  Pick any
        // or all of these three: resize the frame, move the frame,
        // change the magnification scale (using spinner).
        captureResize = new CaptureModel(false);
        captureMove = new CaptureModel(false);
        captureRescale = new CaptureModel(true);
        // When capture is taken, how shall it be displayed in the view
        // panel?  Scrollbars will be present since the displayed image
        // has to be larger than the viewing panel.  Display it either
        // with scrollbars centered, or scrollbars at initial position
        // (upper-left corner of image at upper-left corner of viewer).
        alignment = new CaptureDisplayCentered();// CaptureDisplayUpperleft();
        // Once the alignment is set, these will correctly self-set.
        captureDisplayCenter = new CaptureModel(alignment instanceof CaptureDisplayCentered);
        captureDisplayUpperleft = new CaptureModel(alignment instanceof CaptureDisplayUpperleft);
        // Scribbler has two settings: line width in pixels and line color.
        scribblerSettings = new ScribblerSettings(2, Color.RED);
        // Whether or not to center the Settings dialog over the Magnifier frame.
        dialogDisplayCenter = new CaptureModel(true);
    }

    // A simple explanation of what the tool does.
    private JButton getHelpButton() {
        final String helpContent =
                "Use this utility tool to display a magnified image of a\n" +
                        "screen section and highlight things on the image.  This\n" +
                        "will be of interest mainly to instructors.\n" +
                        "\n" +
                        "To capture an image, size and position the Screen Magnifier\n" +
                        "over the screen segment to be magnified and click \"Capture\".\n" +
                        "The pixels beneath the magnifier will be captured, scaled,\n" +
                        "and displayed in a scrollable window.\n" +
                        "\n" +
                        "To highlight things in the image, just drag the mouse over\n" +
                        "the image to make a scribble line.  This line is ephemeral\n" +
                        "(is not repainted if covered then uncovered).\n" +
                        "\n" +
                        "The magnification scale can be adjusted using the spinner.\n" +
                        "Other settings can be adjusted through the Settings dialog.\n" +
                        "Settings include: justification of displayed image, automatic\n" +
                        "capture upon certain tool events, and the thickness and color\n" +
                        "of the scribble line.\n" +
                        "\n" +
                        "LIMITS: The image is static; it is not updated when the\n" +
                        "underlying pixels change.  Scale changes do not take effect\n" +
                        "until the next capture (but you can set auto-capture).  The\n" +
                        "Magnifier does not capture frame contents of other tools.\n" +
                        "Setting changes are not saved when the tool is closed.\n" +
                        "\n" +
                        "Contact Pete Sanderson at psanderson@otterbein.edu with\n" +
                        "questions or comments.\n";
        JButton help = new JButton("Help");
        help.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JOptionPane.showMessageDialog(frame, helpContent);
                    }
                });
        return help;
    }


    /**
     * Capture the pixels of the specified screen rectangle into an ImageBuffer.
     * The trick is the ScreenMagnifier's frame has to be made invisible first
     * so it does not show up in the image.
     *
     * @param section A rectangle specifying the range of pixels to capture.
     * @return A BufferedImage containing the captured pixel range.
     */
    BufferedImage captureScreenSection(Rectangle section) {
        // Hide Frame so that it does not appear in the screen capture.
        setVisible(false);
        // For some reason, the graphic extent vacated by the above call
        // is not redrawn before the screen capture unless I explicitly
        // force it to be redrawn by telling the Mars GUI to update.
        // If this doesn't work, e.g. getGui() returns null, then there
        // are no alternatives so just let what would happen, happen.
        try {
            mars.Globals.getGui().update(mars.Globals.getGui().getGraphics());
        } catch (Exception e) {
        }
        // Perform the screen capture.
        BufferedImage imageOfSection;
        imageOfSection = robot.createScreenCapture(section);
        setVisible(true);
        return imageOfSection;
    }

    /**
     * Place the current frame size and location into a Rectangle object.
     *
     * @return A Rectangle containing the ScreenMagnifier's location, plus
     * width and height in pixels.
     */
    Rectangle getFrameRectangle() {
        return new Rectangle(getLocation().x, getLocation().y,
                getSize().width, getSize().height);
    }

    /**
     * Place the current screen size and location into a Rectangle object.
     *
     * @return A Rectangle containing the current screen location (0,0),
     * plus width and height in pixels.
     */
    Rectangle getScreenRectangle() {
        return new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    }


    //////////////////////////////////////////////////////////////////////
    //////
    ////// These four methods are required for ComponentListener interface.
    ////// Note: due to single inheritance, I can't extend ComponentAdaptor.
    //////
    //////////////////////////////////////////////////////////////////////

    /**
     * Respond to frame movement event by showing new image based on
     * the screen section of interest for this new position.  This event
     * may occur once at the end of the move or it may occur
     * at multiple points during the move.  The latter causes a flashing
     * image, but I have not been able to determine how to limit it
     * to one event occurance.
     */

    // Regarding the above comment, I want to have only one capture
    // occur, at the end of the move.  But the number and timing of
    // the generated events seems to vary depending on what machine
    // I am running the program on!  I have seen a technique in which
    // componentMoved would start up a timer to go off every 100 ms or
    // so to determine if the frame location had changed since the
    // previous timer check.  If so, the frame is moving so do nothing.
    // If not, the move is assumed to be over and the image is captured
    // and the timer stopped.  The latter would also handle the case
    // where componentMoved is triggered only at the end of the move.
    // componentMoved would also have to determine whether or not
    // such a timer was already running (e.g. is this the
    // first componentMoved event?) in case it is called multiple
    // times throughout the move, not just at the end.
    public void componentMoved(ComponentEvent e) {
        if (captureMove.isEnabled()) {
            captureActionListener.actionPerformed(
                    new ActionEvent(e.getComponent(), e.getID(), "capture"));
        }
    }

    /**
     * Respond to frame resize event by showing new image based on
     * the screen section of interest for this new size.   This event
     * may occur once at the end of the resize or it may occur
     * at multiple points during the resize.  The latter causes a flashing
     * image, but I have not been able to determine how to limit it
     * to one event occurance.
     */
    // See comments above for possible technique for assuring only one
    // capture at the end of the resize.
    public void componentResized(ComponentEvent e) {
        if (captureResize.isEnabled()) {
            captureActionListener.actionPerformed(
                    new ActionEvent(e.getComponent(), e.getID(), "capture"));
        }
    }

    /**
     * Invoked when setVisible(true) is called, do nothing.
     */
    public void componentShown(ComponentEvent e) {
    }

    /**
     * Invoked when setVisible(false) is called, do nothing.
     */
    public void componentHidden(ComponentEvent e) {
    }

}


/**
 * Specialized class for the magnifier's settings dialog.
 */

class SettingsDialog extends JDialog {
    JButton applyButton, cancelButton;
    JCheckBox captureResizeCheckBox, captureMoveCheckBox, captureRescaleCheckBox;
    JRadioButton captureDisplayCenteredButton, captureDisplayUpperleftButton;
    Integer[] scribblerLineWidthSettings = {new Integer(1), new Integer(2),
            new Integer(3), new Integer(4),
            new Integer(5), new Integer(6),
            new Integer(7), new Integer(8)};
    JComboBox lineWidthSetting;
    JButton lineColorSetting;
    JCheckBox dialogCentered; // Whether or not dialog appears centered over the magnfier frame.
    JDialog dialog;
    // temporary storage until committed with "Apply".  Needed because it is returned
    // by same call that shows the color selection dialog, so cannot be retrieved
    // later from the model (as you can with buttons, checkboxes, etc).
    Color scribblerLineColorSetting;
    // Text for tool tips.
    static final String SETTINGS_APPLY_TOOLTIP_TEXT = "Apply current settings and close the dialog.";
    static final String SETTINGS_CANCEL_TOOLTIP_TEXT = "Close the dialog without applying any setting changes.";
    static final String SETTINGS_SCRIBBLER_WIDTH_TOOLTIP_TEXT = "Scribbler line thickness in pixels.";
    static final String SETTINGS_SCRIBBLER_COLOR_TOOLTIP_TEXT = "Click here to change Scribbler line color.";
    static final String SETTINGS_DIALOG_CENTERED_TOOLTIP_TEXT = "Whether to center this dialog over the Magnifier.";

    SettingsDialog(JFrame frame) {
        super(frame, "Magnifier Tool Settings");
        dialog = this;
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        JPanel settingsPanel = new JPanel();
        JPanel selectionsPanel = new JPanel(new GridLayout(2, 1));
        selectionsPanel.add(getCaptureDisplayPanel());
        JPanel secondRow = new JPanel(new GridLayout(1, 2));
        secondRow.add(getAutomaticCaptureSettingsPanel());
        secondRow.add(getScribblerPanel(this));
        selectionsPanel.add(secondRow);
        contentPane.add(selectionsPanel);
        contentPane.add(getButtonRowPanel(), BorderLayout.SOUTH);
        pack();
        if (dialogCentered.isSelected()) {
            setLocationRelativeTo(frame);
        }
        setVisible(true);
    }

    // This panel contains the control buttons for the Settings Dialog.
    private JPanel getButtonRowPanel() {
        JPanel buttonRow = new JPanel();
        applyButton = new JButton("Apply and Close");
        applyButton.setToolTipText(SETTINGS_APPLY_TOOLTIP_TEXT);
        // Action to perform when APply button pressed: commit GUI settings to the models
        applyButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        // commit settings
                        ((Magnifier) getOwner()).captureResize.setEnabled(captureResizeCheckBox.isSelected());
                        ((Magnifier) getOwner()).captureMove.setEnabled(captureMoveCheckBox.isSelected());
                        ((Magnifier) getOwner()).captureRescale.setEnabled(captureRescaleCheckBox.isSelected());
                        ((Magnifier) getOwner()).captureDisplayCenter.setEnabled(captureDisplayCenteredButton.isSelected());
                        ((Magnifier) getOwner()).captureDisplayUpperleft.setEnabled(captureDisplayUpperleftButton.isSelected());
                        ((Magnifier) getOwner()).dialogDisplayCenter.setEnabled(dialogCentered.isSelected());
                        if (captureDisplayCenteredButton.isSelected()) {
                            ((Magnifier) getOwner()).alignment = new CaptureDisplayCentered();
                        } else if (captureDisplayUpperleftButton.isSelected()) {
                            ((Magnifier) getOwner()).alignment = new CaptureDisplayUpperleft();
                        }
                        ((Magnifier) getOwner()).scribblerSettings.setLineWidth(scribblerLineWidthSettings[lineWidthSetting.getSelectedIndex()].intValue());
                        ((Magnifier) getOwner()).scribblerSettings.setLineColor(lineColorSetting.getBackground());
                        dialog.dispose();
                    }
                });
        cancelButton = new JButton("Cancel");
        cancelButton.setToolTipText(SETTINGS_CANCEL_TOOLTIP_TEXT);
        cancelButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dialog.dispose();
                    }
                });
        // By default, display dialog centered over the Magnifier's frame.  This
        // can be changed however to display at upper left corner of screen.  Why
        // would you want to change this?  So you can use the settings dialog to
        // to change scribbler color and/or width without wiping out current
        // scribbler marks.  If the dialog is centered, its mere display may
        // wipe out existing scribbler marks (because they are not repainted
        // when the underlying image is repainted).
        dialogCentered = new JCheckBox("Dialog centered", ((Magnifier) getOwner()).dialogDisplayCenter.isEnabled());
        dialogCentered.setToolTipText(SETTINGS_DIALOG_CENTERED_TOOLTIP_TEXT);
        buttonRow.add(applyButton);
        buttonRow.add(cancelButton);
        buttonRow.add(dialogCentered);
        return buttonRow;
    }

    // Panel that contains settings for automatically performing an image
    // capture.  These are a convenience, as the image can always be
    // manually captured by clicking the "Capture" button.
    private JPanel getAutomaticCaptureSettingsPanel() {
        JPanel automaticCaptureSettings = new JPanel();
        automaticCaptureSettings.setBorder(new TitledBorder("Automatic Capture"));
        Box automaticCaptureSettingsBox = Box.createHorizontalBox();
        automaticCaptureSettings.add(automaticCaptureSettingsBox);
        captureResizeCheckBox = new JCheckBox("Capture upon resize", ((Magnifier) getOwner()).captureResize.isEnabled());
        captureMoveCheckBox = new JCheckBox("Capture upon move", ((Magnifier) getOwner()).captureMove.isEnabled());
        captureRescaleCheckBox = new JCheckBox("Capture upon rescale", ((Magnifier) getOwner()).captureRescale.isEnabled());
        JPanel checkboxColumn = new JPanel(new GridLayout(3, 1));
        checkboxColumn.add(captureResizeCheckBox);
        checkboxColumn.add(captureMoveCheckBox);
        checkboxColumn.add(captureRescaleCheckBox);
        automaticCaptureSettingsBox.add(checkboxColumn);
        return automaticCaptureSettings;
    }

    // Panel that contains settings for extent and display of image upon
    // capture.  In version 1.0, the extent is fixed; it is same as that
    // of the tool's frame itself.  The term "extent" refers to the location
    // and dimension of the rectangle.
    private JPanel getCaptureDisplayPanel() {
        JPanel captureDisplaySetting = new JPanel();
        captureDisplaySetting.setBorder(new TitledBorder("Capture and Display"));
        Box captureDisplaySettingsBox = Box.createHorizontalBox();
        captureDisplaySetting.add(captureDisplaySettingsBox);
        captureDisplayCenteredButton = new JRadioButton("Capture area behind magnifier and display centered",
                ((Magnifier) getOwner()).captureDisplayCenter.isEnabled());
        captureDisplayUpperleftButton = new JRadioButton("Capture area behind magnifier and display upper-left",
                ((Magnifier) getOwner()).captureDisplayUpperleft.isEnabled());
        ButtonGroup displayButtonGroup = new ButtonGroup();
        displayButtonGroup.add(captureDisplayCenteredButton);
        displayButtonGroup.add(captureDisplayUpperleftButton);
        JPanel radioColumn = new JPanel(new GridLayout(2, 1));
        radioColumn.add(captureDisplayCenteredButton);
        radioColumn.add(captureDisplayUpperleftButton);
        JPanel radioLabelColumn = new JPanel(new GridLayout(1, 1));
        captureDisplaySettingsBox.add(radioColumn);
        return captureDisplaySetting;
    }

    // Panel that contains settings for the Scribbler part of the tool.
    // The only settings here are choice of line width (thickness) in pixels
    // and line color.
    private JPanel getScribblerPanel(final JDialog dialog) {
        JPanel scribblerSettings = new JPanel();
        scribblerSettings.setBorder(new TitledBorder("Scribbler"));
        Box scribblerSettingsBox = Box.createHorizontalBox();
        scribblerSettings.add(scribblerSettingsBox);
        lineWidthSetting = new JComboBox(scribblerLineWidthSettings);
        lineWidthSetting.setToolTipText(SETTINGS_SCRIBBLER_WIDTH_TOOLTIP_TEXT);
        lineWidthSetting.setSelectedIndex(((Magnifier) getOwner()).scribblerSettings.getLineWidth() - 1);
        lineColorSetting = new JButton("   ");
        lineColorSetting.setToolTipText(SETTINGS_SCRIBBLER_COLOR_TOOLTIP_TEXT);
        lineColorSetting.setBackground(((Magnifier) getOwner()).scribblerSettings.getLineColor());
        lineColorSetting.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Color newColor = JColorChooser.showDialog(dialog, "Scribbler line color", lineColorSetting.getBackground());
                        lineColorSetting.setBackground(newColor);
                    }
                });
        scribblerLineColorSetting = lineColorSetting.getBackground();
        JPanel settingsColumn = new JPanel(new GridLayout(2, 1, 5, 5));
        settingsColumn.add(lineWidthSetting);
        settingsColumn.add(lineColorSetting);
        JPanel labelColumn = new JPanel(new GridLayout(2, 1, 5, 5));
        labelColumn.add(new JLabel("Line width ", SwingConstants.LEFT));
        labelColumn.add(new JLabel("Line color ", SwingConstants.LEFT));
        scribblerSettingsBox.add(labelColumn);
        scribblerSettingsBox.add(settingsColumn);
        return scribblerSettings;
    }

}

/**
 * Represents an automatic capture "event" which is enabled
 * if the capture operation is to be performed automatically
 * upon occurance.
 */
class CaptureModel {
    private boolean enabled;

    /**
     * Create a new CaptureModel.
     *
     * @param set True if capture is to be automatically performed when
     *            associated event occurs, false otherwise.
     */
    public CaptureModel(boolean set) {
        enabled = set;
    }

    /**
     * Determine whether or not capture will automatically occur.
     *
     * @return True if automatic capture will occur, false otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Specify whether or not capture will automatically occur.
     *
     * @param set True if capture is to be automatically performed when
     *            associated event occurs, false otherwise.
     */
    public void setEnabled(boolean set) {
        enabled = set;
    }

}

/**
 * This class defines a specialized panel for displaying a captured image.
 */

class MagnifierImage extends JPanel {

    // Enclosing JFrame for this panel -- the Screen Magnifier itself.
    private Magnifier frame;
    // Rectangle representing screen pixels to be magnified.
    private Rectangle screenRectangle;
    // Robot used to perform the screen capture.
    private static Robot robot;
    // Displayed image's Image object, which is actually a BufferedImage.
    private Image image;
    // Scribbler for highlighting image using mouse.
    private Scribbler scribbler;


    /**
     * Construct an MagnifierImage component.
     */

    public MagnifierImage(Magnifier frame) {
        this.frame = frame;
        this.scribbler = new Scribbler(frame.scribblerSettings);

        addMouseListener(
                new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        scribbler.moveto(e.getX(), e.getY()); // Move to click position
                    }
                });

        // Install a mouse motion listener to draw the scribble.
        addMouseMotionListener(
                new MouseMotionAdapter() {
                    public void mouseDragged(MouseEvent e) {
                        scribbler.lineto(e.getX(), e.getY(), (Graphics2D) getGraphics());
                    }
                });
    }


    /**
     * Return the current image.
     *
     * @return Image reference to current image
     */

    public Image getImage() {
        return image;
    }

    /**
     * Repaint the MagnifierImage with the current image's pixels.
     *
     * @param g graphics context
     */

    public void paintComponent(Graphics g) {
        // Repaint the component's background.
        super.paintComponent(g);
        // If an image has been defined, draw that image using the Component
        // layer of this MagnifierImage object as the ImageObserver.
        if (image != null)
            g.drawImage(image, 0, 0, this);
    }

    /**
     * Establish a new image and update the display.
     *
     * @param image new image's Image reference
     */

    public void setImage(Image image) {
        // Save the image for later repaint.
        this.image = image;
        // Set this panel's preferred size to the image's size, to influence the
        // display of scrollbars.
        setPreferredSize(new Dimension(image.getWidth(this),
                image.getHeight(this)));
        // Present scrollbars as necessary.
        revalidate();
        // Update the image displayed on the panel.
        repaint();
    }

    /**
     * Get a scaled version of the image.  Ignores scaling values between .99 and 1.01.
     *
     * @param image          the original image
     * @param scale          the magnification scale as a double
     * @param scaleAlgorithm Scaling algorithm to use: Image.SCALE_DEFAULT,
     *                       Image.SCALE_FAST, Image.SCALE_SMOOTH.
     */
    static Image getScaledImage(Image image, double scale, int scaleAlgorithm) {
        // Don't bother if it is close to 1.  I anticipate this will be used mainly to
        // enlarge the image, so short circuit evalution will apply most of the time.
        return (scale < 1.01 && scale > 0.99)
                ? image
                : image.getScaledInstance((int) (image.getWidth(null) * scale),
                (int) (image.getHeight(null) * scale),
                scaleAlgorithm);
    }

    /**
     * Get a scaled version of the image using default scaling algorithm.
     * Ignores scaling values between .99 and 1.01.
     *
     * @param image the original image
     * @param scale the magnification scale as a double
     */
    static Image getScaledImage(Image image, double scale) {
        return getScaledImage(image, scale, Image.SCALE_DEFAULT);
    }

    /*
      *  Little class to allow user to scribble over the image using the
      *  mouse.  The scribble is ephemeral; it is drawn but specifications
      *  are not saved.
      */
    private class Scribbler {
        private ScribblerSettings scribblerSettings;
        private BasicStroke drawingStroke;
        // coordinates of previous mouse position
        protected int last_x, last_y;

        Scribbler(ScribblerSettings scribblerSettings) {
            this.scribblerSettings = scribblerSettings;
            drawingStroke = new BasicStroke(scribblerSettings.getLineWidth());
        }

        /**
         * Get the scribbler's drawing color.
         */
        public Color getColor() {
            return scribblerSettings.getLineColor();//color;
        }

        /**
         * Get the scribbler's line (stroke) width.
         */
        public int getLineWidth() {
            this.drawingStroke = new BasicStroke(scribblerSettings.getLineWidth());
            return scribblerSettings.getLineWidth();//width;
        }

        /**
         * Set the scribbler's drawing color.
         */
        public void setColor(Color newColor) {
            scribblerSettings.setLineColor(newColor);// this.color = newColor;
        }

        /**
         * Set the scribbler's line (stroke) width.
         */
        public void setLineWidth(int newWidth) {
            scribblerSettings.setLineWidth(newWidth);// this.width = newWidth;
            this.drawingStroke = new BasicStroke(newWidth);
        }

        /**
         * Get the scribbler's drawing (stroke) object.
         */
        private BasicStroke getStroke() {
            return drawingStroke;
        }

        /**
         * Set the scribbler's drawing (stroke) object.
         */
        private void setStroke(BasicStroke newStroke) {
            this.drawingStroke = newStroke;
        }

        /**
         * Remember the specified point
         */
        public void moveto(int x, int y) {
            last_x = x;
            last_y = y;
        }

        /**
         * Draw from the last point to this point, then remember new point
         */
        public void lineto(int x, int y, Graphics2D g2d) {
            // System.out.println(drawingStroke.getLineWidth());
            g2d.setStroke(new BasicStroke(scribblerSettings.getLineWidth()));
            g2d.setColor(scribblerSettings.getLineColor()); // Tell it what color to use
            g2d.draw(new Line2D.Float(last_x, last_y, x, y));
            moveto(x, y); // Save the current point
        }
    }

}


/**
 * Class to represent current settings for the scribbler tool.
 */
class ScribblerSettings {
    private int width;
    private Color color;

    /**
     * Build a new ScribblerSettings object.
     */
    public ScribblerSettings(int width, Color color) {
        this.width = width;
        this.color = color;
    }

    /**
     * Fetch the current line width for the scribbler tool.
     */
    public int getLineWidth() {
        return width;
    }

    /**
     * Fetch the current line color for the scribbler tool.
     */
    public Color getLineColor() {
        return color;
    }

    /**
     * Set the current line width for the scribbler tool.
     */
    public void setLineWidth(int newWidth) {
        width = newWidth;
    }

    /**
     * Set the current line color for the scribbler tool.
     */
    public void setLineColor(Color newColor) {
        color = newColor;
    }
}


/**
 * Interface to specify strategy for determining the size and location
 * of the screen rectangle to capture.
 */
interface CaptureRectangleStrategy {
    public Rectangle getCaptureRectangle(Rectangle magnifierRectangle);
}

/**
 * Upon screen capture, capture the same rectangle as the magnifier
 * itself.  Pixels that lie directly beneath it.
 */
class CaptureMagnifierRectangle implements CaptureRectangleStrategy {
    public Rectangle getCaptureRectangle(Rectangle magnifierRectangle) {
        return magnifierRectangle;
    }
}

/**
 * Upon screen capture, capture a rectangle whose size and location is
 * determined by the current magnification scale.  For instance, if the
 * scale is 2.0, the capture rectangle will have half the width and
 * height of the magnifier and its location will be offset so it is
 * centered within the magnifier rectangle -- except when the magnifier
 * is near the edge of the screen in which case the location is adjusted
 * such that the boundary pixels will be captured.
 */
class CaptureScaledRectangle implements CaptureRectangleStrategy {
    public Rectangle getCaptureRectangle(Rectangle magnifierRectangle) {
        throw new UnsupportedOperationException(); // This is not implementated yet!
        //return new Rectangle();
    }
}


/**
 * Interface to specify strategy for determining initial scrollbar settings
 * when displaying captured and scaled image.
 */
interface CaptureDisplayAlignmentStrategy {
    public void setScrollBarValue(JScrollBar scrollBar);
}

/**
 * Captured and scaled image should be displayed centerd in the panel.
 */
class CaptureDisplayCentered implements CaptureDisplayAlignmentStrategy {
    /**
     * Set the scrollbar value to inticate the captured and scaled image
     * is displayed centered in the panel.
     *
     * @param scrollBar The scrollbar to be adjusted.
     */
    public void setScrollBarValue(JScrollBar scrollBar) {
        scrollBar.setValue((scrollBar.getModel().getMaximum()
                        - scrollBar.getModel().getMinimum()
                        - scrollBar.getModel().getExtent()
                ) / 2
        );
    }
}

/**
 * Captured and scaled image should be displayed so that its upper left
 * corner is initially displayed in the upper left corner of the panel.
 */
class CaptureDisplayUpperleft implements CaptureDisplayAlignmentStrategy {
    /**
     * Set the scrollbar value to inticate the captured and scaled image
     * is displayed with the upper left corner initially visible in the panel.
     *
     * @param scrollBar The scrollbar to be adjusted.
     */
    public void setScrollBarValue(JScrollBar scrollBar) {
        scrollBar.setValue(0);
    }
}
   
