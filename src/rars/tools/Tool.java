package rars.tools;

/**
 * Interface for any tool that interacts with an executing program.
 * A qualifying tool must be a class in the Tools package that
 * implements the Tool interface, must be compiled into a .class file,
 * and its .class file must be in the same Tools folder as Tool.class.
 * Rars will detect a qualifying tool upon startup, create an instance
 * using its no-argument constructor and add it to its Tools menu.
 * When its menu item is selected, the action() method will be invoked.
 * <p>
 * <p>A tool may receive communication from system resources
 * (registers or memory) by registering as an Observer with
 * Memory and/or Register objects.
 * <p>
 * It may also
 * communicate directly with those resources through their
 * published methods PROVIDED any such communication is
 * wrapped inside a block synchronized on the
 * Globals.memoryAndRegistersLock object.
 */

public interface Tool {
    /**
     * Return a name you have chosen for this tool.  It will appear as the
     * menu item.
     */
    String getName();

    /**
     * Performs tool functions.  It will be invoked when the tool is selected
     * from the Tools menu.
     */

    void action();
}