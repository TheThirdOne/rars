package rars.venus.util;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// Experimental version 3 August 2006 Pete Sanderson
// This will display the Settings popup menu upon right-click.
// Menu selections themselves are handled separately.
// Code below is adapted from Java Tutorial on working with menus.

public class PopupListener extends MouseAdapter {
    private JPopupMenu popup;

    public PopupListener(JPopupMenu p) {
        popup = p;
    }

    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}