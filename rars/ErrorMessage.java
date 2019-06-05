package rars;

import java.io.File;

import rars.program.RISCVprogram;

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

public class ErrorMessage {
    private boolean isWarning; // allow for warnings too (added Nov 2006)
    private String filename; // name of source file  (added Oct 2006)
    private String message;
    
    /**
     * Constant to indicate this message is warning not error
     */
    public static final boolean WARNING = true;

    /**
     * Constant to indicate this message is error not warning
     */
    public static final boolean ERROR = false;
    
    public ErrorMessage(RISCVprogram source, String message) {
    	this(source, message, ERROR);
    }
    
    public ErrorMessage(RISCVprogram source, String message, boolean isWarning) {
    	if(source == null)
    		this.filename = "";
    	else
    		this.filename = source.getFilename();
    	this.message = message;
    	this.isWarning = isWarning;
    }
    
    /**
     * Produce name of file containing error.
     *
     * @return Returns String containing name of source file containing the error.
     */
    // Added October 2006
    public String getFilename() {
        return filename;
    }
    
    /**
     * Produce error message.
     *
     * @return Returns String containing textual error message.
     */

    public String getMessage() {
        return message;
    }

    /**
     * Determine whether this message represents error or warning.
     *
     * @return Returns true if this message reflects warning, false if error.
     */
    // Method added 28 Nov 2006
    public boolean isWarning() {
        return this.isWarning;
    }
    
    /**
     * Returns the default prefix which includes a warning/error prefix and the source file
     * 
     * @return Returns the prefix
     */
    protected String getDefaultPrefix() {
        String out = ((isWarning) ? ErrorList.WARNING_MESSAGE_PREFIX : ErrorList.ERROR_MESSAGE_PREFIX) + ErrorList.FILENAME_PREFIX;
        if (getFilename().length() > 0)
            out = out + (new File(getFilename()).getPath()); //.getName());
        return out;
    }
    
    /**
     * Returns the default suffix which includes a message seperator followed by the error message
     * followed by a newline.
     * 
     * @return Returns the suffix
     */
    protected String getDefaultSuffix() {
    	return ErrorList.MESSAGE_SEPARATOR + getMessage() + "\n";
    }
    
    /**
     * Generates an error message
     * 
     * @return Returns the generated message
     */
	public String generateReport() {
		return this.getDefaultPrefix() + this.getDefaultSuffix();
	}
}
