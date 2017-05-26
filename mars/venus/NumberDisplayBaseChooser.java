package mars.venus;

import mars.Globals;
import mars.util.Binary;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

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
 * Use to select base for displaying numbers.  Initially the
 * choices are only 10 (decimal) and 16 (hex), so I'm using
 * a check box where checked means hex.  If base 8 (octal)
 * is added later, the Component will need to change.
 */

public class NumberDisplayBaseChooser extends JCheckBox {
    public static final int DECIMAL = 10;
    public static final int HEXADECIMAL = 16;
    public static final int ASCII = 0;
    private int base;
    private JCheckBoxMenuItem settingMenuItem;

    /**
     * constructor. It assumes the text will be worded
     * so that a checked box means hexadecimal!
     *
     * @param text        Text to accompany the check box.
     * @param defaultBase Currently either DECIMAL or HEXADECIMAL
     */
    public NumberDisplayBaseChooser(String text, boolean displayInHex) {
        super(text, displayInHex);
        base = getBase(displayInHex);
        addItemListener(
                new ItemListener() {
                    public void itemStateChanged(ItemEvent ie) {
                        NumberDisplayBaseChooser choose = (NumberDisplayBaseChooser) ie.getItem();
                        if (ie.getStateChange() == ItemEvent.SELECTED) {
                            choose.setBase(NumberDisplayBaseChooser.HEXADECIMAL);
                        } else {
                            choose.setBase(NumberDisplayBaseChooser.DECIMAL);
                        }
                        // Better to use notify, but I am tired...
                        if (settingMenuItem != null) {
                            settingMenuItem.setSelected(choose.isSelected());
                            ActionListener[] listeners = settingMenuItem.getActionListeners();
                            ActionEvent event = new ActionEvent(settingMenuItem, 0, "chooser");
                            for (int i = 0; i < listeners.length; i++) {
                                listeners[i].actionPerformed(event);
                            }
                        }
                        // Better to use notify, but I am tired...
                        Globals.getGui().getMainPane().getExecutePane().numberDisplayBaseChanged(choose);
                    }
                });
    }

    /**
     * Retrieve the current number base.
     *
     * @return current number base, currently DECIMAL or HEXADECIMAL
     */
    public int getBase() {
        return base;
    }

    /**
     * Set the current number base.
     *
     * @param newBase The new number base.  Currently, if it is
     *                neither DECIMAL nor HEXADECIMAL, the base will not be changed.
     */
    public void setBase(int newBase) {
        if (newBase == DECIMAL || newBase == HEXADECIMAL) {
            base = newBase;
        }
    }


    /**
     * Produces a string form of an unsigned given the value and the
     * numerical base to convert it to.  This class
     * method can be used by anyone anytime.  If base is 16, result
     * is same as for formatNumber().  If base is 10, will produce
     * string version of unsigned value.  E.g. 0xffffffff will produce
     * "4294967295" instead of "-1".
     *
     * @param value the number to be converted
     * @param base  the numerical base to use (currently 10 or 16)
     * @return a String equivalent of the value rendered appropriately.
     */
    public static String formatUnsignedInteger(int value, int base) {
        if (base == NumberDisplayBaseChooser.HEXADECIMAL) {
            return Binary.intToHexString(value);
        } else {
            return Binary.unsignedIntToIntString(value);
        }
    }


    /**
     * Produces a string form of an integer given the value and the
     * numerical base to convert it to.  There is an instance
     * method that uses the internally stored base.  This class
     * method can be used by anyone anytime.
     *
     * @param value the number to be converted
     * @param base  the numerical base to use (currently 10 or 16)
     * @return a String equivalent of the value rendered appropriately.
     */
    public static String formatNumber(int value, int base) {
        String result;
        switch (base) {
            case HEXADECIMAL:
                result = Binary.intToHexString(value);
                break;
            case DECIMAL:
                result = Integer.toString(value);
                break;
            case ASCII:
                result = Binary.intToAscii(value);
                break;
            default:
                result = Integer.toString(value);
        }
        return result;
        //          if (base == NumberDisplayBaseChooser.HEXADECIMAL) {
        //             return Binary.intToHexString(value);
        //          }
        //          else {
        //             return Integer.toString(value);
        //          }
    }


    /**
     * Produces a string form of a float given the value and the
     * numerical base to convert it to.  There is an instance
     * method that uses the internally stored base.  This class
     * method can be used by anyone anytime.
     *
     * @param value the number to be converted
     * @param base  the numerical base to use (currently 10 or 16)
     * @return a String equivalent of the value rendered appropriately.
     */
    public static String formatNumber(float value, int base) {
        if (base == NumberDisplayBaseChooser.HEXADECIMAL) {
            return Binary.intToHexString(Float.floatToIntBits(value));
        } else {
            return Float.toString(value);
        }
    }


