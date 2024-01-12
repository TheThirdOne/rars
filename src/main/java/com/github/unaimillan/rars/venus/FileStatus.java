package com.github.unaimillan.rars.venus;

import com.github.unaimillan.rars.Globals;

import java.io.File;

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
 * Used to store and return information on the status of the current ASM file that
 * is being edited in the program.
 *
 * @author Team JSpim
 **/

public class FileStatus {
    /**
     * initial state or after close
     */
    public static final int NO_FILE = 0;
    /**
     * New edit window with no edits
     */
    public static final int NEW_NOT_EDITED = 1;
    /**
     * New edit window with unsaved edits
     */
    public static final int NEW_EDITED = 2;
    /**
     * open/saved edit window with no edits
     */
    public static final int NOT_EDITED = 3;
    /**
     * open/saved edit window with unsaved edits
     */
    public static final int EDITED = 4;
    /**
     * successful assembly
     */
    public static final int RUNNABLE = 5;
    /**
     * execution is under way
     */
    public static final int RUNNING = 6;
    /**
     * execution terminated
     */
    public static final int TERMINATED = 7;
    /**
     * file is being opened.  DPS 9-Aug-2011
     */
    public static final int OPENING = 8;


    ///////////////////////////////////////////////////////////////////
    //                                                               //
    //  The static part.  Legacy code from original student team's   //
    //  2003 Practicum project through MARS 3.8, when the editor     //
    //  was limited to one file.  The status of that file became     //
    //  the de facto status of the system.  Should have used a       //
    //  singleton class but in 2003 did not know what that was!      //
    //  My plan is to phase out all statics but the constants        //
    //  in MARS 4.0 but will keep it in place while at the same time //
    //  defining non-static members for use by individual files      //
    //  currently opened in the editor.  DPS, 9 April 2010.          //
    //                                                               //
    ///////////////////////////////////////////////////////////////////

    private static int systemStatus; // set to one of the above
    private static boolean systemAssembled;
    private static boolean systemSaved;
    private static boolean systemEdited;
    private static String systemName;
    private static File systemFile;

    /**
     * Set file status.  Also updates menu state accordingly.
     *
     * @param newStatus New status: EDITED, RUNNABLE, etc, see list above.
     */
    public static void set(int newStatus) {
        systemStatus = newStatus;
        Globals.getGui().setMenuState(systemStatus);
    }

    /**
     * Get file status
     *
     * @return file status EDITED, RUNNABLE, etc, see list above
     */
    public static int get() {
        return systemStatus;
    }

    /**
     * Changes the value of assenbked to the parameter given.
     *
     * @param b boolean variable that tells what to set assembled to.
     */
    public static void setAssembled(boolean b) {
        systemAssembled = b;
    }

    /**
     * Changes the value of saved to the parameter given.
     *
     * @param b boolean variable that tells what to set saved to .
     */
    public static void setSaved(boolean b) {
        systemSaved = b;
    }

    /**
     * Changes the value of edited to the parameter given.
     *
     * @param b boolean variable that tells what to set edited to.
     */
    public static void setEdited(boolean b) {
        systemEdited = b;
    }

    /**
     * Changes the value of name to the parameter given.
     *
     * @param s string variable tells what to set the name of the file to .
     */
    public static void setName(String s) {
        systemName = s;
    }

    /**
     * Sets the file to the ASM file passed.
     *
     * @param f file object variable that stores the ASM file.
     */
    public static void setFile(File f) {
        systemFile = f;
    }

    /**
     * Returns the ASM file.
     *
     * @return The ASM file.
     */
    public static File getFile() {
        return systemFile;
    }

    /**
     * Returns the name of the file.
     *
     * @return The name of the ASM file.
     */
    public static String getName() {
        return systemName;
    }

    /**
     * Tells whether the file has been assembled.
     *
     * @return Boolean value that is true if the ASM file has been assembled.
     */
    public static boolean isAssembled() {
        return systemAssembled;
    }

