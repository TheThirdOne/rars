package rars.venus.settings;

import rars.Globals;
import rars.Settings;
import rars.venus.Editor;
import rars.venus.GuiAction;
import rars.venus.editors.jeditsyntax.SyntaxStyle;
import rars.venus.editors.jeditsyntax.SyntaxUtilities;
import rars.venus.editors.jeditsyntax.tokenmarker.RISCVTokenMarker;
import rars.venus.util.AbstractFontSettingDialog;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Caret;
import java.awt.*;
import java.awt.event.*;

	/*
Copyright (c) 2003-2011,  Pete Sanderson and Kenneth Vollmar

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
 * Action class for the Settings menu item for text editor settings.
 */
public class SettingsEditorAction extends GuiAction {

    private JDialog editorDialog;

    /**
     * Create a new SettingsEditorAction.  Has all the GuiAction parameters.
     */
    public SettingsEditorAction(String name, Icon icon, String descrip,
                                Integer mnemonic, KeyStroke accel) {
        super(name, icon, descrip, mnemonic, accel);
    }

    /**
     * When this action is triggered, launch a dialog to view and modify
     * editor settings.
     */
    public void actionPerformed(ActionEvent e) {
        editorDialog = new EditorFontDialog(Globals.getGui(), "Text Editor Settings", true, Globals.getSettings().getEditorFont());
        editorDialog.setVisible(true);

    }

    private static final int gridVGap = 2;
    private static final int gridHGap = 2;
    private static final Border ColorSelectButtonEnabledBorder = new BevelBorder(BevelBorder.RAISED, Color.WHITE, Color.GRAY);
    private static final Border ColorSelectButtonDisabledBorder = new LineBorder(Color.GRAY, 2);

    private static final String GENERIC_TOOL_TIP_TEXT = "Use generic editor (original RARS editor, similar to Notepad) instead of language-aware styled editor";

    private static final String SAMPLE_TOOL_TIP_TEXT = "Current setting; modify using buttons to the right";
    private static final String FOREGROUND_TOOL_TIP_TEXT = "Click, to select text color";
    private static final String BOLD_TOOL_TIP_TEXT = "Toggle text bold style";
    private static final String ITALIC_TOOL_TIP_TEXT = "Toggle text italic style";
    private static final String DEFAULT_TOOL_TIP_TEXT = "Check, to select defaults (disables buttons)";
    private static final String BOLD_BUTTON_TOOL_TIP_TEXT = "B";
    private static final String ITALIC_BUTTON_TOOL_TIP_TEXT = "I";

    private static final String TAB_SIZE_TOOL_TIP_TEXT = "Current tab size in characters";
    private static final String BLINK_SPINNER_TOOL_TIP_TEXT = "Current blinking rate in milliseconds";
    private static final String BLINK_SAMPLE_TOOL_TIP_TEXT = "Displays current blinking rate";
    private static final String CURRENT_LINE_HIGHLIGHT_TOOL_TIP_TEXT = "Check, to highlight line currently being edited";
    private static final String AUTO_INDENT_TOOL_TIP_TEXT = "Check, to enable auto-indent to previous line when Enter key is pressed";
    private static final String[] POPUP_GUIDANCE_TOOL_TIP_TEXT = {"Turns off instruction and directive guide popup while typing",
            "Generates instruction guide popup after first letter of potential instruction is typed",
            "Generates instruction guide popup after second letter of potential instruction is typed"
    };

    // Concrete font chooser class.
    private class EditorFontDialog extends AbstractFontSettingDialog {

        private JButton[] foregroundButtons;
        private JLabel[] samples;
        private JToggleButton[] bold, italic;
        private JCheckBox[] useDefault;

        private int[] syntaxStyleIndex;
        private SyntaxStyle[] defaultStyles, initialStyles, currentStyles;
        private Font previewFont;

        private JPanel dialogPanel, syntaxStylePanel, otherSettingsPanel; /////4 Aug 2010

