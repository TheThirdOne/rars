package rars.venus.settings;

import rars.Globals;
import rars.Settings;
import rars.venus.GuiAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

/*
Copyright (c) 20017,  Benjamin Landers

Developed by Benjamin Landers

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
        handler(value);
        Globals.getSettings().setBooleanSetting(setting, value);
    }

    public void handler(boolean value) {
    }

}