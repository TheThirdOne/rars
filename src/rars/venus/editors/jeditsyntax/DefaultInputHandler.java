/*
 * DefaultInputHandler.java - Default implementation of an input handler
 * Copyright (C) 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

package rars.venus.editors.jeditsyntax;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * The default input handler. It maps sequences of keystrokes into actions
 * and inserts key typed events into the text area.
 *
 * @author Slava Pestov
 * @version $Id: DefaultInputHandler.java,v 1.18 1999/12/13 03:40:30 sp Exp $
 */
public class DefaultInputHandler extends InputHandler {
    /**
     * Creates a new input handler with no key bindings defined.
     */
    public DefaultInputHandler() {
        bindings = currentBindings = new BindingMap();
    }

    /**
     * Sets up the default key bindings.
     */
    public void addDefaultKeyBindings() {
        addKeyBinding("BACK_SPACE", BACKSPACE);
        addKeyBinding("C+BACK_SPACE", BACKSPACE_WORD);
        addKeyBinding("DELETE", DELETE);
        addKeyBinding("C+DELETE", DELETE_WORD);

        addKeyBinding("ENTER", INSERT_BREAK);
        addKeyBinding("TAB", INSERT_TAB);

        addKeyBinding("INSERT", OVERWRITE);
        addKeyBinding("C+\\", TOGGLE_RECT);

        addKeyBinding("HOME", HOME);
        addKeyBinding("END", END);
        addKeyBinding("C+A", SELECT_ALL);
        addKeyBinding("S+HOME", SELECT_HOME);
        addKeyBinding("S+END", SELECT_END);
        addKeyBinding("C+HOME", DOCUMENT_HOME);
        addKeyBinding("C+END", DOCUMENT_END);
        addKeyBinding("CS+HOME", SELECT_DOC_HOME);
        addKeyBinding("CS+END", SELECT_DOC_END);

        addKeyBinding("PAGE_UP", PREV_PAGE);
        addKeyBinding("PAGE_DOWN", NEXT_PAGE);
        addKeyBinding("S+PAGE_UP", SELECT_PREV_PAGE);
        addKeyBinding("S+PAGE_DOWN", SELECT_NEXT_PAGE);

        addKeyBinding("LEFT", PREV_CHAR);
        addKeyBinding("S+LEFT", SELECT_PREV_CHAR);
        addKeyBinding("C+LEFT", PREV_WORD);
        addKeyBinding("CS+LEFT", SELECT_PREV_WORD);
        addKeyBinding("RIGHT", NEXT_CHAR);
        addKeyBinding("S+RIGHT", SELECT_NEXT_CHAR);
        addKeyBinding("C+RIGHT", NEXT_WORD);
        addKeyBinding("CS+RIGHT", SELECT_NEXT_WORD);
        addKeyBinding("UP", PREV_LINE);
        addKeyBinding("S+UP", SELECT_PREV_LINE);
        addKeyBinding("DOWN", NEXT_LINE);
        addKeyBinding("S+DOWN", SELECT_NEXT_LINE);

        addKeyBinding("C+ENTER", REPEAT);

        // Clipboard
        addKeyBinding("C+C", CLIP_COPY);
        addKeyBinding("C+V", CLIP_PASTE);
        addKeyBinding("C+X", CLIP_CUT);
    }

    /**
     * Adds a key binding to this input handler. The key binding is
     * a list of white space separated key strokes of the form
     * <i>[modifiers+]key</i> where modifier is C for Control, A for Alt,
     * or S for Shift, and key is either a character (a-z) or a field
     * name in the KeyEvent class prefixed with VK_ (e.g., BACK_SPACE)
     *
     * @param keyBinding The key binding
     * @param action     The action
     */
    public void addKeyBinding(String keyBinding, ActionListener action) {
        BindingMap current = bindings;

        StringTokenizer st = new StringTokenizer(keyBinding);
        while (st.hasMoreTokens()) {
            KeyStroke keyStroke = parseKeyStroke(st.nextToken());
            if (keyStroke == null)
                return;

            if (st.hasMoreTokens()) {
                Binding o = current.get(keyStroke);
                if (o instanceof BindingMap)
                    current = (BindingMap) o;
                else {
                    o = new BindingMap();
                    current.put(keyStroke, o);
                    current = (BindingMap) o;
                }
            } else
                current.put(keyStroke, new BindingAction(action));
        }
    }

    /**
     * Removes a key binding from this input handler. This is not yet
     * implemented.
     *
     * @param keyBinding The key binding
     */
    public void removeKeyBinding(String keyBinding) {
        throw new InternalError("Not yet implemented");
    }

