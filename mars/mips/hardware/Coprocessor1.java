package mars.mips.hardware;

import mars.Globals;
import mars.util.Binary;

import java.util.Observer;

/*
Copyright (c) 2003-2009,  Pete Sanderson and Kenneth Vollmar

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
 * Represents Coprocessor 1, the Floating Point Unit (FPU)
 *
 * @author Pete Sanderson
 * @version July 2005
 **/

// Adapted from RegisterFile class developed by Bumgarner et al in 2003.
// The FPU registers will be implemented by Register objects.  Such objects
// can only hold int values, but we can use Float.floatToIntBits() to translate
// a 32 bit float value into its equivalent 32-bit int representation, and
// Float.intBitsToFloat() to bring it back.  More importantly, there are 
// similar methods Double.doubleToLongBits() and Double.LongBitsToDouble()
// which can be used to extend a double value over 2 registers.  The resulting
// long is split into 2 int values (high order 32 bits, low order 32 bits) for
// storing into registers, and reassembled upon retrieval.

public class Coprocessor1 {
    private static final Register[] registers =
            {new Register("f0", 0, 0), new Register("f1", 1, 0),
                    new Register("f2", 2, 0), new Register("f3", 3, 0),
                    new Register("f4", 4, 0), new Register("f5", 5, 0),
                    new Register("f6", 6, 0), new Register("f7", 7, 0),
                    new Register("f8", 8, 0), new Register("f9", 9, 0),
                    new Register("f10", 10, 0), new Register("f11", 11, 0),
                    new Register("f12", 12, 0), new Register("f13", 13, 0),
                    new Register("f14", 14, 0), new Register("f15", 15, 0),
                    new Register("f16", 16, 0), new Register("f17", 17, 0),
                    new Register("f18", 18, 0), new Register("f19", 19, 0),
                    new Register("f20", 20, 0), new Register("f21", 21, 0),
                    new Register("f22", 22, 0), new Register("f23", 23, 0),
                    new Register("f24", 24, 0), new Register("f25", 25, 0),
                    new Register("f26", 26, 0), new Register("f27", 27, 0),
                    new Register("f28", 28, 0), new Register("f29", 29, 0),
                    new Register("f30", 30, 0), new Register("f31", 31, 0)
            };

    /**
     * Method for displaying the register values for debugging.
     **/

    public static void showRegisters() {
        for (Register register : registers) {

            System.out.println("Name: " + register.getName());
            System.out.println("Number: " + register.getNumber());
            System.out.println("Value: " + register.getValue());
            System.out.println("");
        }
    }

    /**
     * Sets the value of the FPU register given to the value given.
     *
     * @param reg Register to set the value of.
     * @param val The desired float value for the register.
     **/

    public static void setRegisterToFloat(String reg, float val) {
        setRegisterToFloat(getRegisterNumber(reg), val);
    }


    /**
     * Sets the value of the FPU register given to the value given.
     *
     * @param reg Register to set the value of.
     * @param val The desired float value for the register.
     **/

    public static void setRegisterToFloat(int reg, float val) {
        if (reg >= 0 && reg < registers.length) {
            registers[reg].setValue(Float.floatToRawIntBits(val));
        }
    }

    /**
     * Gets the float value stored in the given FPU register.
     *
     * @param reg Register to get the value of.
     * @return The  float value stored by that register.
     **/

    public static float getFloatFromRegister(int reg) {
        float result = 0F;
        if (reg >= 0 && reg < registers.length) {
            result = Float.intBitsToFloat(registers[reg].getValue());
        }
        return result;
    }


    /**
     * Gets the float value stored in the given FPU register.
     *
     * @param reg Register to get the value of.
     * @return The  float value stored by that register.
     **/

    public static float getFloatFromRegister(String reg) {
        return getFloatFromRegister(getRegisterNumber(reg));
    }


    /**
     * Gets the 32-bit int bit pattern stored in the given FPU register.
     *
     * @param reg Register to get the value of.
     * @return The int bit pattern stored by that register.
     **/

    public static int getIntFromRegister(int reg) {
        int result = 0;
        if (reg >= 0 && reg < registers.length) {
            result = registers[reg].getValue();
        }
        return result;
    }


    /**
     * Gets the 32-bit int bit pattern stored in the given FPU register.
     *
     * @param reg Register to get the value of.
     * @return The int bit pattern stored by that register.
     **/

    public static int getIntFromRegister(String reg) {
        return getIntFromRegister(getRegisterNumber(reg));
    }

    /**
     * This method updates the FPU register value who's number is num.  Note the
     * registers themselves hold an int value.  There are helper methods available
     * to which you can give a float or double to store.
     *
     * @param num FPU register to set the value of.
     * @param val The desired int value for the register.
     **/

    public static int updateRegister(int num, int val) {
        int old = 0;
        for (Register register : registers) {
            if (register.getNumber() == num) {
                old = (Globals.getSettings().getBackSteppingEnabled())
                        ? Globals.program.getBackStepper().addCoprocessor1Restore(num, register.setValue(val))
                        : register.setValue(val);
                break;
            }
        }
        return old;
    }

    /**
     * Returns the value of the FPU register who's number is num.  Returns the
     * raw int value actually stored there.  If you need a float, use
     * Float.intBitsToFloat() to get the equivent float.
     *
     * @param num The FPU register number.
     * @return The int value of the given register.
     **/

    public static int getValue(int num) {
        return registers[num].getValue();
    }

    /**
     * For getting the number representation of the FPU register.
     *
     * @param n The string formatted register name to look for.
     * @return The number of the register represented by the string.
     **/

    public static int getRegisterNumber(String n) {
        int j = -1;
        for (Register register : registers) {
            if (register.getName().equals(n)) {
                j = register.getNumber();
                break;
            }
        }
        return j;
    }

    /**
     * For returning the set of registers.
     *
     * @return The set of registers.
     **/

    public static Register[] getRegisters() {
        return registers;
    }

    /**
     * Get register object corresponding to given name.  If no match, return null.
     *
     * @param rName The FPU register name, must be "f0" through "f31".
     * @return The register object,or null if not found.
     **/

    public static Register getRegister(String rName) {
        Register reg = null;
        if (rName.length() > 1 && rName.charAt(0) == 'f') {
            try {
                // check for register number 0-31.
                reg = registers[Binary.stringToInt(rName.substring(1))];    // KENV 1/6/05
            } catch (Exception e) {
                // handles both NumberFormat and ArrayIndexOutOfBounds
                reg = null;
            }
        }
        return reg;
    }


    /**
     * Method to reinitialize the values of the registers.
     **/

    public static void resetRegisters() {
        for (Register register : registers) register.resetValue();
    }


    /**
     * Each individual register is a separate object and Observable.  This handy method
     * will add the given Observer to each one.
     */
    public static void addRegistersObserver(Observer observer) {
        for (Register register : registers) {
            register.addObserver(observer);
        }
    }

    /**
     * Each individual register is a separate object and Observable.  This handy method
     * will delete the given Observer from each one.
     */
    public static void deleteRegistersObserver(Observer observer) {
        for (Register register : registers) {
            register.deleteObserver(observer);
        }
    }
}