    /**
     * Tells whether the file has been saved.
     *
     * @return Boolean variable that is true if the ASM file has been saved
     */
    public static boolean isSaved() {
        return systemSaved;
    }

    /**
     * Tells whether the file has been edited since it has been saved.
     *
     * @return Boolean value that returns true if the ASM file has been edited.
     */
    public static boolean isEdited() {
        return systemEdited;
    }

    /**
     * Resets all the values in FileStatus
     */
    public static void reset() {
        systemStatus = NO_FILE;
        systemName = "";
        systemAssembled = false;
        systemSaved = false;
        systemEdited = false;
        systemFile = null;
    }

    /////////////////////  END OF STATIC PART   ///////////////////////
    ///////////////////////////////////////////////////////////////////


    // Remaining members are of instantiable class that can be used by
    // every file that is currently open in the editor.


    private int status;
    private File file;

    /**
     * Create a FileStatus object with FileStatis.NO_FILE for status and null for file getters.
     */
    public FileStatus() {
        this(FileStatus.NO_FILE, null);
    }

    /**
     * Create a FileStatus object with given status and file pathname.
     *
     * @param status   Initial file status.  See FileStatus static constants.
     * @param pathname Full file pathname. See setPathname(String newPath) below.
     */
    public FileStatus(int status, String pathname) {
        this.status = status;
        if (pathname == null) {
            this.file = null;
        } else {
            setPathname(pathname);
        }
    }

    /**
     * Set editing status of this file.  See FileStatus static constants.
     *
     * @param newStatus the new status
     */
    public void setFileStatus(int newStatus) {
        this.status = newStatus;
    }

    /**
     * Get editing status of this file.
     *
     * @return current editing status.  See FileStatus static constants.
     */
    public int getFileStatus() {
        return this.status;
    }

    /**
     * Determine if file is "new", which means created using New but not yet saved.
     * If created using Open, it is not new.
     *
     * @return true if file was created using New and has not yet been saved, false otherwise.
     */
    public boolean isNew() {
        return status == FileStatus.NEW_NOT_EDITED || status == FileStatus.NEW_EDITED;
    }

    /**
     * Determine if file has been modified since last save or, if not yet saved, since
     * being created using New or Open.
     *
     * @return true if file has been modified since save or creation, false otherwise.
     */
    public boolean hasUnsavedEdits() {
        return status == FileStatus.NEW_EDITED || status == FileStatus.EDITED;
    }

    /**
     * Set full file pathname. See java.io.File(String pathname) for parameter specs.
     *
     * @param newPath the new pathname. If no directory path, getParent() will return null.
     */
    public void setPathname(String newPath) {
        this.file = new File(newPath);
    }

    /**
     * Set full file pathname. See java.io.File(String parent, String child) for parameter specs.
     *
     * @param parent the parent directory of the file.  If null, getParent() will return null.
     * @param name   the name of the file (no directory path)
     */
    public void setPathname(String parent, String name) {
        this.file = new File(parent, name);
    }

    /**
     * Get full file pathname.  See java.io.File.getPath()
     *
     * @return full pathname as a String.  Null if
     */
    public String getPathname() {
        return (this.file == null) ? null : this.file.getPath();
    }

    /**
     * Get file name with no path information.  See java.io.File.getName()
     *
     * @return filename as a String
     */
    public String getFilename() {
        return (this.file == null) ? null : this.file.getName();
    }

    /**
     * Get file parent pathname.  See java.io.File.getParent()
     *
     * @return parent full pathname as a String
     */
    public String getParent() {
        return (this.file == null) ? null : this.file.getParent();
    }


    /**
     * Update static FileStatus fields with values from this FileStatus object
     * To support legacy code that depends on the static.
     */

    public void updateStaticFileStatus() {
        systemStatus = this.status;
        systemName = this.file.getPath();
        systemAssembled = false;
        systemSaved = (status == NOT_EDITED || status == RUNNABLE || status == RUNNING || status == TERMINATED);
        systemEdited = (status == NEW_EDITED || status == EDITED);
        systemFile = this.file;

    }

}