    /**
     * Removes all key bindings from this input handler.
     */
    public void removeAllKeyBindings() {
        bindings.clear();
    }

    /**
     * Returns a copy of this input handler that shares the same
     * key bindings. Setting key bindings in the copy will also
     * set them in the original.
     */
    public InputHandler copy() {
        return new DefaultInputHandler(this);
    }

    /**
     * Handle a key pressed event. This will look up the binding for
     * the key stroke and execute it.
     */
    public void keyPressed(KeyEvent evt) {
        int keyCode = evt.getKeyCode();
        int modifiers = evt.getModifiers();
        if (keyCode == KeyEvent.VK_CONTROL ||
                keyCode == KeyEvent.VK_SHIFT ||
                keyCode == KeyEvent.VK_ALT ||
                keyCode == KeyEvent.VK_META)
            return;

        if ((modifiers & ~KeyEvent.SHIFT_MASK) != 0
                || evt.isActionKey()
                || keyCode == KeyEvent.VK_BACK_SPACE
                || keyCode == KeyEvent.VK_DELETE
                || keyCode == KeyEvent.VK_ENTER
                || keyCode == KeyEvent.VK_TAB
                || keyCode == KeyEvent.VK_ESCAPE) {
            if (grabAction != null) {
                handleGrabAction(evt);
                return;
            }

            KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
            Binding o = currentBindings.get(keyStroke);

            if (o == null) {
                // Don't beep if the user presses some
                // key we don't know about unless a
                // prefix is active. Otherwise it will
                // beep when caps lock is pressed, etc.
                if (currentBindings != bindings) {
                    Toolkit.getDefaultToolkit().beep();
                    // F10 should be passed on, but C+e F10
                    // shouldn't
                    repeatCount = 0;
                    repeat = false;
                    evt.consume();
                }
                currentBindings = bindings;
                // No binding for this keyStroke, pass it to menu
                // (mnemonic, accelerator).  DPS 4-may-2010
                rars.Globals.getGui().dispatchEventToMenu(evt);
                evt.consume();
            } else if (o instanceof BindingAction) {
                currentBindings = bindings;
                executeAction(((BindingAction) o).actionListener,
                        evt.getSource(), null);

                evt.consume();
            } else if (o instanceof BindingMap) {
                currentBindings = (BindingMap) o;
                evt.consume();
            }
        }
    }

    /**
     * Handle a key typed event. This inserts the key into the text area.
     */
    public void keyTyped(KeyEvent evt) {
        int modifiers = evt.getModifiers();
        char c = evt.getKeyChar();
        // This IF statement needed to prevent Macintosh shortcut keyChar from
        // being echoed to the text area.  E.g. Command-s, for Save, will echo
        // the 's' character unless filtered out here.  Command modifier
        // matches KeyEvent.META_MASK.   DPS 30-Nov-2010
        if ((modifiers & KeyEvent.META_MASK) != 0)
            return;
        // DPS 9-Jan-2013.  Umberto Villano from Italy describes Alt combinations
        // not working on Italian Mac keyboards, where # requires Alt (Option).
        // This is preventing him from writing comments.  Similar complaint from
        // Joachim Parrow in Sweden, only for the $ character.  Villano pointed
        // me to this method.  Plus a Google search on "jeditsyntax alt key"
        // (without quotes) took me to
        // http://compgroups.net/comp.lang.java.programmer/option-key-in-jedit-syntax-package/1068227
        // which says to comment out the second condition in this IF statement:
        // if(c != KeyEvent.CHAR_UNDEFINED && (modifiers & KeyEvent.ALT_MASK) == 0)
        // So let's give it a try!
        // (...later) Bummer, it results in keystroke echoed into editing area when I use Alt
        // combination for shortcut menu access (e.g. Alt+f to open the File menu).
        //
        // Torsten Maehne: This is a shortcoming of the menu
        // shortcuts handling in the jedit component: It assumes that
        // modifier keys are the same across all platforms. However,
        // the menu shortcut keymask varies between OS X and
        // Windows/Linux, it is Cmd + <key> instead of Alt +
        // <key>. The "Java Development Guide for Mac" explicitly
        // discusses the issue in:
        // <https://developer.apple.com/library/mac/#documentation/Java/Conceptual/Java14Development/07-NativePlatformIntegration/NativePlatformIntegration.html#//apple_ref/doc/uid/TP40001909-211884-TPXREF130>
        //
        // As jedit always considers Alt + <key> as a keyboard
        // shortcut, they block their output in the editor, which
        // prevents the entry of special characters on OS X that uses
        // Alt + <key> for this purpose instead of AltGr + <key>, as
        // on Windows or Linux.
        //
        // For the latest jedit version (5.0.0), the menu
        // accelerators don't work on OS X, at least the special
        // characters can be entered using Alt + <key>. The issue is
        // still open, but there seems to be progress:
        //
        // http://sourceforge.net/tracker/index.php?func=detail&aid=3558572&group_id=588&atid=300588
        // http://sourceforge.net/tracker/?func=detail&atid=300588&aid=3604532&group_id=588
        //
        // Until this is resolved upstream, don't ignore characters
        // on OS X, which have been entered with the ALT modifier:
        if (c != KeyEvent.CHAR_UNDEFINED && (((modifiers & KeyEvent.ALT_MASK) == 0) || System.getProperty("os.name").contains("OS X"))) {
            if (c >= 0x20 && c != 0x7f) {
                KeyStroke keyStroke = KeyStroke.getKeyStroke(
                        Character.toUpperCase(c));
                Binding o = currentBindings.get(keyStroke);

                if (o instanceof BindingMap) {
                    currentBindings = (BindingMap) o;
                    return;
                } else if (o instanceof ActionListener) {
                    currentBindings = bindings;
                    executeAction((ActionListener) o,
                            evt.getSource(),
                            String.valueOf(c));
                    return;
                }

                currentBindings = bindings;

                if (grabAction != null) {
                    handleGrabAction(evt);
                    return;
                }

                // 0-9 adds another 'digit' to the repeat number
                if (repeat && Character.isDigit(c)) {
                    repeatCount *= 10;
                    repeatCount += (c - '0');
                    return;
                }
                executeAction(INSERT_CHAR, evt.getSource(),
                        String.valueOf(evt.getKeyChar()));
                repeatCount = 0;
                repeat = false;
            }
        }
    }

