package rars.venus;

import rars.Globals;
import rars.venus.editors.TextEditingArea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Action  for the Edit -> Find/Replace menu item
 */
public class EditFindReplaceAction extends GuiAction {
    private static String searchString = "";
    private static boolean caseSensitivity = true;
    private static final String DIALOG_TITLE = "Find and Replace";
    private MainPane mainPane;

    private JDialog findReplaceDialog;

    public EditFindReplaceAction(String name, Icon icon, String descrip,
                                 Integer mnemonic, KeyStroke accel, VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel);
        mainPane = gui.getMainPane();
    }

    public void actionPerformed(ActionEvent e) {
        findReplaceDialog = new FindReplaceDialog(Globals.getGui(), DIALOG_TITLE, false);
        findReplaceDialog.setVisible(true);
    }

    //////////////////////////////////////////////////////////////////////////////
    //
    //   Private class to do all the work!
    //
    private class FindReplaceDialog extends JDialog {
        JButton findButton, replaceButton, replaceAllButton, closeButton;
        JTextField findInputField, replaceInputField;
        JCheckBox caseSensitiveCheckBox;
        JRadioButton linearFromStart, circularFromCursor;
        private JLabel resultsLabel;

        public static final String FIND_TOOL_TIP_TEXT = "Find next occurrence of given text; wraps around at end";
        public static final String REPLACE_TOOL_TIP_TEXT = "Replace current occurrence of text then find next";
        public static final String REPLACE_ALL_TOOL_TIP_TEXT = "Replace all occurrences of text";
        public static final String CLOSE_TOOL_TIP_TEXT = "Close the dialog";
        public static final String RESULTS_TOOL_TIP_TEXT = "Outcome of latest operation (button click)";

        public static final String RESULTS_TEXT_FOUND = "Text found";
        public static final String RESULTS_TEXT_NOT_FOUND = "Text not found";
        public static final String RESULTS_TEXT_REPLACED = "Text replaced and found next";
        public static final String RESULTS_TEXT_REPLACED_LAST = "Text replaced; last occurrence";
        public static final String RESULTS_TEXT_REPLACED_ALL = "Replaced";
        public static final String RESULTS_NO_TEXT_TO_FIND = "No text to find";

        public FindReplaceDialog(Frame owner, String title, boolean modality) {
            super(owner, title, modality);
            this.setContentPane(buildDialogPanel());
            this.setDefaultCloseOperation(
                    JDialog.DO_NOTHING_ON_CLOSE);
            this.addWindowListener(
                    new WindowAdapter() {
                        public void windowClosing(WindowEvent we) {
                            performClose();
                        }
                    });
            this.pack();
            this.setLocationRelativeTo(owner);
        }

        // Constructs the dialog's main panel.
        private JPanel buildDialogPanel() {
            JPanel dialogPanel = new JPanel(new BorderLayout());
            dialogPanel.setBorder(new javax.swing.border.EmptyBorder(10, 10, 10, 10));
            dialogPanel.add(buildInputPanel(), BorderLayout.NORTH);
            dialogPanel.add(buildOptionsPanel());
            dialogPanel.add(buildControlPanel(), BorderLayout.SOUTH);
            return dialogPanel;
        }

        // Top part of the dialog, to contain the two input text fields.
        private Component buildInputPanel() {
            findInputField = new JTextField(30);
            if (searchString.length() > 0) {
                findInputField.setText(searchString);
                findInputField.selectAll();
            }
            replaceInputField = new JTextField(30);
            JPanel inputPanel = new JPanel();
            JPanel labelsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
            JPanel fieldsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
            labelsPanel.add(new JLabel("Find what:"));
            labelsPanel.add(new JLabel("Replace with:"));
            fieldsPanel.add(findInputField);
            fieldsPanel.add(replaceInputField);

            Box columns = Box.createHorizontalBox();
            columns.add(labelsPanel);
            columns.add(Box.createHorizontalStrut(6));
            columns.add(fieldsPanel);
            inputPanel.add(columns);
            return inputPanel;
        }

        // Center part of the dialog, which contains the check box
        // for case sensitivity along with a label to display the
        // outcome of each operation.
        private Component buildOptionsPanel() {
            Box optionsPanel = Box.createHorizontalBox();
            caseSensitiveCheckBox = new JCheckBox("Case Sensitive", caseSensitivity);
            JPanel casePanel = new JPanel(new GridLayout(2, 1));
            casePanel.add(caseSensitiveCheckBox);
            casePanel.setMaximumSize(casePanel.getPreferredSize());
            optionsPanel.add(casePanel);
            optionsPanel.add(Box.createHorizontalStrut(5));
            JPanel resultsPanel = new JPanel(new GridLayout(1, 1));
            resultsPanel.setBorder(BorderFactory.createTitledBorder("Outcome"));
            resultsLabel = new JLabel("");
            resultsLabel.setForeground(Color.RED);
            resultsLabel.setToolTipText(RESULTS_TOOL_TIP_TEXT);
            resultsPanel.add(resultsLabel);
            optionsPanel.add(resultsPanel);
            return optionsPanel;
        }


        // Row of control buttons to be placed along the button of the dialog
        private Component buildControlPanel() {
            Box controlPanel = Box.createHorizontalBox();
            controlPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
            findButton = new JButton("Find");
            findButton.setToolTipText(FIND_TOOL_TIP_TEXT);
            findButton.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            performFind();
                        }
                    });
            replaceButton = new JButton("Replace then Find");
            replaceButton.setToolTipText(REPLACE_TOOL_TIP_TEXT);
            replaceButton.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            performReplace();
                        }
                    });
            replaceAllButton = new JButton("Replace all");
            replaceAllButton.setToolTipText(REPLACE_ALL_TOOL_TIP_TEXT);
            replaceAllButton.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            performReplaceAll();
                        }
                    });
            closeButton = new JButton("Close");
            closeButton.setToolTipText(CLOSE_TOOL_TIP_TEXT);
            closeButton.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            performClose();
                        }
                    });
            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(findButton);
            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(replaceButton);
            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(replaceAllButton);
            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(closeButton);
            controlPanel.add(Box.createHorizontalGlue());
            return controlPanel;
        }

        ////////////////////////////////////////////////////////////////////////
        //
        //  Private methods to carry out the button actions

        //  Performs a find.  The operation starts at the current cursor position
        //  which is not known to this object but is maintained by the EditPane
        //  object.  The operation will wrap around when it reaches the end of the
        //  document.  If found, the matching text is selected.
        private void performFind() {
            resultsLabel.setText("");
            if (findInputField.getText().length() > 0) {
                // Being cautious. Should not be null because find/replace tool button disabled if no file open
                EditPane editPane = mainPane.getEditPane();
                if (editPane != null) {
                    searchString = findInputField.getText();
                    int posn = editPane.doFindText(searchString, caseSensitiveCheckBox.isSelected());
                    if (posn == TextEditingArea.TEXT_NOT_FOUND) {
                        resultsLabel.setText(findButton.getText() + ": " + RESULTS_TEXT_NOT_FOUND);
                    } else {
                        resultsLabel.setText(findButton.getText() + ": " + RESULTS_TEXT_FOUND);
                    }
                }
            } else {
                resultsLabel.setText(findButton.getText() + ": " + RESULTS_NO_TEXT_TO_FIND);
            }
        }

        // Performs a replace-and-find.  If the matched text is current selected with cursor at
        // its end, the replace happens immediately followed by a find for the next occurrence.
        // Otherwise, it performs a find.  This will select the matching text so the next press
        // of Replace will do the replace.  This is apparently common behavior for replace
        // buttons of different apps I've checked.
        private void performReplace() {
            resultsLabel.setText("");
            if (findInputField.getText().length() > 0) {
                // Being cautious. Should not be null b/c find/replace tool button disabled if no file open
                EditPane editPane = mainPane.getEditPane();
                if (editPane != null) {
                    searchString = findInputField.getText();
                    int posn = editPane.doReplace(searchString, replaceInputField.getText(), caseSensitiveCheckBox.isSelected());
                    String result = replaceButton.getText() + ": ";
                    switch (posn) {

                        case TextEditingArea.TEXT_NOT_FOUND:
                            result += RESULTS_TEXT_NOT_FOUND;
                            break;
                        case TextEditingArea.TEXT_FOUND:
                            result += RESULTS_TEXT_FOUND;
                            break;
                        case TextEditingArea.TEXT_REPLACED_NOT_FOUND_NEXT:
                            result += RESULTS_TEXT_REPLACED_LAST;
                            break;
                        case TextEditingArea.TEXT_REPLACED_FOUND_NEXT:
                            result += RESULTS_TEXT_REPLACED;
                            break;
                    }
                    resultsLabel.setText(result);
                }
            } else {
                resultsLabel.setText(replaceButton.getText() + ": " + RESULTS_NO_TEXT_TO_FIND);
            }

        }

        // Performs a replace-all.  Makes one pass through the document starting at
        // position 0.
        private void performReplaceAll() {
            resultsLabel.setText("");
            if (findInputField.getText().length() > 0) {
                // Being cautious. Should not be null b/c find/replace tool button disabled if no file open
                EditPane editPane = mainPane.getEditPane();
                if (editPane != null) {
                    searchString = findInputField.getText();
                    int replaceCount = editPane.doReplaceAll(searchString, replaceInputField.getText(), caseSensitiveCheckBox.isSelected());
                    if (replaceCount == 0) {
                        resultsLabel.setText(replaceAllButton.getText() + ": " + RESULTS_TEXT_NOT_FOUND);
                    } else {
                        resultsLabel.setText(replaceAllButton.getText() + ": " + RESULTS_TEXT_REPLACED_ALL + " " + replaceCount + " occurrence" + (replaceCount == 1 ? "" : "s"));
                    }
                }
            } else {
                resultsLabel.setText(replaceAllButton.getText() + ": " + RESULTS_NO_TEXT_TO_FIND);
            }
        }

        // Performs the close operation.  Records the current state of the case-sensitivity
        // checkbox into a static variable so it will be remembered across invocations within
        // the session.  This also happens with the contents of the "find" text field.
        private void performClose() {
            caseSensitivity = caseSensitiveCheckBox.isSelected();
            this.setVisible(false);
            this.dispose();
        }
        //
        ////////////////////////////////////////////////////////////////////////////////
    }

}