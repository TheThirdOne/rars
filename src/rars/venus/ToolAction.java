package rars.venus;

import rars.tools.Tool;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Connects a Tool class (class that implements Tool interface) to
 * the Mars menu system by supplying the response to that tool's menu item
 * selection.
 *
 * @author Pete Sanderson
 * @version August 2005
 */

public class ToolAction extends AbstractAction {
    private Tool tool; //Tool tool;

    /**
     * Simple constructor.
     *
     * @param tool
     */
    public ToolAction(Tool tool) {
        super(tool.getName(), null);
        this.tool = tool;
    }


    /**
     * Response when tool's item selected from menu.  Invokes tool's action() method.
     *
     * @param e the ActionEvent that triggered this call
     */
    public void actionPerformed(ActionEvent e) {
        try {
            // I am not sure if a new instance needs to be made each time
            // It may be possible to reduce this to tool.action()
            tool.getClass().newInstance().action();
        } catch (Exception ex) {
        }
    }
}