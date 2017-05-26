package mars.tools;

import mars.Globals;
import mars.mips.hardware.AccessNotice;
import mars.mips.hardware.Coprocessor1;
import mars.mips.hardware.Register;
import mars.util.Binary;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Observable;

	/*
Copyright (c) 2003-2006,  Pete Sanderson and Kenneth Vollmar

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
 * Tool to help students learn about IEEE 754 representation of 32 bit
 * floating point values.  This representation is used by MIPS "float"
 * directive and instructions and also the Java (and most other languages)
 * "float" data type.  As written, it can ALMOST be adapted to 64 bit by
 * changing a few constants.
 */
public class FloatRepresentation extends AbstractMarsToolAndApplication {
    private static String version = "Version 1.1";
    private static String heading = "32-bit IEEE 754 Floating Point Representation";
    private static final String title = "Floating Point Representation, ";

    private static final String defaultHex = "00000000";
    private static final String defaultDecimal = "0.0";
    private static final String defaultBinarySign = "0";
    private static final String defaultBinaryExponent = "00000000";
    private static final String defaultBinaryFraction = "00000000000000000000000";
    private static final int maxLengthHex = 8;
    private static final int maxLengthBinarySign = 1;
    private static final int maxLengthBinaryExponent = 8;
    private static final int maxLengthBinaryFraction = 23;
    private static final int maxLengthBinaryTotal = maxLengthBinarySign + maxLengthBinaryExponent + maxLengthBinaryFraction;
    private static final int maxLengthDecimal = 20;
    private static final String denormalizedLabel = "                 significand (denormalized - no 'hidden bit')";
    private static final String normalizedLabel = "                 significand ('hidden bit' underlined)       ";
    private static final Font instructionsFont = new Font("Arial", Font.PLAIN, 14);
    private static final Font hexDisplayFont = new Font("Courier", Font.PLAIN, 32);
    private static final Font binaryDisplayFont = new Font("Courier", Font.PLAIN, 18);
    private static final Font decimalDisplayFont = new Font("Courier", Font.PLAIN, 18);
    private static final Color hexDisplayColor = Color.red;
    private static final Color binaryDisplayColor = Color.black;
    private static final Color decimalDisplayColor = Color.blue;
    private static final String expansionFontTag = "<font size=\"+1\" face=\"Courier\" color=\"#000000\">";
    private static final String instructionFontTag = "<font size=\"+0\" face=\"Verdana, Arial, Helvetica\" color=\"#000000\">";
    private static final int exponentBias = 127;  // 32 bit floating point exponent bias

    private Register attachedRegister = null;
    private Register[] fpRegisters;
    private FloatRepresentation thisFloatTool;
    // Panels to hold binary displays and decorations (labels, arrows)
    private JPanel binarySignDecoratedDisplay,
            binaryExponentDecoratedDisplay, binaryFractionDecoratedDisplay;
    // Editable fields for the hex, binary and decimal representations.
    private JTextField hexDisplay, decimalDisplay,
            binarySignDisplay, binaryExponentDisplay, binaryFractionDisplay;
    // Non-editable fields to display formula translating binary to decimal.
    private JLabel expansionDisplay;
    private JLabel significandLabel = new JLabel(denormalizedLabel, JLabel.CENTER);
    private BinaryToDecimalFormulaGraphic binaryToDecimalFormulaGraphic;
    // Non-editable field to display instructions
    private InstructionsPane instructions;
    private String defaultInstructions = "Modify any value then press the Enter key to update all values.";

    /**
     * Simple constructor, likely used to run a stand-alone memory reference visualizer.
     *
     * @param title   String containing title for title bar
     * @param heading String containing text for heading shown in upper part of window.
     */
    public FloatRepresentation(String title, String heading) {
        super(title, heading);
        thisFloatTool = this;
    }

    /**
     * Simple constructor, likely used by the MARS Tools menu mechanism
     */
    public FloatRepresentation() {
        this(title + version, heading);
    }

    /**
     * Main provided for pure stand-alone use.  Recommended stand-alone use is to write a
     * driver program that instantiates a FloatRepresentation object then invokes its go() method.
     * "stand-alone" means it is not invoked from the MARS Tools menu.  "Pure" means there
     * is no driver program to invoke the application.
     */
    public static void main(String[] args) {
        new FloatRepresentation(title + version, heading).go();
    }

    /**
     * Fetch tool name (for display in MARS Tools menu)
     *
     * @return String containing tool name
     */
    public String getName() {
        return "Floating Point Representation";
    }

    /**
     * Override the inherited method, which registers us as an Observer over the static data segment
     * (starting address 0x10010000) only.  This version will register us as observer over the selected
     * floating point register, if any. If no register is selected, it will not do anything.
     * If you use the inherited GUI buttons, this method is invoked when you click "Connect" button
     * on MarsTool or the "Assemble and Run" button on a Mars-based app.
     */
    protected void addAsObserver() {
        addAsObserver(attachedRegister);
    }

    /**
     * Delete this app/tool as an Observer of the attached register.  This overrides
     * the inherited version which deletes only as an Observer of memory.
     * This method is called when the default "Disconnect" button on a MarsTool is selected or
     * when the MIPS program execution triggered by the default "Assemble and run" on a stand-alone
     * Mars app terminates (e.g. when the button is re-enabled).
     */
    protected void deleteAsObserver() {
        deleteAsObserver(attachedRegister);
    }

