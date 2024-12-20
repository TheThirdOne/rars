package rars.assembler;

import rars.RISCVprogram;

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