package mars.venus;

import mars.Globals;
import mars.Settings;
import mars.mips.dump.DumpFormatLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

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
 * Top level container for Venus GUI.
 *
 * @author Sanderson and Team JSpim
 **/

	  /* Heavily modified by Pete Sanderson, July 2004, to incorporate JSPIMMenu and JSPIMToolbar
       * not as subclasses of JMenuBar and JToolBar, but as instances of them.  They are both
		* here primarily so both can share the Action objects.
		*/

public class VenusUI extends JFrame {
    VenusUI mainUI;
    public JMenuBar menu;
    JToolBar toolbar;
    MainPane mainPane;
    RegistersPane registersPane;
    RegistersWindow registersTab;
    Coprocessor1Window coprocessor1Tab;
    Coprocessor0Window coprocessor0Tab;
    MessagesPane messagesPane;
    JSplitPane splitter, horizonSplitter;
    JPanel north;

    private int frameState; // see windowActivated() and windowDeactivated()
    private static int menuState = FileStatus.NO_FILE;

    // PLEASE PUT THESE TWO (& THEIR METHODS) SOMEWHERE THEY BELONG, NOT HERE
    private boolean reset = true; // registers/memory reset for execution
    private boolean started = false;  // started execution
    Editor editor;

    // components of the menubar
    private JMenu file, run, window, help, edit, settings;
    private JMenuItem fileNew, fileOpen, fileClose, fileCloseAll, fileSave, fileSaveAs, fileSaveAll, fileDumpMemory, filePrint, fileExit;
    private JMenuItem editUndo, editRedo, editCut, editCopy, editPaste, editFindReplace, editSelectAll;
    private JMenuItem runGo, runStep, runBackstep, runReset, runAssemble, runStop, runPause, runClearBreakpoints, runToggleBreakpoints;
    private JCheckBoxMenuItem settingsLabel, settingsPopupInput, settingsValueDisplayBase, settingsAddressDisplayBase,
            settingsExtended, settingsAssembleOnOpen, settingsAssembleAll, settingsWarningsAreErrors, settingsStartAtMain,
            settingsDelayedBranching, settingsProgramArguments, settingsSelfModifyingCode;
    private JMenuItem settingsExceptionHandler, settingsEditor, settingsHighlighting, settingsMemoryConfiguration;
    private JMenuItem helpHelp, helpAbout;

    // components of the toolbar
    private JButton Undo, Redo, Cut, Copy, Paste, FindReplace, SelectAll;
    private JButton New, Open, Save, SaveAs, SaveAll, DumpMemory, Print;
    private JButton Run, Assemble, Reset, Step, Backstep, Stop, Pause;
    private JButton Help;

    // The "action" objects, which include action listeners.  One of each will be created then
    // shared between a menu item and its corresponding toolbar button.  This is a very cool
    // technique because it relates the button and menu item so closely

    private Action fileNewAction, fileOpenAction, fileCloseAction, fileCloseAllAction, fileSaveAction;
    private Action fileSaveAsAction, fileSaveAllAction, fileDumpMemoryAction, filePrintAction, fileExitAction;
    EditUndoAction editUndoAction;
    EditRedoAction editRedoAction;
    private Action editCutAction, editCopyAction, editPasteAction, editFindReplaceAction, editSelectAllAction;
    private Action runAssembleAction, runGoAction, runStepAction, runBackstepAction, runResetAction,
            runStopAction, runPauseAction, runClearBreakpointsAction, runToggleBreakpointsAction;
    private Action settingsLabelAction, settingsPopupInputAction, settingsValueDisplayBaseAction, settingsAddressDisplayBaseAction,
            settingsExtendedAction, settingsAssembleOnOpenAction, settingsAssembleAllAction,
            settingsWarningsAreErrorsAction, settingsStartAtMainAction, settingsProgramArgumentsAction,
            settingsDelayedBranchingAction, settingsExceptionHandlerAction, settingsEditorAction,
            settingsHighlightingAction, settingsMemoryConfigurationAction, settingsSelfModifyingCodeAction;
    private Action helpHelpAction, helpAboutAction;


    /**
     * Constructor for the Class. Sets up a window object for the UI
     *
     * @param s Name of the window to be created.
     **/

