package rars;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*
Copyright (c) 2003-2012,  Pete Sanderson and Kenneth Vollmar

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

import rars.program.AsmRISCVprogram;

/**
 * Represents occurrance of an error detected during tokenizing, assembly or simulation.
 *
 * @author Pete Sanderson
 * @version August 2003
 **/

public class AsmErrorMessage extends ErrorMessage {
    private int line;     // line in source code where error detected
    private int position; // position in source line where error detected
    private String macroExpansionHistory;

    /**
     * Constructor for ErrorMessage.  Assumes line number is calculated after any .include files expanded, and
     * if there were, it will adjust filename and line number so message reflects original file and line number.
     *
     * @param sourceProgram RISCVprogram object of source file in which this error appears.
     * @param line          Line number in source program being processed when error occurred.
     * @param position      Position within line being processed when error occurred.  Normally is starting
     *                      position of source token.
     * @param message       String containing appropriate error message.
     **/

    public AsmErrorMessage(AsmRISCVprogram sourceProgram, int line, int position, String message) {
        this(ERROR, sourceProgram, line, position, message);
    }


    /**
     * Constructor for ErrorMessage.  Assumes line number is calculated after any .include files expanded, and
     * if there were, it will adjust filename and line number so message reflects original file and line number.
     *
     * @param isWarning     set to WARNING if message is a warning not error, else set to ERROR or omit.
     * @param sourceProgram RISCVprogram object of source file in which this error appears.
     * @param line          Line number in source program being processed when error occurred.
     * @param position      Position within line being processed when error occurred.  Normally is starting
     *                      position of source token.
     * @param message       String containing appropriate error message.
     **/

    public AsmErrorMessage(boolean isWarning, AsmRISCVprogram sourceProgram, int line, int position, String message) {
    	super(sourceProgram, message, isWarning);
        if (sourceProgram == null || sourceProgram.getSourceLineList() == null) {
            this.line = line;
        } else {
        	rars.assembler.SourceLine sourceLine = sourceProgram.getSourceLineList().get(line - 1);
        	this.line = sourceLine.getLineNumber();
        }
        this.position = position;
        this.macroExpansionHistory = getExpansionHistory(sourceProgram);
    }

    /**
     * Constructor for ErrorMessage, to be used for runtime exceptions.
     *
     * @param statement The ProgramStatement object for the instruction causing the runtime error
     * @param message   String containing appropriate error message.
     **/
    // Added January 2013
    public AsmErrorMessage(ProgramStatement statement, String message) {
    	super(statement.getSourceProgram(), message, ERROR);
        this.position = 0;
        // Somewhere along the way we lose the macro history, but can
        // normally recreate it here.  The line number for macro use (in the
        // expansion) comes with the ProgramStatement.getSourceLine().
        // The line number for the macro definition comes embedded in
        // the source code from ProgramStatement.getSource(), which is
        // displayed in the Text Segment display.  It would previously
        // have had the macro definition line prepended in brackets,
        // e.g. "<13>  syscall  # finished".  So I'll extract that
        // bracketed number here and include it in the error message.
        // Looks bass-ackwards, but to get the line numbers to display correctly
        // for runtime error occurring in macro expansion (expansion->definition), need
        // to assign to the opposite variables.
        ArrayList<Integer> defineLine = parseMacroHistory(statement.getSource());
        if (defineLine.size() == 0) {
            this.line = statement.getSourceLine();
            this.macroExpansionHistory = "";
        } else {
            this.line = defineLine.get(0);
            this.macroExpansionHistory = "" + statement.getSourceLine();
        }
    }

    private ArrayList<Integer> parseMacroHistory(String string) {
        Pattern pattern = Pattern.compile("<\\d+>");
        Matcher matcher = pattern.matcher(string);
        String verify = new String(string).trim();
        ArrayList<Integer> macroHistory = new ArrayList<>();
        while (matcher.find()) {
            String match = matcher.group();
            if (verify.indexOf(match) == 0) {
                try {
                    int line = Integer.parseInt(match.substring(1, match.length() - 1));
                    macroHistory.add(line);
                } catch (NumberFormatException e) {
                    break;
                }
                verify = verify.substring(match.length()).trim();
            } else {
                break;
            }
        }
        return macroHistory;
    }

    /**
     * Produce line number of error.
     *
     * @return Returns line number in source program where error occurred.
     */

    public int getLine() {
        return line;
    }

    /**
     * Produce position within erroneous line.
     *
     * @return Returns position within line of source program where error occurred.
     */

    public int getPosition() {
        return position;
    }

    @Override
    public String generateReport() {
        String out = ((super.isWarning()) ? ErrorList.WARNING_MESSAGE_PREFIX : ErrorList.ERROR_MESSAGE_PREFIX) + ErrorList.FILENAME_PREFIX;
        if (getFilename().length() > 0)
            out = out + (new File(getFilename()).getPath()); //.getName());
        if (getLine() > 0)
            out = out + ErrorList.LINE_PREFIX + getMacroExpansionHistory() + getLine();
        if (getPosition() > 0)
            out = out + ErrorList.POSITION_PREFIX + getPosition();
        out = out + ErrorList.MESSAGE_SEPARATOR + getMessage() + "\n";
        return out;
    }

    /**
     * Returns string describing macro expansion.  Empty string if none.
     *
     * @return string describing macro expansion
     */
    // Method added by Mohammad Sekavat Dec 2012
    public String getMacroExpansionHistory() {
        if (macroExpansionHistory == null || macroExpansionHistory.length() == 0)
            return "";
        return macroExpansionHistory + "->";
    }

    // Added by Mohammad Sekavat Dec 2012
    private static String getExpansionHistory(AsmRISCVprogram sourceProgram) {
        if (sourceProgram == null || sourceProgram.getLocalMacroPool() == null)
            return "";
        return sourceProgram.getLocalMacroPool().getExpansionHistory();
    }

}  // ErrorMessage