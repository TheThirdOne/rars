package rars;

import java.util.ArrayList;
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

/**
 * Maintains list of generated error messages, regardless of source (tokenizing, parsing,
 * assembly, execution).
 *
 * @author Pete Sanderson
 * @version August 2003
 **/

public class ErrorList {
    private ArrayList<ErrorMessage> messages;
    private int errorCount;
    private int warningCount;
    public static final String ERROR_MESSAGE_PREFIX = "Error";
    public static final String WARNING_MESSAGE_PREFIX = "Warning";
    public static final String FILENAME_PREFIX = " in ";
    public static final String LINE_PREFIX = " line ";
    public static final String POSITION_PREFIX = " column ";
    public static final String MESSAGE_SEPARATOR = ": ";


    /**
     * Constructor for ErrorList
     **/

    public ErrorList() {
        messages = new ArrayList<>();
        errorCount = 0;
        warningCount = 0;
    }

    /**
     * Get ArrayList of error messages.
     *
     * @return ArrayList of ErrorMessage objects
     */
    public ArrayList<ErrorMessage> getErrorMessages() {
        return messages;
    }

    /**
     * Determine whether error has occurred or not.
     *
     * @return <tt>true</tt> if an error has occurred (does not include warnings), <tt>false</tt> otherwise.
     **/
    public boolean errorsOccurred() {
        return (errorCount != 0);
    }

    /**
     * Determine whether warning has occurred or not.
     *
     * @return <tt>true</tt> if an warning has occurred, <tt>false</tt> otherwise.
     **/
    public boolean warningsOccurred() {
        return (warningCount != 0);
    }

    /**
     * Add new error message to end of list.
     *
     * @param mess ErrorMessage object to be added to end of error list.
     **/
    public void add(ErrorMessage mess) {
        add(mess, messages.size());
    }

    /**
     * Add new error message at specified index position.
     *
     * @param mess  ErrorMessage object to be added to end of error list.
     * @param index position in error list
     **/
    public void add(ErrorMessage mess, int index) {
        if (errorCount > getErrorLimit()) {
            return;
        }
        if (errorCount == getErrorLimit()) {
            messages.add(new ErrorMessage((RISCVprogram) null, mess.getLine(), mess.getPosition(), "Error Limit of " + getErrorLimit() + " exceeded."));
            errorCount++; // subsequent errors will not be added; see if statement above
            return;
        }
        messages.add(index, mess);
        if (mess.isWarning()) {
            warningCount++;
        } else {
            errorCount++;
        }
    }


    /**
     * Count of number of error messages in list.
     *
     * @return Number of error messages in list.
     **/

    public int errorCount() {
        return this.errorCount;
    }

    /**
     * Count of number of warning messages in list.
     *
     * @return Number of warning messages in list.
     **/

    public int warningCount() {
        return this.warningCount;
    }

    /**
     * Check to see if error limit has been exceeded.
     *
     * @return True if error limit exceeded, false otherwise.
     **/

    public boolean errorLimitExceeded() {
        return this.errorCount > getErrorLimit();
    }

    /**
     * Get limit on number of error messages to be generated
     * by one assemble operation.
     *
     * @return error limit.
     **/

    public int getErrorLimit() {
        return Globals.maximumErrorMessages;
    }

    /**
     * Produce error report.
     *
     * @return String containing report.
     **/
    public String generateErrorReport() {
        return generateReport(ErrorMessage.ERROR);
    }

    /**
     * Produce warning report.
     *
     * @return String containing report.
     **/
    public String generateWarningReport() {
        return generateReport(ErrorMessage.WARNING);
    }

    /**
     * Produce report containing both warnings and errors, warnings first.
     *
     * @return String containing report.
     **/
    public String generateErrorAndWarningReport() {
        return generateWarningReport() + generateErrorReport();
    }

    // Produces either error or warning report.
    private String generateReport(boolean isWarning) {
        StringBuilder report = new StringBuilder("");
        for (ErrorMessage m : messages) {
            if ((isWarning && m.isWarning()) || (!isWarning && !m.isWarning())) {
                report.append(m.generateReport());
            }
        }
        return report.toString();
    }
}  // ErrorList