    /**
     * Converts a string to a keystroke. The string should be of the
     * form <i>modifiers</i>+<i>shortcut</i> where <i>modifiers</i>
     * is any combination of A for Alt, C for Control, S for Shift
     * or M for Meta, and <i>shortcut</i> is either a single character,
     * or a keycode name from the <code>KeyEvent</code> class, without
     * the <code>VK_</code> prefix.
     *
     * @param keyStroke A string description of the key stroke
     */
    public static KeyStroke parseKeyStroke(String keyStroke) {
        if (keyStroke == null)
            return null;
        int modifiers = 0;
        int index = keyStroke.indexOf('+');
        if (index != -1) {
            for (int i = 0; i < index; i++) {
                switch (Character.toUpperCase(keyStroke
                        .charAt(i))) {
                    case 'A':
                        modifiers |= InputEvent.ALT_MASK;
                        break;
                    case 'C':
                        modifiers |= InputEvent.CTRL_MASK;
                        break;
                    case 'M':
                        modifiers |= InputEvent.META_MASK;
                        break;
                    case 'S':
                        modifiers |= InputEvent.SHIFT_MASK;
                        break;
                }
            }
        }
        String key = keyStroke.substring(index + 1);
        if (key.length() == 1) {
            char ch = Character.toUpperCase(key.charAt(0));
            if (modifiers == 0)
                return KeyStroke.getKeyStroke(ch);
            else
                return KeyStroke.getKeyStroke(ch, modifiers);
        } else if (key.length() == 0) {
            System.err.println("Invalid key stroke: " + keyStroke);
            return null;
        } else {
            int ch;

            try {
                ch = KeyEvent.class.getField("VK_".concat(key))
                        .getInt(null);
            } catch (Exception e) {
                System.err.println("Invalid key stroke: "
                        + keyStroke);
                return null;
            }

            return KeyStroke.getKeyStroke(ch, modifiers);
        }
    }

    // private members
    private BindingMap bindings;
    private BindingMap currentBindings;

    private class Binding {
    }

    private class BindingAction extends Binding {
        ActionListener actionListener;

        BindingAction(ActionListener ac) {
            actionListener = ac;
        }
    }

    private class BindingMap extends Binding {
        Hashtable<KeyStroke, Binding> map;

        BindingMap() {
            map = new Hashtable<>();
        }

        void clear() {
            map.clear();
        }

        void put(KeyStroke k, Binding b) {
            map.put(k, b);
        }

        Binding get(KeyStroke k) {
            return map.get(k);
        }
    }

    private DefaultInputHandler(DefaultInputHandler copy) {
        bindings = currentBindings = copy.bindings;
    }
}
