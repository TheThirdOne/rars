package mars.venus;

import mars.Globals;
import mars.Settings;
import mars.venus.editors.MARSTextEditingArea;
import mars.venus.editors.generic.GenericTextArea;
import mars.venus.editors.jeditsyntax.JEditBasedTextArea;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Observable;
import java.util.Observer;

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
 * Represents one file opened for editing.  Maintains required internal structures.
 * Before Mars 4.0, there was only one editor pane, a tab, and only one file could
 * be open at a time.  With 4.0 came the multifile (pane, tab) editor, and existing
 * duties were split between EditPane and the new EditTabbedPane class.
 *
 * @author Sanderson and Bumgarner
 */

public class EditPane extends JPanel implements Observer {

    private MARSTextEditingArea sourceCode;
    private VenusUI mainUI;
    private String currentDirectoryPath;
    private JLabel caretPositionLabel;
    private JCheckBox showLineNumbers;
    private JLabel lineNumbers;
    private static int count = 0;
    private boolean isCompoundEdit = false;
    private CompoundEdit compoundEdit;
    private FileStatus fileStatus;

    /**
     * Constructor for the EditPane class.
     */

    public EditPane(VenusUI appFrame) {
        super(new BorderLayout());
        this.mainUI = appFrame;
        // user.dir, user's current working directory, is guaranteed to have a value
        currentDirectoryPath = System.getProperty("user.dir");
        // mainUI.editor = new Editor(mainUI);
        // We want to be notified of editor font changes! See update() below.
        Globals.getSettings().addObserver(this);
        this.fileStatus = new FileStatus();
        lineNumbers = new JLabel();

        if (Globals.getSettings().getBooleanSetting(Settings.GENERIC_TEXT_EDITOR)) {
            this.sourceCode = new GenericTextArea(this, lineNumbers);
        } else {
            this.sourceCode = new JEditBasedTextArea(this, lineNumbers);
        }
        // sourceCode is responsible for its own scrolling
        this.add(this.sourceCode.getOuterComponent(), BorderLayout.CENTER);

        // If source code is modified, will set flag to trigger/request file save.
        sourceCode.getDocument().addDocumentListener(
                new DocumentListener() {
                    public void insertUpdate(DocumentEvent evt) {
                        // IF statement added DPS 9-Aug-2011
                        // This method is triggered when file contents added to document
                        // upon opening, even though not edited by user.  The IF
                        // statement will sense this situation and immediately return.
                        if (FileStatus.get() == FileStatus.OPENING) {
                            setFileStatus(FileStatus.NOT_EDITED);
                            FileStatus.set(FileStatus.NOT_EDITED);
                            if (showingLineNumbers()) {
                                lineNumbers.setText(getLineNumbersList(sourceCode.getDocument()));
                            }
                            return;
                        }
                        // End of 9-Aug-2011 modification.
                        if (getFileStatus() == FileStatus.NEW_NOT_EDITED) {
                            setFileStatus(FileStatus.NEW_EDITED);
                        }
                        if (getFileStatus() == FileStatus.NOT_EDITED) {
                            setFileStatus(FileStatus.EDITED);
                        }
                        if (getFileStatus() == FileStatus.NEW_EDITED) {
                            mainUI.editor.setTitle("", getFilename(), getFileStatus());
                        } else {
                            mainUI.editor.setTitle(getPathname(), getFilename(), getFileStatus());
                        }

                        FileStatus.setEdited(true);
                        switch (FileStatus.get()) {
                            case FileStatus.NEW_NOT_EDITED:
                                FileStatus.set(FileStatus.NEW_EDITED);
                                break;
                            case FileStatus.NEW_EDITED:
                                break;
                            default:
                                FileStatus.set(FileStatus.EDITED);
                        }

                        Globals.getGui().getMainPane().getExecutePane().clearPane(); // DPS 9-Aug-2011

                        if (showingLineNumbers()) {
                            lineNumbers.setText(getLineNumbersList(sourceCode.getDocument()));
                        }
                    }

                    public void removeUpdate(DocumentEvent evt) {
                        this.insertUpdate(evt);
                    }

                    public void changedUpdate(DocumentEvent evt) {
                        this.insertUpdate(evt);
                    }
                });

        showLineNumbers = new JCheckBox("Show Line Numbers");
        showLineNumbers.setToolTipText("If checked, will display line number for each line of text.");
        showLineNumbers.setEnabled(false);
        // Show line numbers by default.
        showLineNumbers.setSelected(Globals.getSettings().getEditorLineNumbersDisplayed());

        this.setSourceCode("", false);

        lineNumbers.setFont(getLineNumberFont(sourceCode.getFont()));
        lineNumbers.setVerticalAlignment(JLabel.TOP);
        lineNumbers.setText("");
        lineNumbers.setVisible(true);

        // Listener fires when "Show Line Numbers" check box is clicked.
        showLineNumbers.addItemListener(
                new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        if (showLineNumbers.isSelected()) {
                            lineNumbers.setText(getLineNumbersList(sourceCode.getDocument()));
                            lineNumbers.setVisible(true);
                        } else {
                            lineNumbers.setText("");
                            lineNumbers.setVisible(false);
                        }
                        sourceCode.revalidate(); // added 16 Jan 2012 to assure label redrawn.
                        Globals.getSettings().setEditorLineNumbersDisplayed(showLineNumbers.isSelected());
                        // needed because caret disappears when checkbox clicked
                        sourceCode.setCaretVisible(true);
                        sourceCode.requestFocusInWindow();
                    }
                });

        JPanel editInfo = new JPanel(new BorderLayout());
        caretPositionLabel = new JLabel();
        caretPositionLabel.setToolTipText("Tracks the current position of the text editing cursor.");
        displayCaretPosition(new Point());
        editInfo.add(caretPositionLabel, BorderLayout.WEST);
        editInfo.add(showLineNumbers, BorderLayout.CENTER);
        this.add(editInfo, BorderLayout.SOUTH);
    }


    /**
     * For initalizing the source code when opening an ASM file
     *
     * @param s        String containing text
     * @param editable set true if code is editable else false
     */

    public void setSourceCode(String s, boolean editable) {
        sourceCode.setSourceCode(s, editable);
    }

    /**
     * Get rid of any accumulated undoable edits.  It is useful to call
     * this method after opening a file into the text area.  The
     * act of setting its text content upon reading the file will generate
     * an undoable edit.  Normally you don't want a freshly-opened file
     * to appear with its Undo action enabled.  But it will unless you
     * call this after setting the text.
     */
    public void discardAllUndoableEdits() {
        sourceCode.discardAllUndoableEdits();
    }

    /**
     * Form string with source code line numbers.
     * Resulting string is HTML, for which JLabel will happily honor <br> to do
     * multiline label (it ignores '\n').  The line number list is a JLabel with
     * one line number per line.
     */
    private static final String spaces = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

    public String getLineNumbersList(javax.swing.text.Document doc) {
        StringBuffer lineNumberList = new StringBuffer("<html>");
        int lineCount = doc.getDefaultRootElement().getElementCount(); //this.getSourceLineCount();
        int digits = Integer.toString(lineCount).length();
        for (int i = 1; i <= lineCount; i++) {
            String lineStr = Integer.toString(i);
            int leadingSpaces = digits - lineStr.length();
            if (leadingSpaces == 0) {
                lineNumberList.append(lineStr + "&nbsp;<br>");
            } else {
                lineNumberList.append(spaces.substring(0, leadingSpaces * 6) + lineStr + "&nbsp;<br>");
            }
        }
        lineNumberList.append("<br></html>");
        return lineNumberList.toString();
    }


    /**
     * Calculate and return number of lines in source code text.
     * Do this by counting newline characters then adding one if last line does
     * not end with newline character.
     */

   	/*  IMPLEMENTATION NOTE:
   	 * Tried repeatedly to use StringTokenizer to count lines but got bad results 
   	 * on empty lines (consecutive delimiters) even when returning delimiter as token.
   	 * BufferedReader on StringReader seems to work better.
   	 */
    public int getSourceLineCount() {
        BufferedReader bufStringReader = new BufferedReader(new StringReader(sourceCode.getText()));
        int lineNums = 0;
        try {
            while (bufStringReader.readLine() != null) {
                lineNums++;
            }
        } catch (IOException e) {
        }
        return lineNums;
    }

    /**
     * Get source code text
     *
     * @return Sting containing source code
     */
    public String getSource() {
        return sourceCode.getText();
    }


    /**
     * Set the editing status for this EditPane's associated document.
     * For the argument, use one of the constants from class FileStatus.
     *
     * @param FileStatus the status constant from class FileStatus
     */
    public void setFileStatus(int fileStatus) {
        this.fileStatus.setFileStatus(fileStatus);
    }


    /**
     * Get the editing status for this EditPane's associated document.
     * This will be one of the constants from class FileStatus.
     */

    public int getFileStatus() {
        return this.fileStatus.getFileStatus();
    }

    /**
     * Delegates to corresponding FileStatus method
     */
    public String getFilename() {
        return this.fileStatus.getFilename();
    }


    /**
     * Delegates to corresponding FileStatus method
     */
    public String getPathname() {
        return this.fileStatus.getPathname();
    }


    /**
     * Delegates to corresponding FileStatus method
     */
    public void setPathname(String pathname) {
        this.fileStatus.setPathname(pathname);
    }

    /**
     * Delegates to corresponding FileStatus method
     */
    public boolean hasUnsavedEdits() {
        return this.fileStatus.hasUnsavedEdits();
    }


    /**
     * Delegates to corresponding FileStatus method
     */
    public boolean isNew() {
        return this.fileStatus.isNew();
    }


    /**
     * Delegates to text area's requestFocusInWindow method.
     */

    public void tellEditingComponentToRequestFocusInWindow() {
        this.sourceCode.requestFocusInWindow();
    }


    /**
     * Delegates to corresponding FileStatus method
     */
    public void updateStaticFileStatus() {
        fileStatus.updateStaticFileStatus();
    }


    /**
     * get the manager in charge of Undo and Redo operations
     *
     * @return the UnDo manager
     */
    public UndoManager getUndoManager() {
        return sourceCode.getUndoManager();
    }
   	
      /*       Note: these are invoked only when copy/cut/paste are used from the
   	               toolbar or menu or the defined menu Alt codes.  When
   						Ctrl-C, Ctrl-X or Ctrl-V are used, this code is NOT invoked
   						but the operation works correctly!
   				The "set visible" operations are used because clicking on the toolbar
   				icon causes both the selection highlighting AND the blinking cursor
   				to disappear!  This does not happen when using menu selection or 
   				Ctrl-C/X/V
   	*/

    /**
     * copy currently-selected text into clipboard
     */
    public void copyText() {
        sourceCode.copy();
        sourceCode.setCaretVisible(true);
        sourceCode.setSelectionVisible(true);
    }

    /**
     * cut currently-selected text into clipboard
     */
    public void cutText() {
        sourceCode.cut();
        sourceCode.setCaretVisible(true);
    }

    /**
     * paste clipboard contents at cursor position
     */
    public void pasteText() {
        sourceCode.paste();
        sourceCode.setCaretVisible(true);
    }

    /**
     * select all text
     */
    public void selectAllText() {
        sourceCode.selectAll();
        sourceCode.setCaretVisible(true);
        sourceCode.setSelectionVisible(true);
    }

    /**
     * Undo previous edit
     */
    public void undo() {
        sourceCode.undo();
    }

    /**
     * Redo previous edit
     */
    public void redo() {
        sourceCode.redo();
    }

    /**
     * Update state of Edit menu's Undo menu item.
     */
    public void updateUndoState() {
        mainUI.editUndoAction.updateUndoState();
    }

    /**
     * Update state of Edit menu's Redo menu item.
     */
    public void updateRedoState() {
        mainUI.editRedoAction.updateRedoState();
    }

    /**
     * get editor's line number display status
     *
     * @return true if editor is current displaying line numbers, false otherwise.
     */
    public boolean showingLineNumbers() {
        return showLineNumbers.isSelected();
    }

    /**
     * enable or disable checkbox that controls display of line numbers
     *
     * @param enable True to enable box, false to disable.
     */
    public void setShowLineNumbersEnabled(boolean enabled) {
        showLineNumbers.setEnabled(enabled);
        //showLineNumbers.setSelected(false); // set off, whether closing or opening
    }

    /**
     * Update the caret position label on the editor's border to
     * display the current line and column.  The position is given
     * as text stream offset and will be converted into line and column.
     *
     * @param pos Offset into the text stream of caret.
     */
    public void displayCaretPosition(int pos) {
        displayCaretPosition(convertStreamPositionToLineColumn(pos));
    }

    /**
     * Display cursor coordinates
     *
     * @param p Point object with x-y (column, line number) coordinates of cursor
     */
    public void displayCaretPosition(Point p) {
        caretPositionLabel.setText("Line: " + p.y + " Column: " + p.x);
    }

    /**
     * Given byte stream position in text being edited, calculate its column and line
     * number coordinates.
     *
     * @param stream position of character
     * @return position Its column and line number coordinate as a Point.
     */
    private static final char newline = '\n';

    public Point convertStreamPositionToLineColumn(int position) {
        String textStream = sourceCode.getText();
        int line = 1;
        int column = 1;
        for (int i = 0; i < position; i++) {
            if (textStream.charAt(i) == newline) {
                line++;
                column = 1;
            } else {
                column++;
            }
        }
        return new Point(column, line);
    }

    /**
     * Given line and column (position in the line) numbers, calculate
     * its byte stream position in text being edited.
     *
     * @param line   Line number in file (starts with 1)
     * @param column Position within that line (starts with 1)
     * @return corresponding stream position.  Returns -1 if there is no corresponding position.
     */
    public int convertLineColumnToStreamPosition(int line, int column) {
        String textStream = sourceCode.getText();
        int textLength = textStream.length();
        int textLine = 1;
        int textColumn = 1;
        for (int i = 0; i < textLength; i++) {
            if (textLine == line && textColumn == column) {
                return i;
            }
            if (textStream.charAt(i) == newline) {
                textLine++;
                textColumn = 1;
            } else {
                textColumn++;
            }
        }
        return -1;
    }

    /**
     * Select the specified editor text line.  Lines are numbered starting with 1, consistent
     * with line numbers displayed by the editor.
     *
     * @param line The desired line number of this TextPane's text.  Numbering starts at 1, and
     *             nothing will happen if the parameter value is less than 1
     */
    public void selectLine(int line) {
        if (line > 0) {
            int lineStartPosition = convertLineColumnToStreamPosition(line, 1);
            int lineEndPosition = convertLineColumnToStreamPosition(line + 1, 1) - 1;
            if (lineEndPosition < 0) { // DPS 19 Sept 2012.  Happens if "line" is last line of file.

                lineEndPosition = sourceCode.getText().length() - 1;
            }
            if (lineStartPosition >= 0) {
                sourceCode.select(lineStartPosition, lineEndPosition);
                sourceCode.setSelectionVisible(true);
            }
        }
    }


    /**
     * Select the specified editor text line.  Lines are numbered starting with 1, consistent
     * with line numbers displayed by the editor.
     *
     * @param line   The desired line number of this TextPane's text.  Numbering starts at 1, and
     *               nothing will happen if the parameter value is less than 1
     * @param column Desired column at which to place the cursor.
     */
    public void selectLine(int line, int column) {
        selectLine(line);
        // Made one attempt at setting cursor; didn't work but here's the attempt
        // (imagine using it in the one-parameter overloaded method above)
        //sourceCode.setCaretPosition(lineStartPosition+column-1);
    }

    /**
     * Finds next occurrence of text in a forward search of a string. Search begins
     * at the current cursor location, and wraps around when the end of the string
     * is reached.
     *
     * @param find          the text to locate in the string
     * @param caseSensitive true if search is to be case-sensitive, false otherwise
     * @return TEXT_FOUND or TEXT_NOT_FOUND, depending on the result.
     */
    public int doFindText(String find, boolean caseSensitive) {
        return sourceCode.doFindText(find, caseSensitive);
    }

    /**
     * Finds and replaces next occurrence of text in a string in a forward search.
     * If cursor is initially at end
     * of matching selection, will immediately replace then find and select the
     * next occurrence if any.  Otherwise it performs a find operation.  The replace
     * can be undone with one undo operation.
     *
     * @param find          the text to locate in the string
     * @param replace       the text to replace the find text with - if the find text exists
     * @param caseSensitive true for case sensitive. false to ignore case
     * @return Returns TEXT_FOUND if not initially at end of selected match and matching
     * occurrence is found.  Returns TEXT_NOT_FOUND if the text is not matched.
     * Returns TEXT_REPLACED_NOT_FOUND_NEXT if replacement is successful but there are
     * no additional matches.  Returns TEXT_REPLACED_FOUND_NEXT if reaplacement is
     * successful and there is at least one additional match.
     */
    public int doReplace(String find, String replace, boolean caseSensitive) {
        return sourceCode.doReplace(find, replace, caseSensitive);
    }


    /**
     * Finds and replaces <B>ALL</B> occurrences of text in a string in a forward search.
     * All replacements are bundled into one CompoundEdit, so one Undo operation will
     * undo all of them.
     *
     * @param find          the text to locate in the string
     * @param replace       the text to replace the find text with - if the find text exists
     * @param caseSensitive true for case sensitive. false to ignore case
     * @return the number of occurrences that were matched and replaced.
     */
    public int doReplaceAll(String find, String replace, boolean caseSensitive) {
        return sourceCode.doReplaceAll(find, replace, caseSensitive);
    }


    /**
     * Update, if source code is visible, when Font setting changes.
     * This method is specified by the Observer interface.
     */
    public void update(Observable fontChanger, Object arg) {
        sourceCode.setFont(Globals.getSettings().getEditorFont());
        sourceCode.setLineHighlightEnabled(Globals.getSettings().getBooleanSetting(Settings.EDITOR_CURRENT_LINE_HIGHLIGHTING));
        sourceCode.setCaretBlinkRate(Globals.getSettings().getCaretBlinkRate());
        sourceCode.setTabSize(Globals.getSettings().getEditorTabSize());
        sourceCode.updateSyntaxStyles();
        sourceCode.revalidate();
        // We want line numbers to be displayed same size but always PLAIN style.
        // Easiest way to get same pixel height as source code is to set to same
        // font family as the source code! It can get a bit complicated otherwise
        // because different fonts will render the same font size in different
        // pixel heights.  This is a factor because the line numbers as displayed
        // in the editor form a separate column from the source code and if the
        // pixel height is not the same then the numbers will not line up with
        // the source lines.
        lineNumbers.setFont(getLineNumberFont(sourceCode.getFont()));
        lineNumbers.revalidate();
    }


    /* Private helper method.
     * Determine font to use for editor line number display, given current
     * font for source code.
     */
    private Font getLineNumberFont(Font sourceFont) {
        return (sourceCode.getFont().getStyle() == Font.PLAIN)
                ? sourceFont
                : new Font(sourceFont.getFamily(), Font.PLAIN, sourceFont.getSize());
    }


}