    /**
     * Method that constructs the main display area.  This will be vertically sandwiched between
     * the standard heading area at the top and the control area at the bottom.
     *
     * @return the GUI component containing the application/tool-specific part of the user interface
     */
    protected JComponent buildMainDisplayArea() {
        return buildDisplayArea();
    }

    /**
     * Override inherited update() to update display when "attached" register is modified
     * either by MIPS program or by user editing it on the MARS user interface.
     * The latter is the reason for overriding the inherited update() method.
     * The inherited method will filter out notices triggered by the MARS GUI or the user.
     *
     * @param register     the attached register
     * @param accessNotice information provided by register in RegisterAccessNotice object
     */
    public void update(Observable register, Object accessNotice) {
        if (((AccessNotice) accessNotice).getAccessType() == AccessNotice.WRITE) {
            updateDisplays(new FlavorsOfFloat().buildOneFromInt(attachedRegister.getValue()));
        }
    }

    /**
     * Method to reset display values to 0 when the Reset button selected.
     * If attached to a MIPS register at the time, the register will be reset as well.
     * Overrides inherited method that does nothing.
     */
    protected void reset() {
        instructions.setText(defaultInstructions);
        updateDisplaysAndRegister(new FlavorsOfFloat());
    }


    //////////////////////////////////////////////////////////////////////////////////////
    //  Private methods defined to support the above.

