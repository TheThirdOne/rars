package com.github.unaimillan.rars.venus;

import com.github.unaimillan.rars.tools.Tool;
import com.github.unaimillan.rars.util.FilenameFinder;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;

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
 * This class provides functionality to bring external Mars tools into the Mars
 * system by adding them to its Tools menu.  This permits anyone with knowledge
 * of the Mars public interfaces, in particular of the Memory and Register
 * classes, to write applications which can interact with a MIPS program
 * executing under Mars.  The execution is of course simulated.  The
 * private method for loading tool classes is adapted from Bret Barker's
 * GameServer class from the book "Developing Games In Java".
 *
 * @author Pete Sanderson with help from Bret Barker
 * @version August 2005
 */

public class ToolLoader {

    private static final String CLASS_PREFIX = "rars.tools.";
    private static final String TOOLS_DIRECTORY_PATH = "rars/tools";
    private static final String TOOLS_MENU_NAME = "Tools";
    private static final String TOOL_INTERFACE = "Tool.class";
    private static final String CLASS_EXTENSION = "class";

    /**
     * Called in VenusUI to build its Tools menu.  If there are no qualifying tools
     * or any problems accessing those tools, it returns null.  A qualifying tool
     * must be a class in the Tools package that implements Tool, must be compiled
     * into a .class file, and its .class file must be in the same Tools folder as
     * Tool.class.
     *
     * @return a Tools JMenu if qualifying tool classes are found, otherwise null
     */
    public static JMenu buildToolsMenu() {
        JMenu menu = null;
        ArrayList<Tool> toolList = loadTools();
        if (!toolList.isEmpty()) {
            menu = new JMenu(TOOLS_MENU_NAME);
            menu.setMnemonic(KeyEvent.VK_T);
            // traverse array list and build menu
            for (Tool tool : toolList) {
                menu.add(new ToolAction(tool));
            }
        }
        return menu;
    }

    /*
     *  Dynamically loads MarsTools into an ArrayList.  This method is adapted from
     *  the loadGameControllers() method in Bret Barker's GameServer class.
     *  Barker (bret@hypefiend.com) is co-author of the book "Developing Games
     *  in Java".  It was demo'ed to me by Otterbein student Chris Dieterle
     *  as part of his Spring 2005 independent study of implementing a networked
     *  multi-player game playing system.  Thanks Bret and Chris!
     *
     *  Bug Fix 25 Feb 06, DPS: method did not recognize tools folder if its
     *  absolute pathname contained one or more spaces (e.g. C:\Program Files\rars\tools).
     *  Problem was, class loader's getResource method returns a URL, in which spaces
     *  are replaced with "%20".  So I added a replaceAll() to change them back.
     *
     *  Enhanced 3 Oct 06, DPS: method did not work if running MARS from a JAR file.
     *  The array of files returned is null, but the File object contains the name
     *  of the JAR file (using toString, not getName).  Extract that name, open it
     *  as a ZipFile, get the ZipEntry enumeration, find the class files in the tools
     *  folder, then continue as before.
     */
    private static ArrayList<Tool> loadTools() {
        ArrayList<Tool> toolList = new ArrayList<>();
        ArrayList<String> candidates = FilenameFinder.getFilenameList(ToolLoader.class.getClassLoader(),
                TOOLS_DIRECTORY_PATH, CLASS_EXTENSION);
        // Add any tools stored externally, as listed in Config.properties file.
        // This needs some work, because rars.Globals.getExternalTools() returns
        // whatever is in the properties file entry.  Since the class file will
        // not be located in the rars.tools folder, the loop below will not process
        // it correctly.  Not sure how to create a Class object given an absolute
        // pathname.
        //candidates.addAll(rars.Globals.getExternalTools());  // this by itself is not enough...
        HashSet<String> tools = new HashSet<>();
        for (String file : candidates) {
            // Do not add class if already encountered (happens if run in MARS development directory)
            if (tools.contains(file)) {
                continue;
            } else {
                tools.add(file);
            }
            if (!file.equals(TOOL_INTERFACE)) {
                try {
                    // grab the class, make sure it implements Tool, instantiate, add to menu
                    String toolClassName = CLASS_PREFIX + file.substring(0, file.indexOf(CLASS_EXTENSION) - 1);
                    Class clas = Class.forName(toolClassName);
                    if (!Tool.class.isAssignableFrom(clas) ||
                            Modifier.isAbstract(clas.getModifiers()) ||
                            Modifier.isInterface(clas.getModifiers())) {
                        continue;
                    }

                    toolList.add((Tool) clas.newInstance());
                } catch (Exception e) {
                    System.out.println("Error instantiating Tool from file " + file + ": " + e);
                }
            }
        }
        return toolList;
    }
}