    /**
     * Produces a string form of a double given the value and the
     * numerical base to convert it to.  There is an instance
     * method that uses the internally stored base.  This class
     * method can be used by anyone anytime.
     *
     * @param value the number to be converted
     * @param base  the numerical base to use (currently 10 or 16)
     * @return a String equivalent of the value rendered appropriately.
     */
    public static String formatNumber(double value, int base) {
        if (base == NumberDisplayBaseChooser.HEXADECIMAL) {
            long lguy = Double.doubleToLongBits(value);
            return Binary.intToHexString(Binary.highOrderLongToInt(lguy)) +
                    Binary.intToHexString(Binary.lowOrderLongToInt(lguy)).substring(2);
        } else {
            return Double.toString(value);
        }
    }

    /**
     * Produces a string form of a number given the value.  There
     * is also an class (static method) that uses a specified
     * base.
     *
     * @param value the number to be converted
     * @return a String equivalent of the value rendered appropriately.
     */
    public String formatNumber(int value) {
        if (base == NumberDisplayBaseChooser.HEXADECIMAL) {
            return Binary.intToHexString(value);
        } else {
            return new Integer(value).toString();
        }
    }

    /**
     * Produces a string form of an unsigned integer given the value.  There
     * is also an class (static method) that uses a specified base.
     * If the current base is 16, this produces the same result as formatNumber().
     *
     * @param value the number to be converted
     * @return a String equivalent of the value rendered appropriately.
     */
    public String formatUnsignedInteger(int value) {
        return formatUnsignedInteger(value, base);
    }


    /**
     * Produces a string form of a float given an integer containing
     * the 32 bit pattern and the numerical base to use (10 or 16).  If the
     * base is 16, the string will be built from the 32 bits.  If the
     * base is 10, the int bits will be converted to float and the
     * string constructed from that.  Seems an odd distinction to make,
     * except that contents of floating point registers are stored
     * internally as int bits.  If the int bits represent a NaN value
     * (of which there are many!), converting them to float then calling
     * formatNumber(float, int) above, causes the float value to become
     * the canonical NaN value 0x7fc00000.  It does not preserve the bit
     * pattern!  Then converting it to hex string yields the canonical NaN.
     * Not an issue if display base is 10 since result string will be NaN
     * no matter what the internal NaN value is.
     *
     * @param value the int bits to be converted to string of corresponding float.
     * @param base  the numerical base to use (currently 10 or 16)
     * @return a String equivalent of the value rendered appropriately.
     */
    public static String formatFloatNumber(int value, int base) {
        if (base == NumberDisplayBaseChooser.HEXADECIMAL) {
            return Binary.intToHexString(value);
        } else {
            return Float.toString(Float.intBitsToFloat(value));
        }
    }

    /**
     * Produces a string form of a double given a long containing
     * the 64 bit pattern and the numerical base to use (10 or 16).  If the
     * base is 16, the string will be built from the 64 bits.  If the
     * base is 10, the long bits will be converted to double and the
     * string constructed from that.  Seems an odd distinction to make,
     * except that contents of floating point registers are stored
     * internally as int bits.  If the int bits represent a NaN value
     * (of which there are many!), converting them to double then calling
     * formatNumber(double, int) above, causes the double value to become
     * the canonical NaN value.  It does not preserve the bit
     * pattern!  Then converting it to hex string yields the canonical NaN.
     * Not an issue if display base is 10 since result string will be NaN
     * no matter what the internal NaN value is.
     *
     * @param value the long bits to be converted to string of corresponding double.
     * @param base  the numerical base to use (currently 10 or 16)
     * @return a String equivalent of the value rendered appropriately.
     */
    public static String formatDoubleNumber(long value, int base) {
        if (base == NumberDisplayBaseChooser.HEXADECIMAL) {
            return Binary.longToHexString(value);
        } else {
            return Double.toString(Double.longBitsToDouble(value));
        }
    }


    /**
     * Set the menu item from Settings menu that corresponds to this chooser.
     * It is the responsibility of that item to register here, because this
     * one is created first (before the menu item).  They need to communicate
     * with each other so that whenever one changes, so does the other.  They
     * cannot be the same object (one is JCheckBox, the other is JCheckBoxMenuItem).
     */
    public void setSettingsMenuItem(JCheckBoxMenuItem setter) {
        settingMenuItem = setter;
    }


    /**
     * Return the number base corresponding to the specified setting.
     *
     * @return HEXADECIMAL if setting is true, DECIMAL otherwise.
     */
    public static int getBase(boolean setting) {
        return (setting) ? HEXADECIMAL : DECIMAL;
    }
}