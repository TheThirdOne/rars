package rars.venus.settings;

import rars.Globals;
import rars.simulator.Simulator;
import rars.util.Binary;
import rars.venus.FileStatus;
import rars.venus.GuiAction;
import rars.venus.VenusUI;
import rars.Launch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import java.net.URI;

	/*
Copyright (c) 2003-2009,  Pete Sanderson and Kenneth Vollmar

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
 * Action class for the Settings menu item for text editor settings.
 */
public class SettingsScaleAction extends GuiAction {
    /**
     * Create a new SettingsEditorAction. Has all the GuiAction parameters.
     */
    public SettingsScaleAction(String name, Icon icon, String descrip,
                                             Integer mnemonic, KeyStroke accel) {
        super(name, icon, descrip, mnemonic, accel);
    }

    /**
     * When this action is triggered, scale the GUI by a factor of 2. 
     * This is done by relaunching the GUI. 
     */
    public void actionPerformed(ActionEvent e) {
        try{
            // Get the absolute path of the rars jar file 
            String jar = findJarFile().getAbsolutePath();

            // Relaunch the jar with the uiScale set to 2.0 
            ProcessBuilder processBuilder = new ProcessBuilder(
                "java", "-Dsun.java2d.uiScale=2.0", "-jar", jar);
            processBuilder.start();
        } 
        catch(Exception ex) {
            // Do nothing if the above fails
        }
    }

    /**
     * This function is used by actionPerformed to find the absolute path to the rars jar file. 
     */
    private static File findJarFile() {
    try {
        // Gets the file location where the original jar was executed 
        URI uri = Launch.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        Path dir = Paths.get(uri);

        // Search recursively in subdirectories for "*rars*.jar"
        try (Stream<Path> paths = Files.walk(dir)) {
            return paths.filter(path -> path.toString().matches("^.*rars.*\\.jar$"))
                        .map(Path::toFile)
                        .findFirst()
                        .orElse(null);  // No matching JAR found
        }
    } 
    catch (Exception e) {
    }
        // Return null if the above fails. 
        return null; 
    }
}