        private JSlider tabSizeSelector;
        private JSpinner tabSizeSpinSelector, blinkRateSpinSelector, popupPrefixLengthSpinSelector;
        private JCheckBox lineHighlightCheck, genericEditorCheck, autoIndentCheck;
        private ColorChangerPanel bgChanger, fgChanger, lhChanger, textSelChanger, caretChanger;
        private Caret blinkCaret;
        private JTextField blinkSample;
        private ButtonGroup popupGuidanceButtons;
        private JRadioButton[] popupGuidanceOptions;
        // Flag to indicate whether any syntax style buttons have been clicked
        // since dialog created or most recent "apply".
        private boolean syntaxStylesAction = false;

        private int initialEditorTabSize, initialCaretBlinkRate, initialPopupGuidance;
        private boolean initialLineHighlighting, initialGenericTextEditor, initialAutoIndent;

        public EditorFontDialog(Frame owner, String title, boolean modality, Font font) {
            super(owner, title, modality, font);
            if (Globals.getSettings().getBooleanSetting(Settings.Bool.GENERIC_TEXT_EDITOR)) {
                syntaxStylePanel.setVisible(false);
                otherSettingsPanel.setVisible(false);
            }
        }

        // build the dialog here
        protected JPanel buildDialogPanel() {
            JPanel dialog = new JPanel(new BorderLayout());
            JPanel fontDialogPanel = super.buildDialogPanel();
            JPanel editorStylePanel = buildEditorStylePanel();
            JPanel syntaxStylePanel = buildSyntaxStylePanel();
            JPanel otherSettingsPanel = buildOtherSettingsPanel();
            fontDialogPanel.setBorder(BorderFactory.createTitledBorder("Editor Font"));
            editorStylePanel.setBorder(BorderFactory.createTitledBorder("Editor Style"));
            syntaxStylePanel.setBorder(BorderFactory.createTitledBorder("Syntax Styling"));
            otherSettingsPanel.setBorder(BorderFactory.createTitledBorder("Other Editor Settings"));
            dialog.add(fontDialogPanel, BorderLayout.WEST);
            dialog.add(editorStylePanel, BorderLayout.CENTER);
            dialog.add(syntaxStylePanel, BorderLayout.EAST);
            dialog.add(otherSettingsPanel, BorderLayout.SOUTH);
            this.dialogPanel = dialog; /////4 Aug 2010
            this.syntaxStylePanel = syntaxStylePanel; /////4 Aug 2010
            this.otherSettingsPanel = otherSettingsPanel; /////4 Aug 2010
            return dialog;
        }