    protected JComponent buildDisplayArea() {
        // Panel to hold all floating point dislay and editing components
        Box mainPanel = Box.createVerticalBox();
        JPanel leftPanel = new JPanel(new GridLayout(5, 1, 0, 0));
        JPanel rightPanel = new JPanel(new GridLayout(5, 1, 0, 0));
        Box subMainPanel = Box.createHorizontalBox();
        subMainPanel.add(leftPanel);
        subMainPanel.add(rightPanel);
        mainPanel.add(subMainPanel);

        // Editable display for hexadecimal version of the float value
        hexDisplay = new JTextField(defaultHex, maxLengthHex + 1);
        hexDisplay.setFont(hexDisplayFont);
        hexDisplay.setForeground(hexDisplayColor);
        hexDisplay.setHorizontalAlignment(JTextField.RIGHT);
        hexDisplay.setToolTipText("" + maxLengthHex + "-digit hexadecimal (base 16) display");
        hexDisplay.setEditable(true);
        hexDisplay.revalidate();
        hexDisplay.addKeyListener(new HexDisplayKeystrokeListener(8));

        JPanel hexPanel = new JPanel();
        hexPanel.add(hexDisplay);
        //################  Grid Row :  Hexadecimal ##################################
        leftPanel.add(hexPanel);

        HexToBinaryGraphicPanel hexToBinaryGraphic = new HexToBinaryGraphicPanel();
        //################  Grid Row :  Hex-to-binary graphic ########################
        leftPanel.add(hexToBinaryGraphic);

        // Editable display for binary version of float value.
        // It is split into 3 separately editable components (sign,exponent,fraction)

        binarySignDisplay = new JTextField(defaultBinarySign, maxLengthBinarySign + 1);
        binarySignDisplay.setFont(binaryDisplayFont);
        binarySignDisplay.setForeground(binaryDisplayColor);
        binarySignDisplay.setHorizontalAlignment(JTextField.RIGHT);
        binarySignDisplay.setToolTipText("The sign bit");
        binarySignDisplay.setEditable(true);
        binarySignDisplay.revalidate();

        binaryExponentDisplay = new JTextField(defaultBinaryExponent, maxLengthBinaryExponent + 1);
        binaryExponentDisplay.setFont(binaryDisplayFont);
        binaryExponentDisplay.setForeground(binaryDisplayColor);
        binaryExponentDisplay.setHorizontalAlignment(JTextField.RIGHT);
        binaryExponentDisplay.setToolTipText("" + maxLengthBinaryExponent + "-bit exponent");
        binaryExponentDisplay.setEditable(true);
        binaryExponentDisplay.revalidate();

        binaryFractionDisplay = new BinaryFractionDisplayTextField(defaultBinaryFraction, maxLengthBinaryFraction + 1);
        binaryFractionDisplay.setFont(binaryDisplayFont);
        binaryFractionDisplay.setForeground(binaryDisplayColor);
        binaryFractionDisplay.setHorizontalAlignment(JTextField.RIGHT);
        binaryFractionDisplay.setToolTipText("" + maxLengthBinaryFraction + "-bit fraction");
        binaryFractionDisplay.setEditable(true);
        binaryFractionDisplay.revalidate();

        binarySignDisplay.addKeyListener(new BinaryDisplayKeystrokeListener(maxLengthBinarySign));
        binaryExponentDisplay.addKeyListener(new BinaryDisplayKeystrokeListener(maxLengthBinaryExponent));
        binaryFractionDisplay.addKeyListener(new BinaryDisplayKeystrokeListener(maxLengthBinaryFraction));
        JPanel binaryPanel = new JPanel();

        binarySignDecoratedDisplay = new JPanel(new BorderLayout());
        binaryExponentDecoratedDisplay = new JPanel(new BorderLayout());
        binaryFractionDecoratedDisplay = new JPanel(new BorderLayout());
        binarySignDecoratedDisplay.add(binarySignDisplay, BorderLayout.CENTER);
        binarySignDecoratedDisplay.add(new JLabel("sign", JLabel.CENTER), BorderLayout.SOUTH);
        binaryExponentDecoratedDisplay.add(binaryExponentDisplay, BorderLayout.CENTER);
        binaryExponentDecoratedDisplay.add(new JLabel("exponent", JLabel.CENTER), BorderLayout.SOUTH);
        binaryFractionDecoratedDisplay.add(binaryFractionDisplay, BorderLayout.CENTER);
        binaryFractionDecoratedDisplay.add(new JLabel("fraction", JLabel.CENTER), BorderLayout.SOUTH);

        binaryPanel.add(binarySignDecoratedDisplay);
        binaryPanel.add(binaryExponentDecoratedDisplay);
        binaryPanel.add(binaryFractionDecoratedDisplay);

        //################  Grid Row :  Binary ##################################
        leftPanel.add(binaryPanel);

        //################  Grid Row :  Binary to decimal formula arrows  ##########
        binaryToDecimalFormulaGraphic = new BinaryToDecimalFormulaGraphic();
        binaryToDecimalFormulaGraphic.setBackground(leftPanel.getBackground());
        leftPanel.add(binaryToDecimalFormulaGraphic);

        // Non-Editable display for expansion of binary representation

        expansionDisplay = new JLabel(new FlavorsOfFloat().expansionString);
        expansionDisplay.setFont(new Font("Monospaced", Font.PLAIN, 12));
        expansionDisplay.setFocusable(false); // causes it to be skipped in "tab sequence".
        expansionDisplay.setBackground(leftPanel.getBackground());
        JPanel expansionDisplayBox = new JPanel(new GridLayout(2, 1));
        expansionDisplayBox.add(expansionDisplay);
        expansionDisplayBox.add(significandLabel); // initialized at top
        //################  Grid Row :  Formula mapping binary to decimal ########
        leftPanel.add(expansionDisplayBox);

        // Editable display for decimal version of float value.
        decimalDisplay = new JTextField(defaultDecimal, maxLengthDecimal + 1);
        decimalDisplay.setFont(decimalDisplayFont);
        decimalDisplay.setForeground(decimalDisplayColor);
        decimalDisplay.setHorizontalAlignment(JTextField.RIGHT);
        decimalDisplay.setToolTipText("Decimal floating point value");
        decimalDisplay.setMargin(new Insets(0, 0, 0, 0));
        decimalDisplay.setEditable(true);
        decimalDisplay.revalidate();
        decimalDisplay.addKeyListener(new DecimalDisplayKeystokeListenter());
        Box decimalDisplayBox = Box.createVerticalBox();
        decimalDisplayBox.add(Box.createVerticalStrut(5));
        decimalDisplayBox.add(decimalDisplay);
        decimalDisplayBox.add(Box.createVerticalStrut(15));

        FlowLayout rightPanelLayout = new FlowLayout(FlowLayout.LEFT);
        JPanel place1 = new JPanel(rightPanelLayout);
        JPanel place2 = new JPanel(rightPanelLayout);
        JPanel place3 = new JPanel(rightPanelLayout);
        JPanel place4 = new JPanel(rightPanelLayout);

        JEditorPane hexExplain = new JEditorPane("text/html", expansionFontTag + "&lt;&nbsp;&nbsp;Hexadecimal representation" + "</font>");
        hexExplain.setEditable(false);
        hexExplain.setFocusable(false);
        hexExplain.setForeground(Color.black);
        hexExplain.setBackground(place1.getBackground());
        JEditorPane hexToBinExplain = new JEditorPane("text/html", expansionFontTag + "&lt;&nbsp;&nbsp;Each hex digit represents 4 bits" + "</font>");
        hexToBinExplain.setEditable(false);
        hexToBinExplain.setFocusable(false);
        hexToBinExplain.setBackground(place2.getBackground());
        JEditorPane binExplain = new JEditorPane("text/html", expansionFontTag + "&lt;&nbsp;&nbsp;Binary representation" + "</font>");
        binExplain.setEditable(false);
        binExplain.setFocusable(false);
        binExplain.setBackground(place3.getBackground());
        JEditorPane binToDecExplain = new JEditorPane("text/html", expansionFontTag + "&lt;&nbsp;&nbsp;Binary-to-decimal conversion" + "</font>");
        binToDecExplain.setEditable(false);
        binToDecExplain.setFocusable(false);
        binToDecExplain.setBackground(place4.getBackground());
        place1.add(hexExplain);
        place2.add(hexToBinExplain);
        place3.add(binExplain);
        place4.add(binToDecExplain);
        //################  4 Grid Rows :  Explanations #########################
        rightPanel.add(place1);
        rightPanel.add(place2);
        rightPanel.add(place3);
        rightPanel.add(place4);
        //################  Grid Row :  Decimal ##################################
        rightPanel.add(decimalDisplayBox);

        //########  mainPanel is vertical box, instructions get a row #################
        JPanel instructionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        instructions = new InstructionsPane(instructionsPanel);
        instructionsPanel.add(instructions);
        instructionsPanel.setBorder(new TitledBorder("Instructions"));
        mainPanel.add(instructionsPanel);

        // Means of selecting and deselecting an attached floating point register

        fpRegisters = Coprocessor1.getRegisters();
        String[] registerList = new String[fpRegisters.length + 1];
        registerList[0] = "None";
        for (int i = 0; i < fpRegisters.length; i++) {
            registerList[i + 1] = fpRegisters[i].getName();
        }
        JComboBox registerSelect = new JComboBox(registerList);
        registerSelect.setSelectedIndex(0);  // No register attached
        registerSelect.setToolTipText("Attach to selected FP register");
        registerSelect.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JComboBox cb = (JComboBox) e.getSource();
                        int selectedIndex = cb.getSelectedIndex();
                        if (isObserving()) {
                            deleteAsObserver();
                        }
                        if (selectedIndex == 0) {
                            attachedRegister = null;
                            updateDisplays(new FlavorsOfFloat());
                            instructions.setText("The program is not attached to any MIPS floating point registers.");
                        } else {
                            attachedRegister = fpRegisters[selectedIndex - 1];
                            updateDisplays(new FlavorsOfFloat().buildOneFromInt(attachedRegister.getValue()));
                            if (isObserving()) {
                                addAsObserver();
                            }
                            instructions.setText("The program and register " + attachedRegister.getName() + " will respond to each other when MIPS program connected or running.");
                        }
                    }
                });

        JPanel registerPanel = new JPanel(new BorderLayout(5, 5));
        JPanel registerAndLabel = new JPanel();
        registerAndLabel.add(new JLabel("MIPS floating point Register of interest: "));
        registerAndLabel.add(registerSelect);
        registerPanel.add(registerAndLabel, BorderLayout.WEST);
        registerPanel.add(new JLabel(" "), BorderLayout.NORTH); // just for padding
        mainPanel.add(registerPanel);
        return mainPanel;
    } // end of buildDisplayArea()


    // If display is attached to a register then update the register value.
    private synchronized void updateAnyAttachedRegister(int intValue) {
        if (attachedRegister != null) {
            synchronized (Globals.memoryAndRegistersLock) {
                attachedRegister.setValue(intValue);
            }
            // HERE'S A HACK!!  Want to immediately display the updated register value in MARS
            // but that code was not written for event-driven update (e.g. Observer) --
            // it was written to poll the registers for their values.  So we force it to do so.
            if (Globals.getGui() != null) {
                Globals.getGui().getRegistersPane().getCoprocessor1Window().updateRegisters();
            }
        }
    }

    // Updates all components displaying various representations of the 32 bit
    // floating point value.
    private void updateDisplays(FlavorsOfFloat flavors) {
        int hexIndex = (flavors.hexString.charAt(0) == '0' && (flavors.hexString.charAt(1) == 'x' || flavors.hexString.charAt(1) == 'X')) ? 2 : 0;
        hexDisplay.setText(flavors.hexString.substring(hexIndex).toUpperCase());  // lop off leading "Ox" if present
        binarySignDisplay.setText(flavors.binaryString.substring(0, maxLengthBinarySign));
        binaryExponentDisplay.setText(flavors.binaryString.substring(maxLengthBinarySign, maxLengthBinarySign + maxLengthBinaryExponent));
        binaryFractionDisplay.setText(flavors.binaryString.substring(maxLengthBinarySign + maxLengthBinaryExponent, maxLengthBinaryTotal));
        decimalDisplay.setText(flavors.decimalString);
        binaryToDecimalFormulaGraphic.drawSubtractLabel(Binary.binaryStringToInt((flavors.binaryString.substring(maxLengthBinarySign, maxLengthBinarySign + maxLengthBinaryExponent))));
        expansionDisplay.setText(flavors.expansionString);
        updateSignificandLabel(flavors);
    }

    // Should be called only by those who know a register should be changed due to
    // user action (reset button or Enter key on one of the input fields).  Note
    // this will not update the register unless we are an active Observer.
    private void updateDisplaysAndRegister(FlavorsOfFloat flavors) {
        updateDisplays(flavors);
        if (isObserving()) {
            updateAnyAttachedRegister(flavors.intValue);
        }
    }

    // Called by updateDisplays() to determine whether or not the significand label needs
    // to be changed and if so to change it.  The label explains presence/absence of
    // normalizing "hidden bit".
    private void updateSignificandLabel(FlavorsOfFloat flavors) {
        // Will change significandLabel text only if it needs to be changed...
        if (flavors.binaryString.substring(maxLengthBinarySign, maxLengthBinarySign + maxLengthBinaryExponent)
                .equals(zeroes.substring(maxLengthBinarySign, maxLengthBinarySign + maxLengthBinaryExponent))) {
            // Will change text only if it truly is changing....
            if (significandLabel.getText().indexOf("deno") < 0) {
                significandLabel.setText(denormalizedLabel);
            }
        } else {
            if (significandLabel.getText().indexOf("unde") < 0) {
                significandLabel.setText(normalizedLabel);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    ///////
    ///////   THE REST OF THE TOOL CONSISTS OF LITTLE PRIVATE CLASSES THAT MAKE
    ///////   LIFE EASIER FOR THE ABOVE CODE.
    ///////
    ///////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    //
    // Class of objects that encapsulats 5 different representations of a 32 bit
    // floating point value:
    //   string with hexadecimal value.
    //   String with binary value.  32 characters long.
    //   String with decimal float value.  variable length.
    //   int with 32 bit representation of float value ("int bits").
    //   String for display only, showing formula for expanding bits to decimal.
    //
    private class FlavorsOfFloat {
        String hexString;
        String binaryString;
        String decimalString;
        String expansionString;
        int intValue;

        // Default object
        private FlavorsOfFloat() {
            hexString = defaultHex;
            decimalString = defaultDecimal;
            binaryString = defaultBinarySign + defaultBinaryExponent + defaultBinaryFraction;
            expansionString = buildExpansionFromBinaryString(binaryString);
            intValue = Float.floatToIntBits(Float.parseFloat(decimalString));
        }

        //  Assign all fields given a string representing 32 bit hex value.
        public FlavorsOfFloat buildOneFromHexString(String hexString) {
            this.hexString = "0x" + addLeadingZeroes(
                    ((hexString.indexOf("0X") == 0 || hexString.indexOf("0x") == 0)
                            ? hexString.substring(2) : hexString), maxLengthHex);
            this.binaryString = Binary.hexStringToBinaryString(this.hexString);
            this.decimalString = new Float(Float.intBitsToFloat(Binary.binaryStringToInt(this.binaryString))).toString();
            this.expansionString = buildExpansionFromBinaryString(this.binaryString);
            this.intValue = Binary.binaryStringToInt(this.binaryString);
            return this;
        }

        //  Assign all fields given a string representing 32 bit binary value
        private FlavorsOfFloat buildOneFromBinaryString() {
            this.binaryString = getFullBinaryStringFromDisplays();
            this.hexString = Binary.binaryStringToHexString(binaryString);
            this.decimalString = new Float(Float.intBitsToFloat(Binary.binaryStringToInt(this.binaryString))).toString();
            this.expansionString = buildExpansionFromBinaryString(this.binaryString);
            this.intValue = Binary.binaryStringToInt(this.binaryString);
            return this;
        }

        //  Assign all fields given string representing floating point decimal value.
        private FlavorsOfFloat buildOneFromDecimalString(String decimalString) {
            float floatValue;
            try {
                floatValue = Float.parseFloat(decimalString);
            } catch (NumberFormatException nfe) {
                return null;
            }
            this.decimalString = new Float(floatValue).toString();
            this.intValue = Float.floatToIntBits(floatValue);// use floatToRawIntBits?
            this.binaryString = Binary.intToBinaryString(this.intValue);
            this.hexString = Binary.binaryStringToHexString(this.binaryString);
            this.expansionString = buildExpansionFromBinaryString(this.binaryString);
            return this;
        }

        //  Assign all fields given int representing 32 bit representation of float value
        private FlavorsOfFloat buildOneFromInt(int intValue) {
            this.intValue = intValue;
            this.binaryString = Binary.intToBinaryString(intValue);
            this.hexString = Binary.binaryStringToHexString(this.binaryString);
            this.decimalString = new Float(Float.intBitsToFloat(Binary.binaryStringToInt(this.binaryString))).toString();
            this.expansionString = buildExpansionFromBinaryString(this.binaryString);
            return this;
        }

        //  Build binary expansion formula for display -- will not be editable.
        public String buildExpansionFromBinaryString(String binaryString) {
            int biasedExponent = Binary.binaryStringToInt(
                    binaryString.substring(maxLengthBinarySign, maxLengthBinarySign + maxLengthBinaryExponent));
            String stringExponent = Integer.toString(biasedExponent - exponentBias);
            // stringExponent length will range from 1 to 4 (e.g. "0" to "-128") characters.
            // Right-pad with HTML spaces ("&nbsp;") to total length 5 displayed characters.
            return "<html><head></head><body>" + expansionFontTag
                    + "-1<sup>" + binaryString.substring(0, maxLengthBinarySign) + "</sup> &nbsp;*&nbsp; 2<sup>"
                    + stringExponent + HTMLspaces.substring(0, (5 - stringExponent.length()) * 6)
                    + "</sup> &nbsp;* &nbsp;"
                    + ((biasedExponent == 0) ? "&nbsp;." : "<u>1</u>.")
                    + binaryString.substring(maxLengthBinarySign + maxLengthBinaryExponent, maxLengthBinaryTotal)
                    + " =</font></body></html>";
        }

        // Handy utility to concatentate the binary field values into one 32 character string
        // Left-pad each field with zeroes as needed to reach its full length.
        private String getFullBinaryStringFromDisplays() {
            return addLeadingZeroes(binarySignDisplay.getText(), maxLengthBinarySign) +
                    addLeadingZeroes(binaryExponentDisplay.getText(), maxLengthBinaryExponent) +
                    addLeadingZeroes(binaryFractionDisplay.getText(), maxLengthBinaryFraction);
        }

        // Handy utility. Pads with leading zeroes to specified length, maximum 64 of 'em.
        private String addLeadingZeroes(String str, int length) {
            return (str.length() < length)
                    ? zeroes.substring(0, Math.min(zeroes.length(), length - str.length())) + str
                    : str;
        }

    }

    //Put here because inner class cannot have static members.
    private static final String zeroes =
            "0000000000000000000000000000000000000000000000000000000000000000"; //64
    private static final String HTMLspaces = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";


    // NOTE: It would be nice to use InputVerifier class to verify user input
    // but I want keystroke-level monitoring to assure that no invalid
    // keystrokes are echoed and that maximum string length is not exceeded.


    //////////////////////////////////////////////////////////////////
    //
    //  Class to handle input keystrokes for hexadecimal field
    //
    private class HexDisplayKeystrokeListener extends KeyAdapter {

        private int digitLength; // maximum number of digits long

        public HexDisplayKeystrokeListener(int length) {
            digitLength = length;
        }


        // Process user keystroke.  If not valid for the context, this
        // will consume the stroke and beep.
        public void keyTyped(KeyEvent e) {
            JTextField source = (JTextField) e.getComponent();
            if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE || e.getKeyChar() == KeyEvent.VK_TAB)
                return;
            if (!isHexDigit(e.getKeyChar()) ||
                    source.getText().length() == digitLength && source.getSelectedText() == null) {
                if (e.getKeyChar() != KeyEvent.VK_ENTER && e.getKeyChar() != KeyEvent.VK_TAB) {
                    Toolkit.getDefaultToolkit().beep();
                    if (source.getText().length() == digitLength && source.getSelectedText() == null) {
                        instructions.setText("Maximum length of this field is " + digitLength + ".");
                    } else {
                        instructions.setText("Only digits and A-F (or a-f) are accepted in hexadecimal field.");
                    }
                }
                e.consume();
            }
        }

        // Enter key is echoed on component after keyPressed but before keyTyped?
        // Consuming the VK_ENTER event in keyTyped does not suppress it but this will.
        public void keyPressed(KeyEvent e) {
            if (e.getKeyChar() == KeyEvent.VK_ENTER || e.getKeyChar() == KeyEvent.VK_TAB) {
                updateDisplaysAndRegister(new FlavorsOfFloat().buildOneFromHexString(((JTextField) e.getSource()).getText()));
                instructions.setText(defaultInstructions);
                e.consume();
            }
        }

        // handy utility.
        private boolean isHexDigit(char digit) {
            boolean result = false;
            switch (digit) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                    result = true;
            }
            return result;
        }
    }

    //////////////////////////////////////////////////////////////////
    //
    //  Class to handle input keystrokes for binary field
    //
    private class BinaryDisplayKeystrokeListener extends KeyAdapter {

        private int bitLength;  // maximum number of bits permitted

        public BinaryDisplayKeystrokeListener(int length) {
            bitLength = length;
        }

        // Process user keystroke.  If not valid for the context, this
        // will consume the stroke and beep.
        public void keyTyped(KeyEvent e) {
            JTextField source = (JTextField) e.getComponent();
            if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE)
                return;
            if (!isBinaryDigit(e.getKeyChar()) ||
                    e.getKeyChar() == KeyEvent.VK_ENTER ||
                    source.getText().length() == bitLength && source.getSelectedText() == null) {
                if (e.getKeyChar() != KeyEvent.VK_ENTER) {
                    Toolkit.getDefaultToolkit().beep();
                    if (source.getText().length() == bitLength && source.getSelectedText() == null) {
                        instructions.setText("Maximum length of this field is " + bitLength + ".");
                    } else {
                        instructions.setText("Only 0 and 1 are accepted in binary field.");
                    }
                }
                e.consume();
            }
        }

        // Enter key is echoed on component after keyPressed but before keyTyped?
        // Consuming the VK_ENTER event in keyTyped does not suppress it but this will.
        public void keyPressed(KeyEvent e) {
            if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                updateDisplaysAndRegister(new FlavorsOfFloat().buildOneFromBinaryString());
                instructions.setText(defaultInstructions);
                e.consume();
            }
        }

        // handy utility
        private boolean isBinaryDigit(char digit) {
            boolean result = false;
            switch (digit) {
                case '0':
                case '1':
                    result = true;
            }
            return result;
        }

    }


    //////////////////////////////////////////////////////////////////
    //
    //  Class to handle input keystrokes for decimal field
    //
    private class DecimalDisplayKeystokeListenter extends KeyAdapter {

        // Process user keystroke.  If not valid for the context, this
        // will consume the stroke and beep.
        public void keyTyped(KeyEvent e) {
            JTextField source = (JTextField) e.getComponent();
            if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE)
                return;
            if (!isDecimalFloatDigit(e.getKeyChar())) {
                if (e.getKeyChar() != KeyEvent.VK_ENTER) {
                    instructions.setText("Only digits, period, signs and E (or e) are accepted in decimal field.");
                    Toolkit.getDefaultToolkit().beep();
                }
                e.consume();
            }
        }

        // Enter key is echoed on component after keyPressed but before keyTyped?
        // Consuming the VK_ENTER event in keyTyped does not suppress it but this will.
        public void keyPressed(KeyEvent e) {
            if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                FlavorsOfFloat fof = new FlavorsOfFloat().buildOneFromDecimalString(((JTextField) e.getSource()).getText());
                if (fof == null) {
                    Toolkit.getDefaultToolkit().beep();
                    instructions.setText("'" + ((JTextField) e.getSource()).getText() + "' is not a valid floating point number.");
                } else {
                    updateDisplaysAndRegister(fof);
                    instructions.setText(defaultInstructions);
                }
                e.consume();
            }
        }

        // handy utility
        private boolean isDecimalFloatDigit(char digit) {
            boolean result = false;
            switch (digit) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '-':
                case '+':
                case '.':
                case 'e':
                case 'E':
                    result = true;
            }
            return result;
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    //
    //  Use this to draw graphics visually relating the hexadecimal values
    //  displayed above) to the binary values (displayed below).
    //
    class HexToBinaryGraphicPanel extends JPanel {

        // This overrides inherited JPanel method.  Override is necessary to
        // assure my drawn graphics get painted immediately after painting the
        // underlying JPanel (see first statement).
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.red);
            //FontMetrics fontMetrics = hexDisplay.getGraphics().getFontMetrics();
            int upperY = 0;
            int lowerY = 60;
            int hexColumnWidth = hexDisplay.getWidth() / hexDisplay.getColumns();
            // assume all 3 binary displays use same geometry, so column width same for all.
            int binaryColumnWidth = binaryFractionDisplay.getWidth() / binaryFractionDisplay.getColumns();
            Polygon p;
            // loop will handle the lower order 5 "nibbles" (hex digits)
            for (int i = 1; i < 6; i++) {
                p = new Polygon();
                p.addPoint(hexDisplay.getX() + hexColumnWidth * (hexDisplay.getColumns() - i) + hexColumnWidth / 2, upperY);
                p.addPoint(binaryFractionDecoratedDisplay.getX() + binaryColumnWidth * (binaryFractionDisplay.getColumns() - ((i * 5) - i)), lowerY);
                p.addPoint(binaryFractionDecoratedDisplay.getX() + binaryColumnWidth * (binaryFractionDisplay.getColumns() - (((i * 5) - i) - 4)), lowerY);
                g.fillPolygon(p);
            }
            // Nibble 5 straddles binary display of exponent and fraction.
            p = new Polygon();
            p.addPoint(hexDisplay.getX() + hexColumnWidth * (hexDisplay.getColumns() - 6) + hexColumnWidth / 2, upperY);
            p.addPoint(binaryFractionDecoratedDisplay.getX() + binaryColumnWidth * (binaryFractionDisplay.getColumns() - 20), lowerY);
            p.addPoint(binaryExponentDecoratedDisplay.getX() + binaryColumnWidth * (binaryExponentDisplay.getColumns() - 1), lowerY);
            g.fillPolygon(p);
            // Nibble 6 maps to binary display of exponent.
            p = new Polygon();
            p.addPoint(hexDisplay.getX() + hexColumnWidth * (hexDisplay.getColumns() - 7) + hexColumnWidth / 2, upperY);
            p.addPoint(binaryExponentDecoratedDisplay.getX() + binaryColumnWidth * (binaryExponentDisplay.getColumns() - 1), lowerY);
            p.addPoint(binaryExponentDecoratedDisplay.getX() + binaryColumnWidth * (binaryExponentDisplay.getColumns() - 5), lowerY);
            g.fillPolygon(p);
            // Nibble 7 straddles binary display of sign and exponent.
            p = new Polygon();
            p.addPoint(hexDisplay.getX() + hexColumnWidth * (hexDisplay.getColumns() - 8) + hexColumnWidth / 2, upperY);
            p.addPoint(binaryExponentDecoratedDisplay.getX() + binaryColumnWidth * (binaryExponentDisplay.getColumns() - 5), lowerY);
            p.addPoint(binarySignDecoratedDisplay.getX(), lowerY);
            g.fillPolygon(p);
        }

    }

    //////////////////////////////////////////////////////////////////////
    //
    //  Panel to hold arrows explaining transformation of binary represntation
    //  into formula for calculating decimal value.
    //
    class BinaryToDecimalFormulaGraphic extends JPanel {
        final String subtractLabelTrailer = " - 127";
        final int arrowHeadOffset = 5;
        final int lowerY = 0;
        final int upperY = 50;
        int centerX, exponentCenterX;
        int subtractLabelWidth, subtractLabelHeight;
        int centerY = (upperY - lowerY) / 2;
        int upperYArrowHead = upperY - arrowHeadOffset;
        int currentExponent = Binary.binaryStringToInt(defaultBinaryExponent);

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Arrow down from binary sign field
            centerX = binarySignDecoratedDisplay.getX() + binarySignDecoratedDisplay.getWidth() / 2;
            g.drawLine(centerX, lowerY, centerX, upperY);
            g.drawLine(centerX - arrowHeadOffset, upperYArrowHead, centerX, upperY);
            g.drawLine(centerX + arrowHeadOffset, upperYArrowHead, centerX, upperY);
            // Arrow down from binary exponent field
            centerX = binaryExponentDecoratedDisplay.getX() + binaryExponentDecoratedDisplay.getWidth() / 2;
            g.drawLine(centerX, lowerY, centerX, upperY);
            g.drawLine(centerX - arrowHeadOffset, upperYArrowHead, centerX, upperY);
            g.drawLine(centerX + arrowHeadOffset, upperYArrowHead, centerX, upperY);
            // Label on exponent arrow.  The two assignments serve to initialize two
            // instance variables that are used by drawSubtractLabel().  They are
            // initialized here because they cannot be initialized sooner AND because
            // the drawSubtractLabel() method will later be called by updateDisplays(),
            // an outsider which has no other access to that information.  Once set they
            // do not change so it does no harm that they are "re-initialized" each time
            // this method is called (which occurs only upon startup and when this portion
            // of the GUI needs to be repainted).
            exponentCenterX = centerX;
            subtractLabelHeight = g.getFontMetrics().getHeight();
            drawSubtractLabel(g, buildSubtractLabel(currentExponent));
            // Arrow down from binary fraction field
            centerX = binaryFractionDecoratedDisplay.getX() + binaryFractionDecoratedDisplay.getWidth() / 2;
            g.drawLine(centerX, lowerY, centerX, upperY);
            g.drawLine(centerX - arrowHeadOffset, upperYArrowHead, centerX, upperY);
            g.drawLine(centerX + arrowHeadOffset, upperYArrowHead, centerX, upperY);
        }

        // To be used only by "outsiders" to update the display of the exponent and bias.
        public void drawSubtractLabel(int exponent) {
            if (exponent != currentExponent) { // no need to redraw if it hasn't changed...
                currentExponent = exponent;
                drawSubtractLabel(getGraphics(), buildSubtractLabel(exponent));
            }
        }

        // Is called by both drawSubtractLabel() just above and by paintComponent().
        private void drawSubtractLabel(Graphics g, String label) {
            // Clear the existing subtract label.  The "+2" overwrites the arrow at initial paint when label width is 0.
            // Originally used "clearRect()" but changed to "fillRect()" with background color, because when running
            // as a MarsTool it would clear with a different color.
            Color saved = g.getColor();
            g.setColor(binaryToDecimalFormulaGraphic.getBackground());
            g.fillRect(exponentCenterX - subtractLabelWidth / 2, centerY - subtractLabelHeight / 2, subtractLabelWidth + 2, subtractLabelHeight);
            g.setColor(saved);
            subtractLabelWidth = g.getFontMetrics().stringWidth(label);
            g.drawString(label, exponentCenterX - subtractLabelWidth / 2, centerY + subtractLabelHeight / 2 - 3); // -3 makes it more visually appealing
        }

        // format the label for a given integer exponent value...
        private String buildSubtractLabel(int value) {
            return Integer.toString(value) + subtractLabelTrailer;
        }

    }


    /////////////////////////////////////////////////////////////////////////
    //
    //  Handly little class defined only to allow client to use "setText()" without
    //  needing to know how/whether the text needs to be formatted.  This one is
    //  used to display instructions.

    class InstructionsPane extends JLabel {

        InstructionsPane(Component parent) {
            super(defaultInstructions);
            this.setFont(instructionsFont);
            this.setBackground(parent.getBackground());
        }

        public void setText(String text) {
            super.setText(text);
        }
    }


    ////////////////////////////////////////////////////////////////////////////
    //
    //  Use this to draw custom background in the binary fraction display.
    //
    class BinaryFractionDisplayTextField extends JTextField {

        public BinaryFractionDisplayTextField(String value, int columns) {
            super(value, columns);
        }

        // This overrides inherited JPanel method.  Override is necessary to
        // assure my drawn graphics get painted immediately after painting the
        // underlying JPanel (see first statement).
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            // The code below is commented out because I decided to abandon
            // my effort to provide "striped" background that alternates colors
            // for every 4 characters (bits) of the display.  This would make
            // the correspondence between bits and hex digits very clear.
            // NOTE: this is the only reason for subclassing JTextField.

         	/*
            int columnWidth = getWidth()/getColumns();
            Color shadedColor = Color.red;
            Polygon p;
            // loop will handle the lower order 5 "nibbles" (hex digits)
            for (int i=3; i<20; i+=8) {
               p = new Polygon();
               p.addPoint(getX()+columnWidth*i, getY());
               p.addPoint(getX()+columnWidth*i, getY()+getHeight());
               p.addPoint(getX()+columnWidth*(i+4), getY()+getHeight());
               p.addPoint(getX()+columnWidth*(i+4), getY());
         //			System.out.println("Polygon vertices are:"+
         //			" ("+(getX()+columnWidth*i)    +","+getY()              +") "+
         //			" ("+(getX()+columnWidth*i)    +","+(getY()+getHeight())+") "+
         //			" ("+(getX()+columnWidth*(i+4))+","+(getY()+getHeight())+") "+
         //			" ("+(getX()+columnWidth*(i+4))+","+getY()              +") "
         //			); 
               g.setColor(shadedColor);
               g.fillPolygon(p);
            }*/
         	/*
            // Nibble 5 straddles binary display of exponent and fraction.
            p = new Polygon();
            p.addPoint(hexDisplay.getX()+hexColumnWidth*(hexDisplay.getColumns()-6)+hexColumnWidth/2, upperY);
            p.addPoint(binaryFractionDisplay.getX()+binaryColumnWidth*(binaryFractionDisplay.getColumns()-20), lowerY);
            p.addPoint(binaryExponentDisplay.getX()+binaryColumnWidth*(binaryExponentDisplay.getColumns()-1), lowerY);
            g.fillPolygon(p);
         	// Nibble 6 maps to binary display of exponent.
            p = new Polygon();
            p.addPoint(hexDisplay.getX()+hexColumnWidth*(hexDisplay.getColumns()-7)+hexColumnWidth/2, upperY);
            p.addPoint(binaryExponentDisplay.getX()+binaryColumnWidth*(binaryExponentDisplay.getColumns()-1), lowerY);
            p.addPoint(binaryExponentDisplay.getX()+binaryColumnWidth*(binaryExponentDisplay.getColumns()-5), lowerY);
            g.fillPolygon(p);
         	// Nibble 7 straddles binary display of sign and exponent.
            p = new Polygon();
            p.addPoint(hexDisplay.getX()+hexColumnWidth*(hexDisplay.getColumns()-8)+hexColumnWidth/2, upperY);
            p.addPoint(binaryExponentDisplay.getX()+binaryColumnWidth*(binaryExponentDisplay.getColumns()-5), lowerY);
            p.addPoint(binarySignDisplay.getX(), lowerY);
            g.fillPolygon(p);
         	*/
        }
    }

}