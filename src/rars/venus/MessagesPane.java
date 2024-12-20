package rars.venus;

import rars.CancelException;
import rars.ErrorList;
import rars.Globals;
import rars.Settings;
import rars.simulator.ProgramArgumentList;
import rars.simulator.Simulator;
import rars.util.SystemIO;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position.Bias;
import javax.swing.undo.UndoableEdit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Creates the message window at the bottom of the UI.
 *
 * @author Team JSpim
 **/

public class MessagesPane extends JPanel {
    private final Box runButtonBox;
    private final JRadioButton buttonPopup;
    private final JRadioButton buttonInteractive;
    private final JRadioButton buttonBatch;
    private final JButton inputTabClearButton;
    private final Box runioContent;
    private final JTextField programArguments;
    private JTabbedPane leftPane;
    private JSplitPane batchTab;
    JTextArea assemble, run, input, output;
    private JPanel assembleTab, runTab, inputTab, runioTab;
    // These constants are designed to keep scrolled contents of the
    // two message areas from becoming overwhelmingly large (which
    // seems to slow things down as new text is appended).  Once it
    // reaches MAXIMUM_SCROLLED_CHARACTERS in length then cut off
    // the first NUMBER_OF_CHARACTERS_TO_CUT characters.  The latter
    // must obviously be smaller than the former.
    public static final int MAXIMUM_SCROLLED_CHARACTERS = Globals.maximumMessageCharacters;
    public static final int NUMBER_OF_CHARACTERS_TO_CUT = Globals.maximumMessageCharacters / 10; // 10%

    /**
     * Constructor for the class, sets up two fresh tabbed text areas for program feedback.
     **/