        // Row of control buttons to be placed along the button of the dialog
        protected Component buildControlPanel() {
            Box controlPanel = Box.createHorizontalBox();
            JButton okButton = new JButton("Apply and Close");
            okButton.setToolTipText(SettingsHighlightingAction.CLOSE_TOOL_TIP_TEXT);
            okButton.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            performApply();
                            closeDialog();
                        }
                    });
            JButton applyButton = new JButton("Apply");
            applyButton.setToolTipText(SettingsHighlightingAction.APPLY_TOOL_TIP_TEXT);
            applyButton.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            performApply();
                        }
                    });
            JButton cancelButton = new JButton("Cancel");
            cancelButton.setToolTipText(SettingsHighlightingAction.CANCEL_TOOL_TIP_TEXT);
            cancelButton.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            closeDialog();
                        }
                    });
            JButton resetButton = new JButton("Reset");
            resetButton.setToolTipText(SettingsHighlightingAction.RESET_TOOL_TIP_TEXT);
            resetButton.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            reset();
                        }
                    });
            initialGenericTextEditor = Globals.getSettings().getBooleanSetting(Settings.Bool.GENERIC_TEXT_EDITOR);
            genericEditorCheck = new JCheckBox("Use Generic Editor", initialGenericTextEditor);
            genericEditorCheck.setToolTipText(GENERIC_TOOL_TIP_TEXT);
            genericEditorCheck.addItemListener(
                    new ItemListener() {
                        public void itemStateChanged(ItemEvent e) {
                            if (e.getStateChange() == ItemEvent.SELECTED) {
                                syntaxStylePanel.setVisible(false);
                                otherSettingsPanel.setVisible(false);
                            } else {
                                syntaxStylePanel.setVisible(true);
                                otherSettingsPanel.setVisible(true);
                            }
                        }
                    });

            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(okButton);
            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(applyButton);
            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(cancelButton);
            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(resetButton);
            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(genericEditorCheck);
            controlPanel.add(Box.createHorizontalGlue());
            return controlPanel;
        }

        // User has clicked "Apply" or "Apply and Close" button.  Required method, is
        // abstract in superclass.
        protected void apply(Font font) {
            Globals.getSettings().setBooleanSetting(Settings.Bool.GENERIC_TEXT_EDITOR, genericEditorCheck.isSelected());
            Globals.getSettings().setBooleanSetting(Settings.Bool.EDITOR_CURRENT_LINE_HIGHLIGHTING, lineHighlightCheck.isSelected());
            Globals.getSettings().setBooleanSetting(Settings.Bool.AUTO_INDENT, autoIndentCheck.isSelected());
            Globals.getSettings().setCaretBlinkRate((Integer) blinkRateSpinSelector.getValue());
            Globals.getSettings().setEditorTabSize(tabSizeSelector.getValue());
            bgChanger.save();
            fgChanger.save();
            textSelChanger.save();
            caretChanger.save();
            lhChanger.save();

            if (syntaxStylesAction) {
                for (int i = 0; i < syntaxStyleIndex.length; i++) {
                    Globals.getSettings().setEditorSyntaxStyleByPosition(syntaxStyleIndex[i],
                            new SyntaxStyle(samples[i].getForeground(),
                                    italic[i].isSelected(), bold[i].isSelected()));
                }
                syntaxStylesAction = false; // reset
            }
            Globals.getSettings().setEditorFont(font);
            for (int i = 0; i < popupGuidanceOptions.length; i++) {
                if (popupGuidanceOptions[i].isSelected()) {
                    if (i == 0) {
                        Globals.getSettings().setBooleanSetting(Settings.Bool.POPUP_INSTRUCTION_GUIDANCE, false);
                    } else {
                        Globals.getSettings().setBooleanSetting(Settings.Bool.POPUP_INSTRUCTION_GUIDANCE, true);
                        Globals.getSettings().setEditorPopupPrefixLength(i);
                    }
                    break;
                }
            }
        }

        // User has clicked "Reset" button.  Put everything back to initial state.
        protected void reset() {
            super.reset();
            initializeSyntaxStyleChangeables();
            resetOtherSettings();
            syntaxStylesAction = true;
            genericEditorCheck.setSelected(initialGenericTextEditor);
        }

        // Perform reset on miscellaneous editor settings
        private void resetOtherSettings() {
            tabSizeSelector.setValue(initialEditorTabSize);
            tabSizeSpinSelector.setValue(initialEditorTabSize);
            lineHighlightCheck.setSelected(initialLineHighlighting);
            autoIndentCheck.setSelected(initialAutoIndent);
            blinkRateSpinSelector.setValue(initialCaretBlinkRate);
            blinkCaret.setBlinkRate(initialCaretBlinkRate);
            bgChanger.reset();
            fgChanger.reset();
            lhChanger.reset();
            textSelChanger.reset();
            caretChanger.reset();
            popupGuidanceOptions[initialPopupGuidance].setSelected(true);
        }

        // Miscellaneous editor settings (cursor blinking, line highlighting, tab size, etc)
        private JPanel buildOtherSettingsPanel() {
            JPanel otherSettingsPanel = new JPanel();

            // Tab size selector
            initialEditorTabSize = Globals.getSettings().getEditorTabSize();
            tabSizeSelector = new JSlider(Editor.MIN_TAB_SIZE, Editor.MAX_TAB_SIZE, initialEditorTabSize);
            tabSizeSelector.setToolTipText("Use slider to select tab size from " + Editor.MIN_TAB_SIZE + " to " + Editor.MAX_TAB_SIZE + ".");
            tabSizeSelector.addChangeListener(
                    new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            Integer value = ((JSlider) e.getSource()).getValue();
                            tabSizeSpinSelector.setValue(value);
                        }
                    });
            SpinnerNumberModel tabSizeSpinnerModel = new SpinnerNumberModel(initialEditorTabSize, Editor.MIN_TAB_SIZE, Editor.MAX_TAB_SIZE, 1);
            tabSizeSpinSelector = new JSpinner(tabSizeSpinnerModel);
            tabSizeSpinSelector.setToolTipText(TAB_SIZE_TOOL_TIP_TEXT);
            tabSizeSpinSelector.addChangeListener(
                    new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            Object value = ((JSpinner) e.getSource()).getValue();
                            tabSizeSelector.setValue((Integer) value);
                        }
                    });

            // highlighting of current line
            initialLineHighlighting = Globals.getSettings().getBooleanSetting(Settings.Bool.EDITOR_CURRENT_LINE_HIGHLIGHTING);
            lineHighlightCheck = new JCheckBox("Highlight the line currently being edited");
            lineHighlightCheck.setSelected(initialLineHighlighting);
            lineHighlightCheck.setToolTipText(CURRENT_LINE_HIGHLIGHT_TOOL_TIP_TEXT);

            // auto-indent
            initialAutoIndent = Globals.getSettings().getBooleanSetting(Settings.Bool.AUTO_INDENT);
            autoIndentCheck = new JCheckBox("Auto-Indent");
            autoIndentCheck.setSelected(initialAutoIndent);
            autoIndentCheck.setToolTipText(AUTO_INDENT_TOOL_TIP_TEXT);

            // cursor blink rate selector
            initialCaretBlinkRate = Globals.getSettings().getCaretBlinkRate();
            blinkSample = new JTextField("     ");
            blinkSample.setCaretPosition(2);
            blinkSample.setToolTipText(BLINK_SAMPLE_TOOL_TIP_TEXT);
            blinkSample.setEnabled(false);
            blinkCaret = blinkSample.getCaret();
            blinkCaret.setBlinkRate(initialCaretBlinkRate);
            blinkCaret.setVisible(true);
            SpinnerNumberModel blinkRateSpinnerModel = new SpinnerNumberModel(initialCaretBlinkRate, Editor.MIN_BLINK_RATE, Editor.MAX_BLINK_RATE, 100);
            blinkRateSpinSelector = new JSpinner(blinkRateSpinnerModel);
            blinkRateSpinSelector.setToolTipText(BLINK_SPINNER_TOOL_TIP_TEXT);
            blinkRateSpinSelector.addChangeListener(
                    new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            Object value = ((JSpinner) e.getSource()).getValue();
                            blinkCaret.setBlinkRate((Integer) value);
                            blinkSample.requestFocus();
                            blinkCaret.setVisible(true);
                        }
                    });

            JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            tabPanel.add(new JLabel("Tab Size"));
            tabPanel.add(tabSizeSelector);
            tabPanel.add(tabSizeSpinSelector);

            JPanel blinkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            blinkPanel.add(new JLabel("Cursor Blinking Rate in ms (0=no blink)"));
            blinkPanel.add(blinkRateSpinSelector);
            blinkPanel.add(blinkSample);

            otherSettingsPanel.setLayout(new GridLayout(1, 2));
            JPanel leftColumnSettingsPanel = new JPanel(new GridLayout(4, 1));
            leftColumnSettingsPanel.add(tabPanel);
            leftColumnSettingsPanel.add(blinkPanel);
            leftColumnSettingsPanel.add(lineHighlightCheck);
            leftColumnSettingsPanel.add(autoIndentCheck);

            // Combine instruction guide off/on and instruction prefix length into radio buttons
            JPanel rightColumnSettingsPanel = new JPanel(new GridLayout(4, 1));
            popupGuidanceButtons = new ButtonGroup();
            popupGuidanceOptions = new JRadioButton[3];
            popupGuidanceOptions[0] = new JRadioButton("No popup instruction or directive guide");
            popupGuidanceOptions[1] = new JRadioButton("Display instruction guide after 1 letter typed");
            popupGuidanceOptions[2] = new JRadioButton("Display instruction guide after 2 letters typed");
            for (int i = 0; i < popupGuidanceOptions.length; i++) {
                popupGuidanceOptions[i].setSelected(false);
                popupGuidanceOptions[i].setToolTipText(POPUP_GUIDANCE_TOOL_TIP_TEXT[i]);
                popupGuidanceButtons.add(popupGuidanceOptions[i]);
            }
            initialPopupGuidance = Globals.getSettings().getBooleanSetting(Settings.Bool.POPUP_INSTRUCTION_GUIDANCE)
                    ? Globals.getSettings().getEditorPopupPrefixLength()
                    : 0;
            popupGuidanceOptions[initialPopupGuidance].setSelected(true);
            JPanel popupPanel = new JPanel(new GridLayout(3, 1));
            rightColumnSettingsPanel.setBorder(BorderFactory.createTitledBorder("Popup Instruction Guide"));
            rightColumnSettingsPanel.add(popupGuidanceOptions[0]);
            rightColumnSettingsPanel.add(popupGuidanceOptions[1]);
            rightColumnSettingsPanel.add(popupGuidanceOptions[2]);

            otherSettingsPanel.add(leftColumnSettingsPanel);
            otherSettingsPanel.add(rightColumnSettingsPanel);
            return otherSettingsPanel;
        }


        // Editor style panel
        private JPanel buildEditorStylePanel() {
            JPanel editorStylePanel = new JPanel(new GridLayout(5, 3, gridHGap, gridVGap));
            bgChanger = new ColorChangerPanel("Background", "Select the Editor's Background-Color", Settings.EDITOR_BACKGROUND);
            fgChanger = new ColorChangerPanel("Foreground", "Select the Editor's Foreground-Color", Settings.EDITOR_FOREGROUND);
            lhChanger = new ColorChangerPanel("Line-Highlight", "Select the Editor's Line-Highlight-Color", Settings.EDITOR_LINE_HIGHLIGHT);
            textSelChanger = new ColorChangerPanel("Text-Selection", "Select the Editor's Text-Selection-Color", Settings.EDITOR_SELECTION_COLOR);
            caretChanger = new ColorChangerPanel("Caret", "Select the Editor's Caret-Color", Settings.EDITOR_CARET_COLOR);

            bgChanger.addElements(editorStylePanel);
            fgChanger.addElements(editorStylePanel);
            lhChanger.addElements(editorStylePanel);
            textSelChanger.addElements(editorStylePanel);
            caretChanger.addElements(editorStylePanel);

            return editorStylePanel;
        }

        // control style (color, plain/italic/bold) for syntax highlighting
        private JPanel buildSyntaxStylePanel() {
            JPanel syntaxStylePanel = new JPanel();
            defaultStyles = SyntaxUtilities.getDefaultSyntaxStyles();
            initialStyles = SyntaxUtilities.getCurrentSyntaxStyles();
            String[] labels = RISCVTokenMarker.getRISCVTokenLabels();
            String[] sampleText = RISCVTokenMarker.getRISCVTokenExamples();
            syntaxStylesAction = false;
            int count = 0;
            // Count the number of actual styles specified
            for (String label : labels) {
                if (label != null) {
                    count++;
                }
            }
            // create new arrays (no gaps) for grid display, refer to original index
            syntaxStyleIndex = new int[count];
            currentStyles = new SyntaxStyle[count];
            String[] label = new String[count];
            samples = new JLabel[count];
            foregroundButtons = new JButton[count];
            bold = new JToggleButton[count];
            italic = new JToggleButton[count];
            useDefault = new JCheckBox[count];
            Font genericFont = new JLabel().getFont();
            previewFont = new Font(Font.MONOSPACED, Font.PLAIN, genericFont.getSize());// no bold on button text
            Font boldFont = new Font(Font.SERIF, Font.BOLD, genericFont.getSize());
            Font italicFont = new Font(Font.SERIF, Font.ITALIC, genericFont.getSize());
            count = 0;
            // Set all the fixed features.  Changeable features set/reset in initializeSyntaxStyleChangeables
            for (int i = 0; i < labels.length; i++) {
                if (labels[i] != null) {
                    syntaxStyleIndex[count] = i;
                    samples[count] = new JLabel();
                    samples[count].setOpaque(true);
                    samples[count].setHorizontalAlignment(SwingConstants.CENTER);
                    samples[count].setBorder(BorderFactory.createLineBorder(Color.black));
                    samples[count].setText(sampleText[i]);
                    samples[count].setBackground(Color.WHITE);
                    samples[count].setToolTipText(SAMPLE_TOOL_TIP_TEXT);
                    foregroundButtons[count] = new ColorSelectButton(); // defined in SettingsHighlightingAction
                    foregroundButtons[count].addActionListener(new ForegroundChanger(count));
                    foregroundButtons[count].setToolTipText(FOREGROUND_TOOL_TIP_TEXT);
                    BoldItalicChanger boldItalicChanger = new BoldItalicChanger(count);
                    bold[count] = new JToggleButton(BOLD_BUTTON_TOOL_TIP_TEXT, false);
                    bold[count].setFont(boldFont);
                    bold[count].addActionListener(boldItalicChanger);
                    bold[count].setToolTipText(BOLD_TOOL_TIP_TEXT);
                    italic[count] = new JToggleButton(ITALIC_BUTTON_TOOL_TIP_TEXT, false);
                    italic[count].setFont(italicFont);
                    italic[count].addActionListener(boldItalicChanger);
                    italic[count].setToolTipText(ITALIC_TOOL_TIP_TEXT);
                    label[count] = labels[i];
                    useDefault[count] = new JCheckBox();
                    useDefault[count].addItemListener(new DefaultChanger(count));
                    useDefault[count].setToolTipText(DEFAULT_TOOL_TIP_TEXT);
                    count++;
                }
            }
            initializeSyntaxStyleChangeables();
            // build a grid
            syntaxStylePanel.setLayout(new BorderLayout());
            JPanel labelPreviewPanel = new JPanel(new GridLayout(syntaxStyleIndex.length, 2, gridVGap, gridHGap));
            JPanel buttonsPanel = new JPanel(new GridLayout(syntaxStyleIndex.length, 4, gridVGap, gridHGap));
            // column 1: label,  column 2: preview, column 3: foreground chooser, column 4/5: bold/italic, column 6: default
            for (int i = 0; i < syntaxStyleIndex.length; i++) {
                labelPreviewPanel.add(new JLabel(label[i], SwingConstants.RIGHT));
                labelPreviewPanel.add(samples[i]);
                buttonsPanel.add(foregroundButtons[i]);
                buttonsPanel.add(bold[i]);
                buttonsPanel.add(italic[i]);
                buttonsPanel.add(useDefault[i]);
            }
            JPanel instructions = new JPanel(new FlowLayout(FlowLayout.CENTER));
            // create deaf, dumb and blind checkbox, for illustration
            JCheckBox illustrate =
                    new JCheckBox() {
                        protected void processMouseEvent(MouseEvent e) {
                        }

                        protected void processKeyEvent(KeyEvent e) {
                        }
                    };
            illustrate.setSelected(true);
            instructions.add(illustrate);
            instructions.add(new JLabel("= use defaults (disables buttons)"));
            labelPreviewPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            buttonsPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            syntaxStylePanel.add(instructions, BorderLayout.NORTH);
            syntaxStylePanel.add(labelPreviewPanel, BorderLayout.WEST);
            syntaxStylePanel.add(buttonsPanel, BorderLayout.CENTER);
            return syntaxStylePanel;
        }


        // Set or reset the changeable features of component for syntax style
        private void initializeSyntaxStyleChangeables() {
            for (int count = 0; count < samples.length; count++) {
                int i = syntaxStyleIndex[count];
                samples[count].setFont(previewFont);
                samples[count].setForeground(initialStyles[i].getColor());
                foregroundButtons[count].setBackground(initialStyles[i].getColor());
                foregroundButtons[count].setEnabled(true);
                currentStyles[count] = initialStyles[i];
                bold[count].setSelected(initialStyles[i].isBold());
                if (bold[count].isSelected()) {
                    Font f = samples[count].getFont();
                    samples[count].setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
                }
                italic[count].setSelected(initialStyles[i].isItalic());
                if (italic[count].isSelected()) {
                    Font f = samples[count].getFont();
                    samples[count].setFont(f.deriveFont(f.getStyle() ^ Font.ITALIC));
                }
                useDefault[count].setSelected(initialStyles[i].toString().equals(defaultStyles[i].toString()));
                if (useDefault[count].isSelected()) {
                    foregroundButtons[count].setEnabled(false);
                    bold[count].setEnabled(false);
                    italic[count].setEnabled(false);
                }
            }
        }


        // set the foreground color, bold and italic of sample (a JLabel)
        private void setSampleStyles(JLabel sample, SyntaxStyle style) {
            Font f = previewFont;
            if (style.isBold()) {
                f = f.deriveFont(f.getStyle() ^ Font.BOLD);
            }
            if (style.isItalic()) {
                f = f.deriveFont(f.getStyle() ^ Font.ITALIC);
            }
            sample.setFont(f);
            sample.setForeground(style.getColor());
        }


        ///////////////////////////////////////////////////////////////////////////
        // Toggle bold or italic style on preview button when B or I button clicked
        private class BoldItalicChanger implements ActionListener {
            private int row;

            public BoldItalicChanger(int row) {
                this.row = row;
            }

            public void actionPerformed(ActionEvent e) {
                Font f = samples[row].getFont();
                if (e.getActionCommand() == BOLD_BUTTON_TOOL_TIP_TEXT) {
                    if (bold[row].isSelected()) {
                        samples[row].setFont(f.deriveFont(f.getStyle() | Font.BOLD));
                    } else {
                        samples[row].setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
                    }
                } else {
                    if (italic[row].isSelected()) {
                        samples[row].setFont(f.deriveFont(f.getStyle() | Font.ITALIC));
                    } else {
                        samples[row].setFont(f.deriveFont(f.getStyle() ^ Font.ITALIC));
                    }
                }
                currentStyles[row] = new SyntaxStyle(foregroundButtons[row].getBackground(),
                        italic[row].isSelected(), bold[row].isSelected());
                syntaxStylesAction = true;

            }
        }

        /////////////////////////////////////////////////////////////////
        //
        //  Class that handles click on the foreground selection button
        //
        private class ForegroundChanger implements ActionListener {
            private int row;

            public ForegroundChanger(int pos) {
                row = pos;
            }

            public void actionPerformed(ActionEvent e) {
                JButton button = (JButton) e.getSource();
                Color newColor = JColorChooser.showDialog(null, "Set Text Color", button.getBackground());
                if (newColor != null) {
                    button.setBackground(newColor);
                    samples[row].setForeground(newColor);
                }
                currentStyles[row] = new SyntaxStyle(button.getBackground(),
                        italic[row].isSelected(), bold[row].isSelected());
                syntaxStylesAction = true;
            }
        }

        /**
         * Panel to change colors via {@link Settings}
         */
        class ColorChangerPanel extends JPanel {
            private final int index;
            private final ColorSelectButton colorSelect;
            private final JButton modeButton;
            private final JLabel label;
            private Settings.ColorMode mode;
            private Color color;

            /**
             * Creates a {@link ColorChangerPanel}
             * @param label The label to be put next to the changer
             * @param title The title of the dialogue to be opened
             * @param index the index of the color for
             *  {@link Settings#getColorSettingByPosition(int)} and {@link Settings#setColorSettingByPosition(int, Color)}
             */
            public ColorChangerPanel(String label, String title, int index) {
                super(new FlowLayout(FlowLayout.LEFT));
                this.index = index;
                this.label = new JLabel(label);
                add(this.label);
                colorSelect = new ColorSelectButton(); // defined in SettingsHighlightingAction
                color = Globals.getSettings().getColorSettingByPosition(index);
                mode = Globals.getSettings().getColorModeByPosition(index);
                colorSelect.setBackground(color);
                colorSelect.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Color newColor = JColorChooser.showDialog(null, title, colorSelect.getBackground());
                        if (newColor != null) {
                            color = newColor;
                            setMode(Settings.ColorMode.CUSTOM);
                            colorSelect.setBackground(color);
                        }
                    }
                });
                add(colorSelect);

                modeButton = new JButton(mode.name());
                modeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setMode(mode == Settings.ColorMode.DEFAULT ? Settings.ColorMode.SYSTEM : Settings.ColorMode.DEFAULT);
                    }
                });
                add(modeButton);

                setToolTipText(title);
            }

            public void reset() {
                setMode(Globals.getSettings().getDefaultColorMode());
            }

            public void save() {
                if (mode == Settings.ColorMode.CUSTOM) {
                    Globals.getSettings().setColorSettingByPosition(index, colorSelect.getBackground());
                } else {
                    Globals.getSettings().setColorSettingByPosition(index, mode);
                }
                updateColorBySetting();
            }

            public void updateColorBySetting() {
                color = Globals.getSettings().getColorSettingByPosition(index);
                colorSelect.setBackground(color);
            }

            private void setMode(Settings.ColorMode mode) {
                this.mode = mode;
                modeButton.setText(mode.name());
                if (mode != Settings.ColorMode.CUSTOM) {
                    color = Globals.getSettings().previewColorModeByPosition(index, mode);
                    colorSelect.setBackground(color);
                }
            }

            public void addElements(JPanel gridPanel) {
                gridPanel.add(label);
                gridPanel.add(colorSelect);
                gridPanel.add(modeButton);
            }
        }

        /////////////////////////////////////////////////////////////////
        //
        // Class that handles action (check, uncheck) on the Default checkbox.
        //
        private class DefaultChanger implements ItemListener {
            private int row;

            public DefaultChanger(int pos) {
                row = pos;
            }

            public void itemStateChanged(ItemEvent e) {

                // If selected: disable buttons, save current settings, set to defaults
                // If deselected:restore current settings, enable buttons
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    foregroundButtons[row].setEnabled(false);
                    bold[row].setEnabled(false);
                    italic[row].setEnabled(false);
                    currentStyles[row] = new SyntaxStyle(foregroundButtons[row].getBackground(),
                            italic[row].isSelected(), bold[row].isSelected());
                    SyntaxStyle defaultStyle = defaultStyles[syntaxStyleIndex[row]];
                    setSampleStyles(samples[row], defaultStyle);
                    foregroundButtons[row].setBackground(defaultStyle.getColor());
                    bold[row].setSelected(defaultStyle.isBold());
                    italic[row].setSelected(defaultStyle.isItalic());
                } else {
                    setSampleStyles(samples[row], currentStyles[row]);
                    foregroundButtons[row].setBackground(currentStyles[row].getColor());
                    bold[row].setSelected(currentStyles[row].isBold());
                    italic[row].setSelected(currentStyles[row].isItalic());
                    foregroundButtons[row].setEnabled(true);
                    bold[row].setEnabled(true);
                    italic[row].setEnabled(true);
                }
                syntaxStylesAction = true;
            }
        }
    }

}