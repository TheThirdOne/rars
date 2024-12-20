package rars.venus;

import rars.Globals;

import javax.swing.*;
import java.awt.event.ActionEvent;

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
                        "RARS is the RISC-V Assembler and Runtime Simulator.\n\n" +                        "Toolbar and menu icons are from:\n" +
                        "  *  Tango Desktop Project (tango.freedesktop.org),\n" +
                        "  *  glyFX (www.glyfx.com) Common Toolbar Set,\n" +
                        "  *  KDE-Look (www.kde-look.org) crystalline-blue-0.1,\n" +
                        "  *  Icon-King (www.icon-king.com) Nuvola 1.0.",
                "About Rars",
                JOptionPane.INFORMATION_MESSAGE,
                new ImageIcon("images/RISC-V.png"));
    }
}