    public MessagesPane() {
        super();
        this.setMinimumSize(new Dimension(0, 0));
        leftPane = new JTabbedPane();
        assemble = new JTextArea();
        run = new JTextArea();
        input = new JTextArea();
        output = new JTextArea();
        programArguments = new JTextField();
        assemble.setEditable(false);
        run.setEditable(false);
        input.setEditable(true);
        output.setEditable(false);
        // Set both text areas to mono font.  For assemble
        // pane, will make messages more readable.  For run
        // pane, will allow properly aligned "text graphics"
        // DPS 15 Dec 2008
        Font monoFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        assemble.setFont(monoFont);
        run.setFont(monoFont);
        input.setFont(monoFont);
        output.setFont(monoFont);
        programArguments.setFont(monoFont);

        JButton assembleTabClearButton = new JButton("Clear");
        assembleTabClearButton.setToolTipText("Clear the Messages area");
        assembleTabClearButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        assemble.setText("");
                    }
                });
        assembleTab = new JPanel(new BorderLayout());
        assembleTab.add(createBoxForComponent(assembleTabClearButton), BorderLayout.WEST);
        assembleTab.add(new JScrollPane(assemble, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        assemble.addMouseListener(
                new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        String text;
                        int lineStart = 0;
                        int lineEnd = 0;
                        try {
                            int line = assemble.getLineOfOffset(assemble.viewToModel(e.getPoint()));
                            lineStart = assemble.getLineStartOffset(line);
                            lineEnd = assemble.getLineEndOffset(line);
                            text = assemble.getText(lineStart, lineEnd - lineStart);
                        } catch (BadLocationException ble) {
                            text = "";
                        }
                        if (text.length() > 0) {
                            // If error or warning, parse out the line and column number.
                            if (text.startsWith(ErrorList.ERROR_MESSAGE_PREFIX) || text.startsWith(ErrorList.WARNING_MESSAGE_PREFIX)) {
                                assemble.select(lineStart, lineEnd);
                                assemble.setSelectionColor(Color.YELLOW);
                                assemble.setSelectedTextColor(Color.BLACK);
                                assemble.repaint();
                                int separatorPosition = text.indexOf(ErrorList.MESSAGE_SEPARATOR);
                                if (separatorPosition >= 0) {
                                    text = text.substring(0, separatorPosition);
                                }
                                String[] stringTokens = text.split("\\s"); // tokenize with whitespace delimiter
                                String lineToken = ErrorList.LINE_PREFIX.trim();
                                String columnToken = ErrorList.POSITION_PREFIX.trim();
                                String lineString = "";
                                String columnString = "";
                                for (int i = 0; i < stringTokens.length; i++) {
                                    if (stringTokens[i].equals(lineToken) && i < stringTokens.length - 1)
                                        lineString = stringTokens[i + 1];
                                    if (stringTokens[i].equals(columnToken) && i < stringTokens.length - 1)
                                        columnString = stringTokens[i + 1];
                                }
                                int line = 0;
                                int column = 0;
                                try {
                                    line = Integer.parseInt(lineString);
                                } catch (NumberFormatException nfe) {
                                    line = 0;
                                }
                                try {
                                    column = Integer.parseInt(columnString);
                                } catch (NumberFormatException nfe) {
                                    column = 0;
                                }
                                // everything between FILENAME_PREFIX and LINE_PREFIX is filename.
                                int fileNameStart = text.indexOf(ErrorList.FILENAME_PREFIX) + ErrorList.FILENAME_PREFIX.length();
                                int fileNameEnd = text.indexOf(ErrorList.LINE_PREFIX);
                                String fileName = "";
                                if (fileNameStart < fileNameEnd && fileNameStart >= ErrorList.FILENAME_PREFIX.length()) {
                                    fileName = text.substring(fileNameStart, fileNameEnd).trim();
                                }
                                if (fileName != null && fileName.length() > 0) {
                                    selectEditorTextLine(fileName, line, column);
                                    selectErrorMessage(fileName, line, column);
                                }
                            }
                        }
                    }
                });

        JButton runTabClearButton = new JButton("Clear output");
        runTabClearButton.setToolTipText("Clear the output area");
        runTabClearButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        run.setText("");
                        output.setText("");
                    }
                });
        inputTabClearButton = new JButton("Clear input");
        inputTabClearButton.setToolTipText("Clear the input area (batch mode only)");
        inputTabClearButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        input.setText("");
                    }
                });

        /* Stack of buttons on the left */
        runButtonBox = Box.createVerticalBox();
        buttonPopup = new JRadioButton("Popups");
        buttonInteractive = new JRadioButton("Interactive");
        buttonBatch = new JRadioButton("Batch");
        ButtonGroup bg = new ButtonGroup();
        bg.add(buttonPopup);
        bg.add(buttonInteractive);
        bg.add(buttonBatch);
        ActionListener bl = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               updateIOTab();
            }
        };
        buttonPopup.addActionListener(bl);
        buttonInteractive.addActionListener(bl);
        buttonBatch.addActionListener(bl);
        runButtonBox.add(buttonPopup);
        runButtonBox.add(buttonInteractive);
        runButtonBox.add(buttonBatch);
        runButtonBox.add(runTabClearButton);
        runButtonBox.add(inputTabClearButton);
        if (Globals.getSettings().getBooleanSetting(Settings.Bool.BATCH_IOMODE)) {
            buttonBatch.setSelected(true);
        } else if (Globals.getSettings().getBooleanSetting(Settings.Bool.POPUP_SYSCALL_INPUT)) {
            buttonPopup.setSelected(true);
        } else {
            buttonInteractive.setSelected(true);
        }

        /* runTab: A single interactive text area */
        runTab = new JPanel(new BorderLayout());
        runTab.add(createBoxForComponent(runButtonBox), BorderLayout.WEST);
        runTab.add(new JScrollPane(run, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);

        /* batchTab: A dual text area */
        JPanel inputTab = new JPanel(new BorderLayout());
        inputTab.add(new JScrollPane(input, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        JPanel outputTab = new JPanel(new BorderLayout());
        outputTab.add(new JScrollPane(output, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        batchTab = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputTab, outputTab);
        batchTab.setOneTouchExpandable(true);
        batchTab.resetToPreferredSizes();
        batchTab.setResizeWeight(0.5);

        /* The runio, the content is dynamic */
        runioContent = Box.createHorizontalBox();
        runioTab = new JPanel(new BorderLayout());
        runioTab.add(createBoxForComponent(runButtonBox), BorderLayout.WEST);
        runioTab.add(runioContent);
        updateIOTab();

        /* The program argument tab */
        JButton programArgumentsClearButton = new JButton("Clear");
        programArgumentsClearButton.setToolTipText("Clear the program arguments");
        programArgumentsClearButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        programArguments.setText("");
                    }
                });
        JPanel programArgumentsPanel = new JPanel(new BorderLayout());
        programArgumentsPanel.add(createBoxForComponent(programArgumentsClearButton), BorderLayout.WEST);
        programArguments.setMaximumSize(new Dimension(Integer.MAX_VALUE, programArguments.getPreferredSize().height));
        programArgumentsPanel.add(createBoxForComponent(programArguments), BorderLayout.CENTER);

        leftPane.addTab("Messages", assembleTab);
        leftPane.addTab("Run I/O", runioTab);
        leftPane.addTab("Program Arguments", programArgumentsPanel);

        leftPane.setToolTipTextAt(0, "Messages produced by Run menu. Click on assemble error message to select erroneous line");
        leftPane.setToolTipTextAt(1, "Simulated console input (used while running) and other run messages");
        leftPane.setToolTipTextAt(2, "Arguments provided to program at runtime via a0 (argc) and a1 (argv)");

        this.setLayout(new BorderLayout());
        this.add(leftPane);
    }

    /** Update the content of the runio tab according to the selected radio buttons. */
    private void updateIOTab() {
        runioContent.removeAll();
        if (buttonBatch.isSelected() ) {
            Globals.getSettings().setBooleanSetting(Settings.Bool.BATCH_IOMODE, true);
            Globals.getSettings().setBooleanSetting(Settings.Bool.POPUP_SYSCALL_INPUT, false);
            runioContent.add(batchTab);
            inputTabClearButton.setEnabled(true);
        } else {
            Globals.getSettings().setBooleanSetting(Settings.Bool.BATCH_IOMODE, false);
            runioContent.add(runTab);
            Globals.getSettings().setBooleanSetting(Settings.Bool.POPUP_SYSCALL_INPUT, buttonPopup.isSelected());
            inputTabClearButton.setEnabled(false);
        }
        // Force the repaint and the application of the look-and-feel
        SwingUtilities.updateComponentTreeUI(runioContent);
    }

    // Center given button in a box, centered vertically and 6 pixels on left and right
    private Box createBoxForComponent(Component component) {
        Box componentRow = Box.createHorizontalBox();
        componentRow.add(Box.createHorizontalStrut(6));
        componentRow.add(component);
        componentRow.add(Box.createHorizontalStrut(6));
        Box componentBox = Box.createVerticalBox();
        componentBox.add(Box.createVerticalGlue());
        componentBox.add(componentRow);
        componentBox.add(Box.createVerticalGlue());
        return componentBox;
    }

    /**
     * Will select the Mars Messages tab error message that matches the given
     * specifications, if it is found. Matching is done by constructing
     * a string using the parameter values and searching the text area for the last
     * occurrance of that string.
     *
     * @param fileName A String containing the file path name.
     * @param line     Line number for error message
     * @param column   Column number for error message
     */
    public void selectErrorMessage(String fileName, int line, int column) {
        String errorReportSubstring = new java.io.File(fileName).getName() + ErrorList.LINE_PREFIX + line + ErrorList.POSITION_PREFIX + column;
        int textPosition = assemble.getText().lastIndexOf(errorReportSubstring);
        if (textPosition >= 0) {
            int textLine = 0;
            int lineStart = 0;
            int lineEnd = 0;
            try {
                textLine = assemble.getLineOfOffset(textPosition);
                lineStart = assemble.getLineStartOffset(textLine);
                lineEnd = assemble.getLineEndOffset(textLine);
                assemble.setSelectionColor(Color.YELLOW);
                assemble.setSelectedTextColor(Color.BLACK);
                assemble.select(lineStart, lineEnd);
                assemble.getCaret().setSelectionVisible(true);
                assemble.repaint();
            } catch (BadLocationException ble) {
                // If there is a problem, simply skip the selection
            }
        }
    }


    /**
     * Will select the specified line in an editor tab.  If the file is open
     * but not current, its tab will be made current.  If the file is not open,
     * it will be opened in a new tab and made current, however the line will
     * not be selected (apparent apparent problem with JEditTextArea).
     *
     * @param fileName A String containing the file path name.
     * @param line     Line number for error message
     * @param column   Column number for error message
     */
    public void selectEditorTextLine(String fileName, int line, int column) {
        EditTabbedPane editTabbedPane = (EditTabbedPane) Globals.getGui().getMainPane().getEditTabbedPane();
        EditPane editPane, currentPane = null;
        editPane = editTabbedPane.getEditPaneForFile(new java.io.File(fileName).getPath());
        if (editPane != null) {
            if (editPane != editTabbedPane.getCurrentEditTab()) {
                editTabbedPane.setCurrentEditTab(editPane);
            }
            currentPane = editPane;
        } else {    // file is not open.  Try to open it.
            if (editTabbedPane.openFile(new java.io.File(fileName))) {
                currentPane = editTabbedPane.getCurrentEditTab();
            }
        }
        // If editPane == null, it means the desired file was not open.  Line selection
        // does not properly with the JEditTextArea editor in this situation (it works
        // fine for the original generic editor).  So we just won't do it. DPS 9-Aug-2010
        if (editPane != null && currentPane != null) {
            currentPane.selectLine(line, column);
        }
    }

    /**
     * Returns component used to display assembler messages
     *
     * @return assembler message text component
     */
    public JTextArea getAssembleTextArea() {
        return assemble;
    }

    /**
     * Returns component used to display runtime messages
     *
     * @return runtime message text component
     */
    public JTextArea getRunTextArea() {
        return run;
    }

    /**
     * Return true if the execution is (or will be) in interactive mode and false for batch mode.
     * Currently, batch mode means that the input field is not empty.
     */
    public Boolean isInteractiveMode() {
        return ! Globals.getSettings().getBooleanSetting(Settings.Bool.BATCH_IOMODE);
    }

    /**
     * Returns the text written in the input field
     *
     * @return input text field content
     */
    public String getInputField() {
        return input.getText();
    }

    /** Append a message to a textarea and garbage collect very old text if needed. */
    private void append(JTextArea area, String string) {
        area.append(string);
        // can do some crude cutting here.  If the document gets "very large",
        // let's cut off the oldest text. This will limit scrolling but the limit
        // can be set reasonably high.
        if (area.getDocument().getLength() > MAXIMUM_SCROLLED_CHARACTERS) {
            try {
                area.getDocument().remove(0, NUMBER_OF_CHARACTERS_TO_CUT);
            } catch (BadLocationException ble) {
                // only if NUMBER_OF_CHARACTERS_TO_CUT > MAXIMUM_SCROLLED_CHARACTERS
            }
        }
        area.setCaretPosition(area.getDocument().getLength());
    }

    /**
     * Post a message to the assembler display
     *
     * @param message String to append to assembler display text
     */
    public void postMessage(String message) {
        append(assemble, message);
        leftPane.setSelectedComponent(assembleTab);
    }

    /**
     * Post a message to the runtime display
     *
     * @param message String to append to runtime display text
     */
    // The work of this method is done by "invokeLater" because
    // its JTextArea is maintained by the main event thread
    // but also used, via this method, by the execution thread for
    // "print" syscalls. "invokeLater" schedules the code to be
    // run under the event-processing thread no matter what.
    // DPS, 23 Aug 2005.
    public void postRunMessage(String message) {
        final String mess = message;
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        selectRunMessageTab();
                        append(run, mess);
                        append(output, mess);
                    }
                });
    }

    /**
     * Post a message to the output display
     *
     * @param message String to append to output display text
     */
    public void postOutput(String message) {
        final String mess = message;
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        append(run, mess);
                        append(output, mess);
                    }
                });
    }

    /**
     * Make the assembler message tab current (up front)
     */
    public void selectMessageTab() {
        leftPane.setSelectedComponent(assembleTab);
    }

    /**
     * Make the runtime message tab current (up front)
     */
    public void selectRunMessageTab() {
        leftPane.setSelectedComponent(runioTab);
    }

    /**
    * Method to store any program arguments into memory and registers before
    * execution begins. Arguments go into the gap between $sp and kernel memory.
    * Argument pointers and count go into runtime stack and $sp is adjusted accordingly.
    * $a0 gets argument count (argc), $a1 gets stack address of first arg pointer (argv).
    */
    public void processProgramArgumentsIfAny() {
        new ProgramArgumentList(programArguments.getText()).storeProgramArguments();
    }

    /**
     * Method used by the SystemIO class to get interactive user input
     * requested by a running MIPS program (e.g. syscall #5 to read an
     * integer).  SystemIO knows whether simulator is being run at
     * command line by the user, or by the GUI. If run at command line,
     * it gets input from System.in rather than here.
     * <p>
     * This is an overloaded method.  This version, with the String parameter,
     * is used to get input from a popup dialog.
     *
     * @param prompt Prompt to display to the user.
     * @return User input.
     */
    public String getInputString(String prompt) throws CancelException {
        String input;
        boolean lock = Globals.memoryAndRegistersLock.isHeldByCurrentThread();
        if (lock) {
            Globals.memoryAndRegistersLock.unlock();
        }
        JOptionPane pane = new JOptionPane(prompt, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        pane.setWantsInput(true);
        JDialog dialog = pane.createDialog(Globals.getGui(), "Keyboard Input");
        dialog.setVisible(true);
        input = (String) pane.getInputValue();
        if (lock) {
            Globals.memoryAndRegistersLock.lock();
        }
        if (input == JOptionPane.UNINITIALIZED_VALUE) {
            throw new CancelException();
        } else {
            this.postRunMessage(Globals.userInputAlert + input + "\n");
        }
        return input;
    }

    /**
     * Method used by the SystemIO class to get interactive user input
     * requested by a running MIPS program (e.g. syscall #5 to read an
     * integer).  SystemIO knows whether simulator is being run at
     * command line by the user, or by the GUI. If run at command line,
     * it gets input from System.in rather than here.
     * <p>
     * This is an overloaded method.  This version, with the int parameter,
     * is used to get input from the MARS Run I/O window.
     *
     * @param maxLen: maximum length of input. This method returns when maxLen characters have been read. Use -1 for no length restrictions.
     * @return User input.
     */
    public String getInputString(int maxLen) {
        boolean lock = Globals.memoryAndRegistersLock.isHeldByCurrentThread();
        if (lock) {
            Globals.memoryAndRegistersLock.unlock();
        }
        Asker asker = new Asker(maxLen); // Asker defined immediately below.
        String out = asker.response();
        if (lock) {
            Globals.memoryAndRegistersLock.lock();
        }
        return out;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Thread class for obtaining user input in the Run I/O window (MessagesPane)
    // Written by Ricardo Fern�ndez Pascual [rfernandez@ditec.um.es] December 2009.
    class Asker implements Runnable {
        ArrayBlockingQueue<String> resultQueue = new ArrayBlockingQueue<>(1);
        int initialPos;
        int maxLen;

        Asker(int maxLen) {
            this.maxLen = maxLen;
            // initialPos will be set in run()
        }

        final DocumentListener listener =
                new DocumentListener() {
                    public void insertUpdate(final DocumentEvent e) {
                        EventQueue.invokeLater(
                                new Runnable() {
                                    public void run() {
                                        try {
                                            String inserted = e.getDocument().getText(e.getOffset(), e.getLength());
                                            int i = inserted.indexOf('\n');
                                            if (i >= 0) {
                                                int offset = e.getOffset() + i;
                                                if (offset + 1 == e.getDocument().getLength()) {
                                                    returnResponse();
                                                } else {
                                                    // remove the '\n' and put it at the end
                                                    e.getDocument().remove(offset, 1);
                                                    e.getDocument().insertString(e.getDocument().getLength(), "\n", null);
                                                    // insertUpdate will be called again, since we have inserted the '\n' at the end
                                                }
                                            } else if (maxLen >= 0 && e.getDocument().getLength() - initialPos >= maxLen) {
                                                returnResponse();
                                            }
                                        } catch (BadLocationException ex) {
                                            returnResponse();
                                        }
                                    }
                                });
                    }

                    public void removeUpdate(final DocumentEvent e) {
                        EventQueue.invokeLater(
                                new Runnable() {
                                    public void run() {
                                        if ((e.getDocument().getLength() < initialPos || e.getOffset() < initialPos) && e instanceof UndoableEdit) {
                                            ((UndoableEdit) e).undo();
                                            run.setCaretPosition(e.getOffset() + e.getLength());
                                        }
                                    }
                                });
                    }

                    public void changedUpdate(DocumentEvent e) {
                    }
                };
        final NavigationFilter navigationFilter =
                new NavigationFilter() {
                    public void moveDot(FilterBypass fb, int dot, Bias bias) {
                        if (dot < initialPos) {
                            dot = Math.min(initialPos, run.getDocument().getLength());
                        }
                        fb.moveDot(dot, bias);
                    }

                    public void setDot(FilterBypass fb, int dot, Bias bias) {
                        if (dot < initialPos) {
                            dot = Math.min(initialPos, run.getDocument().getLength());
                        }
                        fb.setDot(dot, bias);
                    }
                };
        final Simulator.StopListener stopListener =
                new Simulator.StopListener() {
                    public void stopped(Simulator s) {
                        returnResponse();
                    }
                };

        public void run() { // must be invoked from the GUI thread
            selectRunMessageTab();
            run.setEditable(true);
            run.requestFocusInWindow();
            run.setCaretPosition(run.getDocument().getLength());
            initialPos = run.getCaretPosition();
            run.setNavigationFilter(navigationFilter);
            run.getDocument().addDocumentListener(listener);
            Simulator.getInstance().addStopListener(stopListener);
        }

        void cleanup() { // not required to be called from the GUI thread
            EventQueue.invokeLater(
                    new Runnable() {
                        public void run() {
                            run.getDocument().removeDocumentListener(listener);
                            run.setEditable(false);
                            run.setNavigationFilter(null);
                            run.setCaretPosition(run.getDocument().getLength());
                            Simulator.getInstance().removeStopListener(stopListener);
                        }
                    });
        }

        void returnResponse() {
            try {
                int p = Math.min(initialPos, run.getDocument().getLength());
                int l = Math.min(run.getDocument().getLength() - p, maxLen >= 0 ? maxLen : Integer.MAX_VALUE);
                resultQueue.offer(run.getText(p, l));
            } catch (BadLocationException ex) {
                // this cannot happen
                resultQueue.offer("");
            }
        }

        String response() {
            EventQueue.invokeLater(this);
            try {
                return resultQueue.take();
            } catch (InterruptedException ex) {
                return null;
            } finally {
                cleanup();
            }
        }
    }  // Asker class
    ////////////////////////////////////////////////////////////////////////////
}