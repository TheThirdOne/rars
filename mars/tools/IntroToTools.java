package mars.tools;

import javax.swing.*;
import java.awt.*;

	
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
 * The "hello world" of MarsTools!
 */
public class IntroToTools extends AbstractMarsToolAndApplication {

    private static String heading = "Introduction to MARS Tools and Applications";
    private static String version = " Version 1.0";

    /**
     * Simple constructor, likely used to run a stand-alone memory reference visualizer.
     *
     * @param title   String containing title for title bar
     * @param heading String containing text for heading shown in upper part of window.
     */
    public IntroToTools(String title, String heading) {
        super(title, heading);
    }

    /**
     * Simple constructor, likely used by the MARS Tools menu mechanism
     */
    public IntroToTools() {
        super(heading + ", " + version, heading);
    }


    /**
     * Main provided for pure stand-alone use.  Recommended stand-alone use is to write a
     * driver program that instantiates a MemoryReferenceVisualization object then invokes its go() method.
     * "stand-alone" means it is not invoked from the MARS Tools menu.  "Pure" means there
     * is no driver program to invoke the application.
     */
    public static void main(String[] args) {
        new IntroToTools(heading + ", " + version, heading).go();
    }


    /**
     * Required method to return Tool name.
     *
     * @return Tool name.  MARS will display this in menu item.
     */
    public String getName() {
        return "Introduction to Tools";
    }

    /**
     * Implementation of the inherited abstract method to build the main
     * display area of the GUI.  It will be placed in the CENTER area of a
     * BorderLayout.  The title is in the NORTH area, and the controls are
     * in the SOUTH area.
     */
    protected JComponent buildMainDisplayArea() {
        JTextArea message = new JTextArea();
        message.setEditable(false);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setFont(new Font("Ariel", Font.PLAIN, 12));
        message.setText(
                "Hello!  This Tool does not do anything but you may use its " +
                        "source code as a starting point to build your own MARS Tool " +
                        "or Application." +
                        "\n\n" +
                        "A MARS Tool is a program listed in the MARS Tools menu.  It is launched " +
                        "when you select its menu item and typically interacts with executing MIPS " +
                        "programs to do something exciting and informative or at least interesting." +
                        "\n\n" +
                        "A MARS Application is a stand-alone program for similarly interacting with " +
                        "executing MIPS programs.  It uses MARS' MIPS assembler and " +
                        "runtime simulator in the background to control MIPS execution." +
                        "\n\n" +
                        "The basic requirements for building a MARS Tool are:" +
                        "\n" +
                        "  1. It must be a class that implements the MarsTool interface.  " +
                        "This has only two methods: 'String getName()' to return the " +
                        "name to be displayed in its Tools menu item, and " +
                        "'void action()' which is invoked when that menu item " +
                        "is selected by the MARS user." +
                        "\n" +
                        "  2. It must be stored in the mars.tools package (in folder " +
                        "mars/tools)" +
                        "\n" +
                        "  3. It must be successfully compiled in that package.  This " +
                        "normally means the MARS distribution needs to be extracted from the " +
                        "JAR file before you can develop your Tool." +
                        "\n\n" +
                        "If these requirements are met, MARS will recognize and load " +
                        "your Tool into its Tools menu the next time it runs." +
                        "\n\n" +
                        "There are no fixed requirements for building a MARS Application, a " +
                        "stand-alone program that utilizes the MARS API." +
                        "\n\n" +
                        "You can build a program that may be run as either a MARS Tool or an Application.  " +
                        "The easiest way is to extend an abstract class provided in the MARS distribution: " +
                        "mars.tools.AbstractMarsToolAndApplication.  " +
                        "\n" +
                        "  1. It defines a suite of methods and provides default definitions for " +
                        "all but two: getName() and buildMainDisplayArea()." +
                        "\n" +
                        "  2.  String getName() was introduced above." +
                        "\n" +
                        "  3.  JComponent buildMainDisplayArea() returns the JComponent to be placed in the " +
                        "BorderLayout.CENTER region of the tool/app's user interface.  The NORTH and " +
                        "SOUTH are defined to contain a heading and a set of button controls, respectively.  " +
                        "\n" +
                        "  4. It defines a default 'void go()' method to launch the application." +
                        "\n" +
                        "  5. Conventional usage is to define your application as a subclass then launch it " +
                        "by invoking its go() method." +
                        "\n\n" +
                        "The frame/dialog you are reading right now is an example of an " +
                        "AbstractMarsToolAndApplication subclass.  If you run it as an application, you " +
                        "will notice the set of controls at the bottom of the window differ from those " +
                        "you get when running it from MARS' Tools menu.  It includes additional controls " +
                        "to load and control the execution of pre-existing MIPS programs." +
                        "\n\n" +
                        "See the mars.tools.AbstractMarsToolAndApplication API or the source code of " +
                        "existing tool/apps for further information." +
                        "\n"
        );
        message.setCaretPosition(0); // Assure first line is visible and at top of scroll pane.
        return new JScrollPane(message);
    }

}