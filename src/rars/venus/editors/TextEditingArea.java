package rars.venus.editors;

import javax.swing.text.Document;
import javax.swing.undo.UndoManager;
import java.awt.*;

/**
 * Specifies capabilities that any test editor used in MARS must have.
 */

public interface TextEditingArea {

    // Used by Find/Replace
    public static final int TEXT_NOT_FOUND = 0;
    public static final int TEXT_FOUND = 1;
    public static final int TEXT_REPLACED_FOUND_NEXT = 2;
    public static final int TEXT_REPLACED_NOT_FOUND_NEXT = 3;


    public void copy();

    public void cut();

    public int doFindText(String find, boolean caseSensitive);

    public int doReplace(String find, String replace, boolean caseSensitive);

    public int doReplaceAll(String find, String replace, boolean caseSensitive);

    public int getCaretPosition();

    public Document getDocument();

    public String getSelectedText();

    public int getSelectionEnd();

    public int getSelectionStart();

    public void select(int selectionStart, int selectionEnd);

    public void selectAll();

    public String getText();

    public UndoManager getUndoManager();

    public void paste();

    public void replaceSelection(String str);

    public void setCaretPosition(int position);

    public void setEditable(boolean editable);

    public void setSelectionEnd(int pos);

    public void setSelectionStart(int pos);

    public void setText(String text);

    public void setFont(Font f);

    public Font getFont();

    public boolean requestFocusInWindow();

    public FontMetrics getFontMetrics(Font f);

    public void setBackground(Color c);

    public void setEnabled(boolean enabled);

    public void grabFocus();

    public void redo();

    public void revalidate();

    public void setSourceCode(String code, boolean editable);

    public void setCaretVisible(boolean vis);

    public void setSelectionVisible(boolean vis);

    public void undo();

    public void discardAllUndoableEdits();

    public void setLineHighlightEnabled(boolean highlight);

    public void setCaretBlinkRate(int rate);

    public void setTabSize(int chars);

    public void updateSyntaxStyles();

    public Component getOuterComponent();
}