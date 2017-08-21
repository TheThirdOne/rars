package mars.venus;

import mars.Globals;

import javax.swing.*;
import java.awt.event.ActionEvent;

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
 * Action  for the Help -> About menu item
 */
public class HelpAboutAction extends GuiAction {
    private VenusUI mainUI;
    public HelpAboutAction(String name, Icon icon, String descrip,
                           Integer mnemonic, KeyStroke accel, VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel);
        mainUI = gui;
    }

    public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(mainUI,
                "RARS " + Globals.version + "    Copyright " + Globals.copyrightYears + "\n" +
                        Globals.copyrightHolders + "\n" +
                        "RARS is the Mips Assembler and Runtime Simulator.\n\n" +
                        "Mars image courtesy of NASA/JPL.\n" +
                        "Toolbar and menu icons are from:\n" +
                        "  *  Tango Desktop Project (tango.freedesktop.org),\n" +
                        "  *  glyFX (www.glyfx.com) Common Toolbar Set,\n" +
                        "  *  KDE-Look (www.kde-look.org) crystalline-blue-0.1,\n" +
                        "  *  Icon-King (www.icon-king.com) Nuvola 1.0.",
                "About Rars",
                JOptionPane.INFORMATION_MESSAGE,
                new ImageIcon("images/RedMars50.gif"));
    }
}