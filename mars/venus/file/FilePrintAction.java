package mars.venus.file;

import mars.venus.EditPane;
import mars.venus.GuiAction;
import mars.venus.HardcopyWriter;
import mars.venus.VenusUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
 
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
 * Action  for the File -> Print menu item
 */
public class FilePrintAction extends GuiAction {

    public FilePrintAction(String name, Icon icon, String descrip,
                           Integer mnemonic, KeyStroke accel, VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }

    /**
     * Uses the HardcopyWriter class developed by David Flanagan for the book
     * "Java Examples in a Nutshell".  It will do basic printing of multipage
     * text documents.  It displays a print dialog but does not act on any
     * changes the user may have specified there, such as number of copies.
     *
     * @param e component triggering this call
     */

    public void actionPerformed(ActionEvent e) {
        EditPane editPane = mainUI.getMainPane().getEditPane();
        if (editPane == null) return;
        int fontsize = 10;  // fixed at 10 point
        double margins = .5; // all margins (left,right,top,bottom) fixed at .5"
        HardcopyWriter out;
        try {
            out = new HardcopyWriter(mainUI, editPane.getFilename(),
                    fontsize, margins, margins, margins, margins);
        } catch (HardcopyWriter.PrintCanceledException pce) {
            return;
        }
        BufferedReader in = new BufferedReader(new StringReader(editPane.getSource()));
        int lineNumberDigits = Integer.toString(editPane.getSourceLineCount()).length();
        String line;
        String lineNumberString = "";
        int lineNumber = 0;
        int numchars;
        try {
            line = in.readLine();
            while (line != null) {
                if (editPane.showingLineNumbers()) {
                    lineNumber++;
                    lineNumberString = Integer.toString(lineNumber) + ": ";
                    while (lineNumberString.length() < lineNumberDigits) {
                        lineNumberString = lineNumberString + " ";
                    }
                }
                line = lineNumberString + line + "\n";
                out.write(line.toCharArray(), 0, line.length());
                line = in.readLine();
            }
            in.close();
            out.close();
        } catch (IOException ioe) {
        }
    }
}