    public VenusUI(String s) {
        super(s);
        mainUI = this;
        Globals.setGui(this);
        this.editor = new Editor(this);

        double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        // basically give up some screen space if running at 800 x 600
        double messageWidthPct = (screenWidth < 1000.0) ? 0.67 : 0.73;
        double messageHeightPct = (screenWidth < 1000.0) ? 0.12 : 0.15;
        double mainWidthPct = (screenWidth < 1000.0) ? 0.67 : 0.73;
        double mainHeightPct = (screenWidth < 1000.0) ? 0.60 : 0.65;
        double registersWidthPct = (screenWidth < 1000.0) ? 0.18 : 0.22;
        double registersHeightPct = (screenWidth < 1000.0) ? 0.72 : 0.80;

        Dimension messagesPanePreferredSize = new Dimension((int) (screenWidth * messageWidthPct), (int) (screenHeight * messageHeightPct));
        Dimension mainPanePreferredSize = new Dimension((int) (screenWidth * mainWidthPct), (int) (screenHeight * mainHeightPct));
        Dimension registersPanePreferredSize = new Dimension((int) (screenWidth * registersWidthPct), (int) (screenHeight * registersHeightPct));

        // the "restore" size (window control button that toggles with maximize)
        // I want to keep it large, with enough room for user to get handles
        //this.setSize((int)(screenWidth*.8),(int)(screenHeight*.8));

        Globals.initialize(true);

        //  image courtesy of NASA/JPL.
        URL im = this.getClass().getResource(Globals.imagesPath + "RedMars16.gif");
        if (im == null) {
            System.out.println("Internal Error: images folder or file not found");
            System.exit(0);
        }
        Image mars = Toolkit.getDefaultToolkit().getImage(im);
        this.setIconImage(mars);
        // Everything in frame will be arranged on JPanel "center", which is only frame component.
        // "center" has BorderLayout and 2 major components:
        //   -- panel (jp) on North with 2 components
        //      1. toolbar
        //      2. run speed slider.
        //   -- split pane (horizonSplitter) in center with 2 components side-by-side
        //      1. split pane (splitter) with 2 components stacked
        //         a. main pane, with 2 tabs (edit, execute)
        //         b. messages pane with 2 tabs (mars, run I/O)
        //      2. registers pane with 3 tabs (register file, coproc 0, coproc 1)
        // I should probably run this breakdown out to full detail.  The components are created
        // roughly in bottom-up order; some are created in component constructors and thus are
        // not visible here.

        registersTab = new RegistersWindow();
        coprocessor1Tab = new Coprocessor1Window();
        coprocessor0Tab = new Coprocessor0Window();
        registersPane = new RegistersPane(mainUI, registersTab, coprocessor1Tab, coprocessor0Tab);
        registersPane.setPreferredSize(registersPanePreferredSize);

        //Insets defaultTabInsets = (Insets)UIManager.get("TabbedPane.tabInsets");
        //UIManager.put("TabbedPane.tabInsets", new Insets(1, 1, 1, 1));
        mainPane = new MainPane(mainUI, editor, registersTab, coprocessor1Tab, coprocessor0Tab);
        //UIManager.put("TabbedPane.tabInsets", defaultTabInsets);

        mainPane.setPreferredSize(mainPanePreferredSize);
        messagesPane = new MessagesPane();
        messagesPane.setPreferredSize(messagesPanePreferredSize);
        splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainPane, messagesPane);
        splitter.setOneTouchExpandable(true);
        splitter.resetToPreferredSizes();
        horizonSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitter, registersPane);
        horizonSplitter.setOneTouchExpandable(true);
        horizonSplitter.resetToPreferredSizes();

        // due to dependencies, do not set up menu/toolbar until now.
        this.createActionObjects();
        menu = this.setUpMenuBar();
        this.setJMenuBar(menu);

        toolbar = this.setUpToolBar();

        JPanel jp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jp.add(toolbar);
        jp.add(RunSpeedPanel.getInstance());
        JPanel center = new JPanel(new BorderLayout());
        center.add(jp, BorderLayout.NORTH);
        center.add(horizonSplitter);


        this.getContentPane().add(center);

        FileStatus.reset();
        // The following has side effect of establishing menu state
        FileStatus.set(FileStatus.NO_FILE);

        // This is invoked when opening the app.  It will set the app to
        // appear at full screen size.
        this.addWindowListener(
                new WindowAdapter() {
                    public void windowOpened(WindowEvent e) {
                        mainUI.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    }
                });

        // This is invoked when exiting the app through the X icon.  It will in turn
        // check for unsaved edits before exiting.
        this.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        if (mainUI.editor.closeAll()) {
                            System.exit(0);
                        }
                    }
                });

        // The following will handle the windowClosing event properly in the
        // situation where user Cancels out of "save edits?" dialog.  By default,
        // the GUI frame will be hidden but I want it to do nothing.
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        this.pack();
        this.setVisible(true);
    }


    /*
     * Action objects are used instead of action listeners because one can be easily shared between
     * a menu item and a toolbar button.  Does nice things like disable both if the action is
     * disabled, etc.
     */
    private void createActionObjects() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Class cs = this.getClass();
        try {
            fileNewAction = new FileNewAction("New",
                    new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "New22.png"))),
                    "Create a new file for editing", KeyEvent.VK_N,
                    KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                    mainUI);
            fileOpenAction = new FileOpenAction("Open ...",
                    new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Open22.png"))),
                    "Open a file for editing", KeyEvent.VK_O,
                    KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                    mainUI);
            fileCloseAction = new FileCloseAction("Close", null,
                    "Close the current file", KeyEvent.VK_C,
                    KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                    mainUI);
            fileCloseAllAction = new FileCloseAllAction("Close All", null,
                    "Close all open files", KeyEvent.VK_L,
                    null, mainUI);
            fileSaveAction = new FileSaveAction("Save",
                    new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Save22.png"))),
                    "Save the current file", KeyEvent.VK_S,
                    KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                    mainUI);
            fileSaveAsAction = new FileSaveAsAction("Save as ...",
                    new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "SaveAs22.png"))),
                    "Save current file with different name", KeyEvent.VK_A,
                    null, mainUI);
            fileSaveAllAction = new FileSaveAllAction("Save All", null,
                    "Save all open files", KeyEvent.VK_V,
                    null, mainUI);
            fileDumpMemoryAction = new FileDumpMemoryAction("Dump Memory ...",
                    new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Dump22.png"))),
                    "Dump machine code or data in an available format", KeyEvent.VK_D,
                    KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                    mainUI);
            filePrintAction = new FilePrintAction("Print ...",
                    new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Print22.gif"))),
                    "Print current file", KeyEvent.VK_P,
                    null, mainUI);
            fileExitAction = new FileExitAction("Exit", null,
                    "Exit Mars", KeyEvent.VK_X,
                    null, mainUI);
            editUndoAction = new EditUndoAction("Undo",
                    new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Undo22.png"))),
                    "Undo last edit", KeyEvent.VK_U,
                    KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                    mainUI);
            editRedoAction = new EditRedoAction("Redo",
                    new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Redo22.png"))),
                    "Redo last edit", KeyEvent.VK_R,
                    KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                    mainUI);
            editCutAction = new EditCutAction("Cut",
                    new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Cut22.gif"))),
                    "Cut", KeyEvent.VK_C,
                    KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                    mainUI);
            editCopyAction = new EditCopyAction("Copy",
                    new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Copy22.png"))),
                    "Copy", KeyEvent.VK_O,
                    KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                    mainUI);
            editPasteAction = new EditPasteAction("Paste",
                    new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Paste22.png"))),
                    "Paste", KeyEvent.VK_P,
                    KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                    mainUI);
            editFindReplaceAction = new EditFindReplaceAction("Find/Replace",
                    new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Find22.png"))),
                    "Find/Replace", KeyEvent.VK_F,
                    KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                    mainUI);
            editSelectAllAction = new EditSelectAllAction("Select All",
                    null, //new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath+"Find22.png"))),
                    "Select All", KeyEvent.VK_A,
                    KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                    mainUI);
            runAssembleAction = new RunAssembleAction("Assemble",
                    new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Assemble22.png"))),
                    "Assemble the current file and clear breakpoints", KeyEvent.VK_A,
                    KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0),
                    mainUI);
            runGoAction = new RunGoAction("Go",
                    new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Play22.png"))),
                    "Run the current program", KeyEvent.VK_G,
                    KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
                    mainUI);
            runStepAction = new RunStepAction("Step",
                    new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "StepForward22.png"))),
                    "Run one step at a time", KeyEvent.VK_T,
                    KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0),
                    mainUI);
            runBackstepAction = new RunBackstepAction("Backstep",
                    new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "StepBack22.png"))),
                    "Undo the last step", KeyEvent.VK_B,
                    KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0),
                    mainUI);
            runPauseAction = new RunPauseAction("Pause",
                    new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Pause22.png"))),
                    "Pause the currently running program", KeyEvent.VK_P,
                    KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0),
                    mainUI);
            runStopAction = new RunStopAction("Stop",
                    new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Stop22.png"))),
                    "Stop the currently running program", KeyEvent.VK_S,
                    KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0),
                    mainUI);
            runResetAction = new RunResetAction("Reset",
                    new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Reset22.png"))),
                    "Reset MIPS memory and registers", KeyEvent.VK_R,
                    KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0),
                    mainUI);
            runClearBreakpointsAction = new RunClearBreakpointsAction("Clear all breakpoints",
                    null,
                    "Clears all execution breakpoints set since the last assemble.",
                    KeyEvent.VK_K,
                    KeyStroke.getKeyStroke(KeyEvent.VK_K, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                    mainUI);
            runToggleBreakpointsAction = new RunToggleBreakpointsAction("Toggle all breakpoints",
                    null,
                    "Disable/enable all breakpoints without clearing (can also click Bkpt column header)",
                    KeyEvent.VK_T,
                    KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                    mainUI);
            settingsLabelAction = new SettingsLabelAction("Show Labels Window (symbol table)",
                    null,
                    "Toggle visibility of Labels window (symbol table) in the Execute tab",
                    null, null,
                    mainUI);
            settingsPopupInputAction = new SettingsPopupInputAction("Popup dialog for input syscalls (5,6,7,8,12)",
                    null,
                    "If set, use popup dialog for input syscalls (5,6,7,8,12) instead of cursor in Run I/O window",
                    null, null,
                    mainUI);

            settingsValueDisplayBaseAction = new SettingsValueDisplayBaseAction("Values displayed in hexadecimal",
                    null,
                    "Toggle between hexadecimal and decimal display of memory/register values",
                    null, null,
                    mainUI);
            settingsAddressDisplayBaseAction = new SettingsAddressDisplayBaseAction("Addresses displayed in hexadecimal",
                    null,
                    "Toggle between hexadecimal and decimal display of memory addresses",
                    null, null,
                    mainUI);
            settingsExtendedAction = new SettingsExtendedAction("Permit extended (pseudo) instructions and formats",
                    null,
                    "If set, MIPS extended (pseudo) instructions are formats are permitted.",
                    null, null,
                    mainUI);
            settingsAssembleOnOpenAction = new SettingsAssembleOnOpenAction("Assemble file upon opening",
                    null,
                    "If set, a file will be automatically assembled as soon as it is opened.  File Open dialog will show most recently opened file.",
                    null, null,
                    mainUI);
            settingsAssembleAllAction = new SettingsAssembleAllAction("Assemble all files in directory",
                    null,
                    "If set, all files in current directory will be assembled when Assemble operation is selected.",
                    null, null,
                    mainUI);
            settingsWarningsAreErrorsAction = new SettingsWarningsAreErrorsAction("Assembler warnings are considered errors",
                    null,
                    "If set, assembler warnings will be interpreted as errors and prevent successful assembly.",
                    null, null,
                    mainUI);
            settingsStartAtMainAction = new SettingsStartAtMainAction("Initialize Program Counter to global 'main' if defined",
                    null,
                    "If set, assembler will initialize Program Counter to text address globally labeled 'main', if defined.",
                    null, null,
                    mainUI);
            settingsProgramArgumentsAction = new SettingsProgramArgumentsAction("Program arguments provided to MIPS program",
                    null,
                    "If set, program arguments for MIPS program can be entered in border of Text Segment window.",
                    null, null,
                    mainUI);
            settingsDelayedBranchingAction = new SettingsDelayedBranchingAction("Delayed branching",
                    null,
                    "If set, delayed branching will occur during MIPS execution.",
                    null, null,
                    mainUI);
            settingsSelfModifyingCodeAction = new SettingsSelfModifyingCodeAction("Self-modifying code",
                    null,
                    "If set, the MIPS program can write and branch to both text and data segments.",
                    null, null,
                    mainUI);
            settingsEditorAction = new SettingsEditorAction("Editor...",
                    null,
                    "View and modify text editor settings.",
                    null, null,
                    mainUI);
            settingsHighlightingAction = new SettingsHighlightingAction("Highlighting...",
                    null,
                    "View and modify Execute Tab highlighting colors",
                    null, null,
                    mainUI);
            settingsExceptionHandlerAction = new SettingsExceptionHandlerAction("Exception Handler...",
                    null,
                    "If set, the specified exception handler file will be included in all Assemble operations.",
                    null, null,
                    mainUI);
            settingsMemoryConfigurationAction = new SettingsMemoryConfigurationAction("Memory Configuration...",
                    null,
                    "View and modify memory segment base addresses for simulated MIPS.",
                    null, null,
                    mainUI);
            helpHelpAction = new HelpHelpAction("Help",
                    new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Help22.png"))),
                    "Help", KeyEvent.VK_H,
                    KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
                    mainUI);
            helpAboutAction = new HelpAboutAction("About ...", null,
                    "Information about Mars", null, null, mainUI);
        } catch (NullPointerException e) {
            System.out.println("Internal Error: images folder not found, or other null pointer exception while creating Action objects");
            e.printStackTrace();
            System.exit(0);
        }
    }
   
    /*
     * build the menus and connect them to action objects (which serve as action listeners
     * shared between menu item and corresponding toolbar icon).
     */

    private JMenuBar setUpMenuBar() {

        Toolkit tk = Toolkit.getDefaultToolkit();
        Class cs = this.getClass();
        JMenuBar menuBar = new JMenuBar();
        file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        edit = new JMenu("Edit");
        edit.setMnemonic(KeyEvent.VK_E);
        run = new JMenu("Run");
        run.setMnemonic(KeyEvent.VK_R);
        //window = new JMenu("Window");
        //window.setMnemonic(KeyEvent.VK_W);
        settings = new JMenu("Settings");
        settings.setMnemonic(KeyEvent.VK_S);
        help = new JMenu("Help");
        help.setMnemonic(KeyEvent.VK_H);
        // slight bug: user typing alt-H activates help menu item directly, not help menu

        fileNew = new JMenuItem(fileNewAction);
        fileNew.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "New16.png"))));
        fileOpen = new JMenuItem(fileOpenAction);
        fileOpen.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Open16.png"))));
        fileClose = new JMenuItem(fileCloseAction);
        fileClose.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "MyBlank16.gif"))));
        fileCloseAll = new JMenuItem(fileCloseAllAction);
        fileCloseAll.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "MyBlank16.gif"))));
        fileSave = new JMenuItem(fileSaveAction);
        fileSave.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Save16.png"))));
        fileSaveAs = new JMenuItem(fileSaveAsAction);
        fileSaveAs.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "SaveAs16.png"))));
        fileSaveAll = new JMenuItem(fileSaveAllAction);
        fileSaveAll.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "MyBlank16.gif"))));
        fileDumpMemory = new JMenuItem(fileDumpMemoryAction);
        fileDumpMemory.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Dump16.png"))));
        filePrint = new JMenuItem(filePrintAction);
        filePrint.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Print16.gif"))));
        fileExit = new JMenuItem(fileExitAction);
        fileExit.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "MyBlank16.gif"))));
        file.add(fileNew);
        file.add(fileOpen);
        file.add(fileClose);
        file.add(fileCloseAll);
        file.addSeparator();
        file.add(fileSave);
        file.add(fileSaveAs);
        file.add(fileSaveAll);
        if (DumpFormatLoader.getDumpFormats().size() > 0) {
            file.add(fileDumpMemory);
        }
        file.addSeparator();
        file.add(filePrint);
        file.addSeparator();
        file.add(fileExit);

        editUndo = new JMenuItem(editUndoAction);
        editUndo.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Undo16.png"))));//"Undo16.gif"))));
        editRedo = new JMenuItem(editRedoAction);
        editRedo.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Redo16.png"))));//"Redo16.gif"))));
        editCut = new JMenuItem(editCutAction);
        editCut.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Cut16.gif"))));
        editCopy = new JMenuItem(editCopyAction);
        editCopy.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Copy16.png"))));//"Copy16.gif"))));
        editPaste = new JMenuItem(editPasteAction);
        editPaste.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Paste16.png"))));//"Paste16.gif"))));
        editFindReplace = new JMenuItem(editFindReplaceAction);
        editFindReplace.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Find16.png"))));//"Paste16.gif"))));
        editSelectAll = new JMenuItem(editSelectAllAction);
        editSelectAll.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "MyBlank16.gif"))));
        edit.add(editUndo);
        edit.add(editRedo);
        edit.addSeparator();
        edit.add(editCut);
        edit.add(editCopy);
        edit.add(editPaste);
        edit.addSeparator();
        edit.add(editFindReplace);
        edit.add(editSelectAll);

        runAssemble = new JMenuItem(runAssembleAction);
        runAssemble.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Assemble16.png"))));//"MyAssemble16.gif"))));
        runGo = new JMenuItem(runGoAction);
        runGo.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Play16.png"))));//"Play16.gif"))));
        runStep = new JMenuItem(runStepAction);
        runStep.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "StepForward16.png"))));//"MyStepForward16.gif"))));
        runBackstep = new JMenuItem(runBackstepAction);
        runBackstep.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "StepBack16.png"))));//"MyStepBack16.gif"))));
        runReset = new JMenuItem(runResetAction);
        runReset.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Reset16.png"))));//"MyReset16.gif"))));
        runStop = new JMenuItem(runStopAction);
        runStop.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Stop16.png"))));//"Stop16.gif"))));
        runPause = new JMenuItem(runPauseAction);
        runPause.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Pause16.png"))));//"Pause16.gif"))));
        runClearBreakpoints = new JMenuItem(runClearBreakpointsAction);
        runClearBreakpoints.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "MyBlank16.gif"))));
        runToggleBreakpoints = new JMenuItem(runToggleBreakpointsAction);
        runToggleBreakpoints.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "MyBlank16.gif"))));

        run.add(runAssemble);
        run.add(runGo);
        run.add(runStep);
        run.add(runBackstep);
        run.add(runPause);
        run.add(runStop);
        run.add(runReset);
        run.addSeparator();
        run.add(runClearBreakpoints);
        run.add(runToggleBreakpoints);

        settingsLabel = new JCheckBoxMenuItem(settingsLabelAction);
        settingsLabel.setSelected(Globals.getSettings().getBooleanSetting(Settings.LABEL_WINDOW_VISIBILITY));
        settingsPopupInput = new JCheckBoxMenuItem(settingsPopupInputAction);
        settingsPopupInput.setSelected(Globals.getSettings().getBooleanSetting(Settings.POPUP_SYSCALL_INPUT));
        settingsValueDisplayBase = new JCheckBoxMenuItem(settingsValueDisplayBaseAction);
        settingsValueDisplayBase.setSelected(Globals.getSettings().getBooleanSetting(Settings.DISPLAY_VALUES_IN_HEX));//mainPane.getExecutePane().getValueDisplayBaseChooser().isSelected());
        // Tell the corresponding JCheckBox in the Execute Pane about me -- it has already been created.
        mainPane.getExecutePane().getValueDisplayBaseChooser().setSettingsMenuItem(settingsValueDisplayBase);
        settingsAddressDisplayBase = new JCheckBoxMenuItem(settingsAddressDisplayBaseAction);
        settingsAddressDisplayBase.setSelected(Globals.getSettings().getBooleanSetting(Settings.DISPLAY_ADDRESSES_IN_HEX));//mainPane.getExecutePane().getValueDisplayBaseChooser().isSelected());
        // Tell the corresponding JCheckBox in the Execute Pane about me -- it has already been created.
        mainPane.getExecutePane().getAddressDisplayBaseChooser().setSettingsMenuItem(settingsAddressDisplayBase);
        settingsExtended = new JCheckBoxMenuItem(settingsExtendedAction);
        settingsExtended.setSelected(Globals.getSettings().getBooleanSetting(Settings.EXTENDED_ASSEMBLER_ENABLED));
        settingsDelayedBranching = new JCheckBoxMenuItem(settingsDelayedBranchingAction);
        settingsDelayedBranching.setSelected(Globals.getSettings().getBooleanSetting(Settings.DELAYED_BRANCHING_ENABLED));
        settingsSelfModifyingCode = new JCheckBoxMenuItem(settingsSelfModifyingCodeAction);
        settingsSelfModifyingCode.setSelected(Globals.getSettings().getBooleanSetting(Settings.SELF_MODIFYING_CODE_ENABLED));
        settingsAssembleOnOpen = new JCheckBoxMenuItem(settingsAssembleOnOpenAction);
        settingsAssembleOnOpen.setSelected(Globals.getSettings().getBooleanSetting(Settings.ASSEMBLE_ON_OPEN_ENABLED));
        settingsAssembleAll = new JCheckBoxMenuItem(settingsAssembleAllAction);
        settingsAssembleAll.setSelected(Globals.getSettings().getBooleanSetting(Settings.ASSEMBLE_ALL_ENABLED));
        settingsWarningsAreErrors = new JCheckBoxMenuItem(settingsWarningsAreErrorsAction);
        settingsWarningsAreErrors.setSelected(Globals.getSettings().getBooleanSetting(Settings.WARNINGS_ARE_ERRORS));
        settingsStartAtMain = new JCheckBoxMenuItem(settingsStartAtMainAction);
        settingsStartAtMain.setSelected(Globals.getSettings().getBooleanSetting(Settings.START_AT_MAIN));
        settingsProgramArguments = new JCheckBoxMenuItem(settingsProgramArgumentsAction);
        settingsProgramArguments.setSelected(Globals.getSettings().getBooleanSetting(Settings.PROGRAM_ARGUMENTS));
        settingsEditor = new JMenuItem(settingsEditorAction);
        settingsHighlighting = new JMenuItem(settingsHighlightingAction);
        settingsExceptionHandler = new JMenuItem(settingsExceptionHandlerAction);
        settingsMemoryConfiguration = new JMenuItem(settingsMemoryConfigurationAction);

        settings.add(settingsLabel);
        settings.add(settingsProgramArguments);
        settings.add(settingsPopupInput);
        settings.add(settingsAddressDisplayBase);
        settings.add(settingsValueDisplayBase);
        settings.addSeparator();
        settings.add(settingsAssembleOnOpen);
        settings.add(settingsAssembleAll);
        settings.add(settingsWarningsAreErrors);
        settings.add(settingsStartAtMain);
        settings.addSeparator();
        settings.add(settingsExtended);
        settings.add(settingsDelayedBranching);
        settings.add(settingsSelfModifyingCode);
        settings.addSeparator();
        settings.add(settingsEditor);
        settings.add(settingsHighlighting);
        settings.add(settingsExceptionHandler);
        settings.add(settingsMemoryConfiguration);

        helpHelp = new JMenuItem(helpHelpAction);
        helpHelp.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "Help16.png"))));//"Help16.gif"))));
        helpAbout = new JMenuItem(helpAboutAction);
        helpAbout.setIcon(new ImageIcon(tk.getImage(cs.getResource(Globals.imagesPath + "MyBlank16.gif"))));
        help.add(helpHelp);
        help.addSeparator();
        help.add(helpAbout);

        menuBar.add(file);
        menuBar.add(edit);
        menuBar.add(run);
        menuBar.add(settings);
        JMenu toolMenu = ToolLoader.buildToolsMenu();
        if (toolMenu != null) menuBar.add(toolMenu);
        menuBar.add(help);

        // experiment with popup menu for settings. 3 Aug 2006 PS
        //setupPopupMenu();

        return menuBar;
    }
   
    /*
     * build the toolbar and connect items to action objects (which serve as action listeners
     * shared between toolbar icon and corresponding menu item).
     */

    JToolBar setUpToolBar() {
        JToolBar toolBar = new JToolBar();

        New = new JButton(fileNewAction);
        New.setText("");
        Open = new JButton(fileOpenAction);
        Open.setText("");
        Save = new JButton(fileSaveAction);
        Save.setText("");
        SaveAs = new JButton(fileSaveAsAction);
        SaveAs.setText("");
        DumpMemory = new JButton(fileDumpMemoryAction);
        DumpMemory.setText("");
        Print = new JButton(filePrintAction);
        Print.setText("");

        Undo = new JButton(editUndoAction);
        Undo.setText("");
        Redo = new JButton(editRedoAction);
        Redo.setText("");
        Cut = new JButton(editCutAction);
        Cut.setText("");
        Copy = new JButton(editCopyAction);
        Copy.setText("");
        Paste = new JButton(editPasteAction);
        Paste.setText("");
        FindReplace = new JButton(editFindReplaceAction);
        FindReplace.setText("");
        SelectAll = new JButton(editSelectAllAction);
        SelectAll.setText("");

        Run = new JButton(runGoAction);
        Run.setText("");
        Assemble = new JButton(runAssembleAction);
        Assemble.setText("");
        Step = new JButton(runStepAction);
        Step.setText("");
        Backstep = new JButton(runBackstepAction);
        Backstep.setText("");
        Reset = new JButton(runResetAction);
        Reset.setText("");
        Stop = new JButton(runStopAction);
        Stop.setText("");
        Pause = new JButton(runPauseAction);
        Pause.setText("");
        Help = new JButton(helpHelpAction);
        Help.setText("");

        toolBar.add(New);
        toolBar.add(Open);
        toolBar.add(Save);
        toolBar.add(SaveAs);
        if (DumpFormatLoader.getDumpFormats().size() > 0) {
            toolBar.add(DumpMemory);
        }
        toolBar.add(Print);
        toolBar.add(new JToolBar.Separator());
        toolBar.add(Undo);
        toolBar.add(Redo);
        toolBar.add(Cut);
        toolBar.add(Copy);
        toolBar.add(Paste);
        toolBar.add(FindReplace);
        toolBar.add(new JToolBar.Separator());
        toolBar.add(Assemble);
        toolBar.add(Run);
        toolBar.add(Step);
        toolBar.add(Backstep);
        toolBar.add(Pause);
        toolBar.add(Stop);
        toolBar.add(Reset);
        toolBar.add(new JToolBar.Separator());
        toolBar.add(Help);
        toolBar.add(new JToolBar.Separator());

        return toolBar;
    }


    /* Determine from FileStatus what the menu state (enabled/disabled)should 
     * be then call the appropriate method to set it.  Current states are:
     *
     * setMenuStateInitial: set upon startup and after File->Close
     * setMenuStateEditingNew: set upon File->New
     * setMenuStateEditing: set upon File->Open or File->Save or erroneous Run->Assemble
     * setMenuStateRunnable: set upon successful Run->Assemble
     * setMenuStateRunning: set upon Run->Go
     * setMenuStateTerminated: set upon completion of simulated execution
     */
    void setMenuState(int status) {
        menuState = status;
        switch (status) {
            case FileStatus.NO_FILE:
                setMenuStateInitial();
                break;
            case FileStatus.NEW_NOT_EDITED:
                setMenuStateEditingNew();
                break;
            case FileStatus.NEW_EDITED:
                setMenuStateEditingNew();
                break;
            case FileStatus.NOT_EDITED:
                setMenuStateNotEdited(); // was MenuStateEditing. DPS 9-Aug-2011
                break;
            case FileStatus.EDITED:
                setMenuStateEditing();
                break;
            case FileStatus.RUNNABLE:
                setMenuStateRunnable();
                break;
            case FileStatus.RUNNING:
                setMenuStateRunning();
                break;
            case FileStatus.TERMINATED:
                setMenuStateTerminated();
                break;
            case FileStatus.OPENING:// This is a temporary state. DPS 9-Aug-2011
                break;
            default:
                System.out.println("Invalid File Status: " + status);
                break;
        }
    }


    void setMenuStateInitial() {
        fileNewAction.setEnabled(true);
        fileOpenAction.setEnabled(true);
        fileCloseAction.setEnabled(false);
        fileCloseAllAction.setEnabled(false);
        fileSaveAction.setEnabled(false);
        fileSaveAsAction.setEnabled(false);
        fileSaveAllAction.setEnabled(false);
        fileDumpMemoryAction.setEnabled(false);
        filePrintAction.setEnabled(false);
        fileExitAction.setEnabled(true);
        editUndoAction.setEnabled(false);
        editRedoAction.setEnabled(false);
        editCutAction.setEnabled(false);
        editCopyAction.setEnabled(false);
        editPasteAction.setEnabled(false);
        editFindReplaceAction.setEnabled(false);
        editSelectAllAction.setEnabled(false);
        settingsDelayedBranchingAction.setEnabled(true); // added 25 June 2007
        settingsMemoryConfigurationAction.setEnabled(true); // added 21 July 2009
        runAssembleAction.setEnabled(false);
        runGoAction.setEnabled(false);
        runStepAction.setEnabled(false);
        runBackstepAction.setEnabled(false);
        runResetAction.setEnabled(false);
        runStopAction.setEnabled(false);
        runPauseAction.setEnabled(false);
        runClearBreakpointsAction.setEnabled(false);
        runToggleBreakpointsAction.setEnabled(false);
        helpHelpAction.setEnabled(true);
        helpAboutAction.setEnabled(true);
        editUndoAction.updateUndoState();
        editRedoAction.updateRedoState();
    }

    /* Added DPS 9-Aug-2011, for newly-opened files.  Retain
        existing Run menu state (except Assemble, which is always true).
         Thus if there was a valid assembly it is retained. */
    void setMenuStateNotEdited() {
      /* Note: undo and redo are handled separately by the undo manager*/
        fileNewAction.setEnabled(true);
        fileOpenAction.setEnabled(true);
        fileCloseAction.setEnabled(true);
        fileCloseAllAction.setEnabled(true);
        fileSaveAction.setEnabled(true);
        fileSaveAsAction.setEnabled(true);
        fileSaveAllAction.setEnabled(true);
        fileDumpMemoryAction.setEnabled(false);
        filePrintAction.setEnabled(true);
        fileExitAction.setEnabled(true);
        editCutAction.setEnabled(true);
        editCopyAction.setEnabled(true);
        editPasteAction.setEnabled(true);
        editFindReplaceAction.setEnabled(true);
        editSelectAllAction.setEnabled(true);
        settingsDelayedBranchingAction.setEnabled(true);
        settingsMemoryConfigurationAction.setEnabled(true);
        runAssembleAction.setEnabled(true);
        // If assemble-all, allow previous Run menu settings to remain.
        // Otherwise, clear them out.  DPS 9-Aug-2011
        if (!Globals.getSettings().getBooleanSetting(mars.Settings.ASSEMBLE_ALL_ENABLED)) {
            runGoAction.setEnabled(false);
            runStepAction.setEnabled(false);
            runBackstepAction.setEnabled(false);
            runResetAction.setEnabled(false);
            runStopAction.setEnabled(false);
            runPauseAction.setEnabled(false);
            runClearBreakpointsAction.setEnabled(false);
            runToggleBreakpointsAction.setEnabled(false);
        }
        helpHelpAction.setEnabled(true);
        helpAboutAction.setEnabled(true);
        editUndoAction.updateUndoState();
        editRedoAction.updateRedoState();
    }


    void setMenuStateEditing() {
      /* Note: undo and redo are handled separately by the undo manager*/
        fileNewAction.setEnabled(true);
        fileOpenAction.setEnabled(true);
        fileCloseAction.setEnabled(true);
        fileCloseAllAction.setEnabled(true);
        fileSaveAction.setEnabled(true);
        fileSaveAsAction.setEnabled(true);
        fileSaveAllAction.setEnabled(true);
        fileDumpMemoryAction.setEnabled(false);
        filePrintAction.setEnabled(true);
        fileExitAction.setEnabled(true);
        editCutAction.setEnabled(true);
        editCopyAction.setEnabled(true);
        editPasteAction.setEnabled(true);
        editFindReplaceAction.setEnabled(true);
        editSelectAllAction.setEnabled(true);
        settingsDelayedBranchingAction.setEnabled(true); // added 25 June 2007
        settingsMemoryConfigurationAction.setEnabled(true); // added 21 July 2009
        runAssembleAction.setEnabled(true);
        runGoAction.setEnabled(false);
        runStepAction.setEnabled(false);
        runBackstepAction.setEnabled(false);
        runResetAction.setEnabled(false);
        runStopAction.setEnabled(false);
        runPauseAction.setEnabled(false);
        runClearBreakpointsAction.setEnabled(false);
        runToggleBreakpointsAction.setEnabled(false);
        helpHelpAction.setEnabled(true);
        helpAboutAction.setEnabled(true);
        editUndoAction.updateUndoState();
        editRedoAction.updateRedoState();
    }

    /* Use this when "File -> New" is used
     */
    void setMenuStateEditingNew() {
      /* Note: undo and redo are handled separately by the undo manager*/
        fileNewAction.setEnabled(true);
        fileOpenAction.setEnabled(true);
        fileCloseAction.setEnabled(true);
        fileCloseAllAction.setEnabled(true);
        fileSaveAction.setEnabled(true);
        fileSaveAsAction.setEnabled(true);
        fileSaveAllAction.setEnabled(true);
        fileDumpMemoryAction.setEnabled(false);
        filePrintAction.setEnabled(true);
        fileExitAction.setEnabled(true);
        editCutAction.setEnabled(true);
        editCopyAction.setEnabled(true);
        editPasteAction.setEnabled(true);
        editFindReplaceAction.setEnabled(true);
        editSelectAllAction.setEnabled(true);
        settingsDelayedBranchingAction.setEnabled(true); // added 25 June 2007
        settingsMemoryConfigurationAction.setEnabled(true); // added 21 July 2009
        runAssembleAction.setEnabled(false);
        runGoAction.setEnabled(false);
        runStepAction.setEnabled(false);
        runBackstepAction.setEnabled(false);
        runResetAction.setEnabled(false);
        runStopAction.setEnabled(false);
        runPauseAction.setEnabled(false);
        runClearBreakpointsAction.setEnabled(false);
        runToggleBreakpointsAction.setEnabled(false);
        helpHelpAction.setEnabled(true);
        helpAboutAction.setEnabled(true);
        editUndoAction.updateUndoState();
        editRedoAction.updateRedoState();
    }

    /* Use this upon successful assemble or reset
     */
    void setMenuStateRunnable() {
      /* Note: undo and redo are handled separately by the undo manager */
        fileNewAction.setEnabled(true);
        fileOpenAction.setEnabled(true);
        fileCloseAction.setEnabled(true);
        fileCloseAllAction.setEnabled(true);
        fileSaveAction.setEnabled(true);
        fileSaveAsAction.setEnabled(true);
        fileSaveAllAction.setEnabled(true);
        fileDumpMemoryAction.setEnabled(true);
        filePrintAction.setEnabled(true);
        fileExitAction.setEnabled(true);
        editCutAction.setEnabled(true);
        editCopyAction.setEnabled(true);
        editPasteAction.setEnabled(true);
        editFindReplaceAction.setEnabled(true);
        editSelectAllAction.setEnabled(true);
        settingsDelayedBranchingAction.setEnabled(true); // added 25 June 2007
        settingsMemoryConfigurationAction.setEnabled(true); // added 21 July 2009
        runAssembleAction.setEnabled(true);
        runGoAction.setEnabled(true);
        runStepAction.setEnabled(true);
        runBackstepAction.setEnabled(
                Globals.getSettings().getBackSteppingEnabled() && !Globals.program.getBackStepper().empty());
        runResetAction.setEnabled(true);
        runStopAction.setEnabled(false);
        runPauseAction.setEnabled(false);
        runToggleBreakpointsAction.setEnabled(true);
        helpHelpAction.setEnabled(true);
        helpAboutAction.setEnabled(true);
        editUndoAction.updateUndoState();
        editRedoAction.updateRedoState();
    }

    /* Use this while program is running
     */
    void setMenuStateRunning() {
      /* Note: undo and redo are handled separately by the undo manager */
        fileNewAction.setEnabled(false);
        fileOpenAction.setEnabled(false);
        fileCloseAction.setEnabled(false);
        fileCloseAllAction.setEnabled(false);
        fileSaveAction.setEnabled(false);
        fileSaveAsAction.setEnabled(false);
        fileSaveAllAction.setEnabled(false);
        fileDumpMemoryAction.setEnabled(false);
        filePrintAction.setEnabled(false);
        fileExitAction.setEnabled(false);
        editCutAction.setEnabled(false);
        editCopyAction.setEnabled(false);
        editPasteAction.setEnabled(false);
        editFindReplaceAction.setEnabled(false);
        editSelectAllAction.setEnabled(false);
        settingsDelayedBranchingAction.setEnabled(false); // added 25 June 2007
        settingsMemoryConfigurationAction.setEnabled(false); // added 21 July 2009
        runAssembleAction.setEnabled(false);
        runGoAction.setEnabled(false);
        runStepAction.setEnabled(false);
        runBackstepAction.setEnabled(false);
        runResetAction.setEnabled(false);
        runStopAction.setEnabled(true);
        runPauseAction.setEnabled(true);
        runToggleBreakpointsAction.setEnabled(false);
        helpHelpAction.setEnabled(true);
        helpAboutAction.setEnabled(true);
        editUndoAction.setEnabled(false);//updateUndoState(); // DPS 10 Jan 2008
        editRedoAction.setEnabled(false);//updateRedoState(); // DPS 10 Jan 2008
    }

    /* Use this upon completion of execution
     */
    void setMenuStateTerminated() {
      /* Note: undo and redo are handled separately by the undo manager */
        fileNewAction.setEnabled(true);
        fileOpenAction.setEnabled(true);
        fileCloseAction.setEnabled(true);
        fileCloseAllAction.setEnabled(true);
        fileSaveAction.setEnabled(true);
        fileSaveAsAction.setEnabled(true);
        fileSaveAllAction.setEnabled(true);
        fileDumpMemoryAction.setEnabled(true);
        filePrintAction.setEnabled(true);
        fileExitAction.setEnabled(true);
        editCutAction.setEnabled(true);
        editCopyAction.setEnabled(true);
        editPasteAction.setEnabled(true);
        editFindReplaceAction.setEnabled(true);
        editSelectAllAction.setEnabled(true);
        settingsDelayedBranchingAction.setEnabled(true); // added 25 June 2007
        settingsMemoryConfigurationAction.setEnabled(true); // added 21 July 2009
        runAssembleAction.setEnabled(true);
        runGoAction.setEnabled(false);
        runStepAction.setEnabled(false);
        runBackstepAction.setEnabled(
                Globals.getSettings().getBackSteppingEnabled() && !Globals.program.getBackStepper().empty());
        runResetAction.setEnabled(true);
        runStopAction.setEnabled(false);
        runPauseAction.setEnabled(false);
        runToggleBreakpointsAction.setEnabled(true);
        helpHelpAction.setEnabled(true);
        helpAboutAction.setEnabled(true);
        editUndoAction.updateUndoState();
        editRedoAction.updateRedoState();
    }


    /**
     * Get current menu state.  State values are constants in FileStatus class.  DPS 23 July 2008
     *
     * @return current menu state.
     **/

    public static int getMenuState() {
        return menuState;
    }

    /**
     * To set whether the register values are reset.
     *
     * @param b Boolean true if the register values have been reset.
     **/

    public void setReset(boolean b) {
        reset = b;
    }

    /**
     * To set whether MIPS program execution has started.
     *
     * @param b true if the MIPS program execution has started.
     **/

    public void setStarted(boolean b) {
        started = b;
    }

    /**
     * To find out whether the register values are reset.
     *
     * @return Boolean true if the register values have been reset.
     **/

    public boolean getReset() {
        return reset;
    }

    /**
     * To find out whether MIPS program is currently executing.
     *
     * @return true if MIPS program is currently executing.
     **/
    public boolean getStarted() {
        return started;
    }

    /**
     * Get reference to Editor object associated with this GUI.
     *
     * @return Editor for the GUI.
     **/

    public Editor getEditor() {
        return editor;
    }

    /**
     * Get reference to messages pane associated with this GUI.
     *
     * @return MessagesPane object associated with the GUI.
     **/

    public MainPane getMainPane() {
        return mainPane;
    }

    /**
     * Get reference to messages pane associated with this GUI.
     *
     * @return MessagesPane object associated with the GUI.
     **/

    public MessagesPane getMessagesPane() {
        return messagesPane;
    }

    /**
     * Get reference to registers pane associated with this GUI.
     *
     * @return RegistersPane object associated with the GUI.
     **/

    public RegistersPane getRegistersPane() {
        return registersPane;
    }

    /**
     * Get reference to settings menu item for display base of memory/register values.
     *
     * @return the menu item
     **/

    public JCheckBoxMenuItem getValueDisplayBaseMenuItem() {
        return settingsValueDisplayBase;
    }

    /**
     * Get reference to settings menu item for display base of memory/register values.
     *
     * @return the menu item
     **/

    public JCheckBoxMenuItem getAddressDisplayBaseMenuItem() {
        return settingsAddressDisplayBase;
    }

    /**
     * Return reference tothe Run->Assemble item's action.  Needed by File->Open in case
     * assemble-upon-open flag is set.
     *
     * @return the Action object for the Run->Assemble operation.
     */
    public Action getRunAssembleAction() {
        return runAssembleAction;
    }

    /**
     * Have the menu request keyboard focus.  DPS 5-4-10
     */
    public void haveMenuRequestFocus() {
        this.menu.requestFocus();
    }

    /**
     * Send keyboard event to menu for possible processing.  DPS 5-4-10
     *
     * @param evt KeyEvent for menu component to consider for processing.
     */
    public void dispatchEventToMenu(KeyEvent evt) {
        this.menu.dispatchEvent(evt);
    }

    // pop up menu experiment 3 Aug 2006.  Keep for possible later revival.
    private void setupPopupMenu() {
        JPopupMenu popup;
        popup = new JPopupMenu();
        // cannot put the same menu item object on two different menus.
        // If you want to duplicate functionality, need a different item.
        // Should be able to share listeners, but if both menu items are
        // JCheckBoxMenuItem, how to keep their checked status in synch?
        // If you popup this menu and check the box, the right action occurs
        // but its counterpart on the regular menu is not checked.
        popup.add(new JCheckBoxMenuItem(settingsLabelAction));
        //Add listener to components that can bring up popup menus.
        MouseListener popupListener = new PopupListener(popup);
        this.addMouseListener(popupListener);
    }
}