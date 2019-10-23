package rars.assembler;

import rars.RISCVprogram;

/*
Copyright (c) 2003-2013,  Pete Sanderson and Kenneth Vollmar

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
 * Handy class to represent, for a given line of source code, the code
 * itself, the program containing it, and its line number within that program.
 * This is used to separately keep track of the original file/position of
 * a given line of code.  When .include is used, it will migrate to a different
 * line and possibly different program but the migration should not be visible
 * to the user.
 */
public class SourceLine {
    private String source;
    private String filename;
    private RISCVprogram program;
    private int lineNumber;

    /**
     * SourceLine constructor
     *
     * @param source     The source code itself
     * @param program    The program (object representing source file) containing that line
     * @param lineNumber The line number within that program where source appears.
     */
    public SourceLine(String source, RISCVprogram program, int lineNumber) {
        this.source = source;
        this.program = program;
        if (program != null)
            this.filename = program.getFilename();
        this.lineNumber = lineNumber;
    }

    /**
     * Retrieve source statement itself
     *
     * @return Source statement as String
     */
    public String getSource() {
        return source;
    }

    /**
     * Retrieve name of file containing source statement
     *
     * @return File name as String
     */

    public String getFilename() {
        return filename;
    }

    /**
     * Retrieve line number of source statement
     *
     * @return Line number of source statement
     */

    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Retrieve RISCVprogram object containing source statement
     *
     * @return program as RISCVprogram object
     */

    public RISCVprogram getRISCVprogram() {
        return program;
    }
}