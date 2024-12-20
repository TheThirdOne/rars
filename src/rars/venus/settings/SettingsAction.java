package rars.venus.settings;

import rars.Globals;
import rars.Settings;
import rars.venus.GuiAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Simple wrapper for boolean settings actions
 */
public class SettingsAction extends GuiAction {
    private Settings.Bool setting;

    public SettingsAction(String name, String descrip, Settings.Bool setting) {
        super(name, null, descrip, null, null);
        this.setting = setting;
    }
    public void actionPerformed(ActionEvent e) {
        boolean value = ((JCheckBoxMenuItem) e.getSource()).isSelected();
        Globals.getSettings().setBooleanSetting(setting, value);
        handler(value);
    }

    public void handler(boolean value) {
    }

}