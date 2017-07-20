package mars.venus;

import java.io.File;
 
 /*
Copyright (c) 2003-2007,  Pete Sanderson and Kenneth Vollmar

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
 * Manage the file being edited.
 * Currently only manages one file at a time, but can be expanded.
 */

public class Editor {

    public static final int MIN_TAB_SIZE = 1;
    public static final int MAX_TAB_SIZE = 32;
    public static final int MIN_BLINK_RATE = 0;     // no flashing
    public static final int MAX_BLINK_RATE = 1000;  // once per second

    private VenusUI mainUI;
    private EditTabbedPane editTabbedPane;
    private String mainUIbaseTitle;
    /* number of times File->New has been selected.  Used to generate
     * default filename until first Save or Save As.
     */
    private int newUsageCount;
    // Current Directory for Open operation, same for Save operation
    // Values will mainly be set by the EditTabbedPane as Open/Save operations occur.
    private String defaultOpenDirectory, currentOpenDirectory;
    private String defaultSaveDirectory, currentSaveDirectory;

    /**
     * Create editor.
     *
     * @param ui the GUI that owns this editor
     */
    public Editor(VenusUI ui) {
        mainUI = ui;
        FileStatus.reset();
        mainUIbaseTitle = mainUI.getTitle();
        newUsageCount = 0;
        // Directory from which MARS was launched. Guaranteed to have a value.
        defaultOpenDirectory = System.getProperty("user.dir");
        defaultSaveDirectory = System.getProperty("user.dir");
        currentOpenDirectory = defaultOpenDirectory;
        currentSaveDirectory = defaultSaveDirectory;
    }

    /**
     * Set associated EditTabbedPane.  This is container for any/all open files.
     *
     * @param editTabbedPane an existing editTabbedPane object
     */
    public void setEditTabbedPane(EditTabbedPane editTabbedPane) {
        this.editTabbedPane = editTabbedPane;
    }

    /**
     * Get name of current directory for Open operation.
     *
     * @return String containing directory pathname.  Returns null if there is
     * no EditTabbedPane.  Returns default, directory MARS is launched from, if
     * no Opens have been performed.
     */

    public String getCurrentOpenDirectory() {
        return currentOpenDirectory;
    }

    /**
     * Set name of current directory for Open operation.  The contents of this directory will
     * be displayed when Open dialog is launched.
     *
     * @param currentOpenDirectory String containing pathname for current Open directory. If
     *                             it does not exist or is not a directory, the default (MARS launch directory) will be used.
     */

    void setCurrentOpenDirectory(String currentOpenDirectory) {
        File file = new File(currentOpenDirectory);
        if (!file.exists() || !file.isDirectory()) {
            this.currentOpenDirectory = defaultOpenDirectory;
        } else {
            this.currentOpenDirectory = currentOpenDirectory;
        }
    }


    /**
     * Get name of current directory for Save or Save As operation.
     *
     * @return String containing directory pathname.  Returns null if there is
     * no EditTabbedPane.  Returns default, directory MARS is launched from, if
     * no Save or Save As operations have been performed.
     */

    public String getCurrentSaveDirectory() {
        return currentSaveDirectory;
    }

    /**
     * Set name of current directory for Save operation.  The contents of this directory will
     * be displayed when Save dialog is launched.
     *
     * @param currentSaveDirectory String containing pathname for current Save directory. If
     *                             it does not exist or is not a directory, the default (MARS launch directory) will be used.
     */

    void setCurrentSaveDirectory(String currentSaveDirectory) {
        File file = new File(currentSaveDirectory);
        if (!file.exists() || !file.isDirectory()) {
            this.currentSaveDirectory = defaultSaveDirectory;
        } else {
            this.currentSaveDirectory = currentSaveDirectory;
        }
    }


    /**
     * Generates a default file name
     *
     * @return returns string mipsN.asm, where N is 1,2,3,...
     */
    public String getNextDefaultFilename() {
        newUsageCount++;
        return "riscv" + newUsageCount + ".asm";
    }


    /**
     * Places name of file currently being edited into its edit tab and
     * the application's title bar.  The edit tab will contain only
     * the filename, the title bar will contain full pathname.
     * If file has been modified since created, opened or saved, as
     * indicated by value of the status parameter, the name and path
     * will be followed with an '*'.  If newly-created file has not
     * yet been saved, the title bar will show (temporary) file name
     * but not path.
     *
     * @param path   Full pathname for file
     * @param name   Name of file (last component of path)
     * @param status Edit status of file.  See FileStatus static constants.
     */
    public void setTitle(String path, String name, int status) {
        if (status == FileStatus.NO_FILE || name == null || name.length() == 0) {
            mainUI.setTitle(mainUIbaseTitle);
        } else {
            String edited = (status == FileStatus.NEW_EDITED || status == FileStatus.EDITED) ? "*" : " ";
            String titleName = (status == FileStatus.NEW_EDITED || status == FileStatus.NEW_NOT_EDITED) ? name : path;
            mainUI.setTitle(titleName + edited + " - " + mainUIbaseTitle);
            editTabbedPane.setTitleAt(editTabbedPane.getSelectedIndex(), name + edited);
        }
    }


    /**
     * Perform "new" operation to create an empty tab.
     */
    public void newFile() {
        editTabbedPane.newFile();
    }

    /**
     * Perform "close" operation on current tab's file.
     *
     * @return true if succeeded, else false.
     */
    public boolean close() {
        return editTabbedPane.closeCurrentFile();
    }

    /**
     * Close all currently open files.
     *
     * @return true if succeeded, else false.
     */
    public boolean closeAll() {
        return editTabbedPane.closeAllFiles();
    }

    /**
     * Perform "save" operation on current tab's file.
     *
     * @return true if succeeded, else false.
     */
    public boolean save() {
        return editTabbedPane.saveCurrentFile();
    }

    /**
     * Perform "save as" operation on current tab's file.
     *
     * @return true if succeeded, else false.
     */
    public boolean saveAs() {
        return editTabbedPane.saveAsCurrentFile();
    }

    /**
     * Perform save operation on all open files (tabs).
     *
     * @return true if succeeded, else false.
     */
    public boolean saveAll() {
        return editTabbedPane.saveAllFiles();
    }

    /**
     * Open file in a new tab.
     *
     * @return true if succeeded, else false.
     */
    public boolean open() {
        return editTabbedPane.openFile();
    }


    /**
     * Called by several of the Action objects when there is potential
     * loss of editing changes.  Specifically: if there is a current
     * file open for editing and its modify flag is true, then give user
     * a dialog box with choice to save, discard edits, or cancel and
     * carry out the decision.  This applies to File->New, File->Open,
     * File->Close, and File->Exit.
     *
     * @return false means user selected Cancel so caller should do that.
     * Return of true means caller can proceed (edits were saved or discarded).
     */
    public boolean editsSavedOrAbandoned() {
        return editTabbedPane.editsSavedOrAbandoned();
    }

}