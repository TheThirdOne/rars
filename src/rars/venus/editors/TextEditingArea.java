package rars.venus.editors;

import javax.swing.text.Document;
import javax.swing.undo.UndoManager;
import java.awt.*;

/*
Copyright (c) 2003-2010,  Pete Sanderson and Kenneth Vollmar

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