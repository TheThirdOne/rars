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
    private static Register[] registers =
            {new Register("$f0", 0, 0), new Register("$f1", 1, 0),
                    new Register("$f2", 2, 0), new Register("$f3", 3, 0),
                    new Register("$f4", 4, 0), new Register("$f5", 5, 0),
                    new Register("$f6", 6, 0), new Register("$f7", 7, 0),
                    new Register("$f8", 8, 0), new Register("$f9", 9, 0),
                    new Register("$f10", 10, 0), new Register("$f11", 11, 0),
                    new Register("$f12", 12, 0), new Register("$f13", 13, 0),
                    new Register("$f14", 14, 0), new Register("$f15", 15, 0),
                    new Register("$f16", 16, 0), new Register("$f17", 17, 0),
                    new Register("$f18", 18, 0), new Register("$f19", 19, 0),
                    new Register("$f20", 20, 0), new Register("$f21", 21, 0),
                    new Register("$f22", 22, 0), new Register("$f23", 23, 0),
                    new Register("$f24", 24, 0), new Register("$f25", 25, 0),
                    new Register("$f26", 26, 0), new Register("$f27", 27, 0),
                    new Register("$f28", 28, 0), new Register("$f29", 29, 0),
                    new Register("$f30", 30, 0), new Register("$f31", 31, 0)
            };
    // The 8 condition flags will be stored in bits 0-7 for flags 0-7.
    private static Register condition = new Register("cf", 32, 0);
    private static int numConditionFlags = 8;

    /**
     * Method for displaying the register values for debugging.
     **/

    public static void showRegisters() {
        for (int i = 0; i < registers.length; i++) {

            System.out.println("Name: " + registers[i].getName());
            System.out.println("Number: " + registers[i].getNumber());
            System.out.println("Value: " + registers[i].getValue());
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
     * Sets the value of the FPU register given to the 32-bit
     * pattern given by the int parameter.
     *
     * @param reg Register to set the value of.
     * @param val The desired int bit pattern for the register.
     **/

    public static void setRegisterToInt(String reg, int val) {
        setRegisterToInt(getRegisterNumber(reg), val);
    }


    /**
     * Sets the value of the FPU register given to the 32-bit
     * pattern given by the int parameter.
     *
     * @param reg Register to set the value of.
     * @param val The desired int bit pattern for the register.
     **/

    public static void setRegisterToInt(int reg, int val) {
        if (reg >= 0 && reg < registers.length) {
            registers[reg].setValue(val);
        }
    }


    /**
     * Sets the value of the FPU register given to the double value given.  The register
     * must be even-numbered, and the low order 32 bits are placed in it.  The high order
     * 32 bits are placed in the (odd numbered) register that follows it.
     *
     * @param reg Register to set the value of.
     * @param val The desired double value for the register.
     * @throws InvalidRegisterAccessException if register ID is invalid or odd-numbered.
     **/

    public static void setRegisterPairToDouble(int reg, double val)
            throws InvalidRegisterAccessException {
        if (reg % 2 != 0) {
            throw new InvalidRegisterAccessException();
        }
        long bits = Double.doubleToRawLongBits(val);
        registers[reg + 1].setValue(Binary.highOrderLongToInt(bits));  // high order 32 bits
        registers[reg].setValue(Binary.lowOrderLongToInt(bits)); // low order 32 bits
    }


    /**
     * Sets the value of the FPU register given to the double value given.  The register
     * must be even-numbered, and the low order 32 bits are placed in it.  The high order
     * 32 bits are placed in the (odd numbered) register that follows it.
     *
     * @param reg Register to set the value of.
     * @param val The desired double value for the register.
     * @throws InvalidRegisterAccessException if register ID is invalid or odd-numbered.
     **/
    public static void setRegisterPairToDouble(String reg, double val)
            throws InvalidRegisterAccessException {
        setRegisterPairToDouble(getRegisterNumber(reg), val);
    }


    /**
     * Sets the value of the FPU register pair given to the long value containing 64 bit pattern
     * given.  The register
     * must be even-numbered, and the low order 32 bits from the long are placed in it.  The high order
     * 32 bits from the long are placed in the (odd numbered) register that follows it.
     *
     * @param reg Register to set the value of.  Must be even register of even/odd pair.
     * @param val The desired double value for the register.
     * @throws InvalidRegisterAccessException if register ID is invalid or odd-numbered.
     **/

    public static void setRegisterPairToLong(int reg, long val)
            throws InvalidRegisterAccessException {
        if (reg % 2 != 0) {
            throw new InvalidRegisterAccessException();
        }
        registers[reg + 1].setValue(Binary.highOrderLongToInt(val));  // high order 32 bits
        registers[reg].setValue(Binary.lowOrderLongToInt(val)); // low order 32 bits
    }


    /**
     * Sets the value of the FPU register pair given to the long value containing 64 bit pattern
     * given.  The register
     * must be even-numbered, and the low order 32 bits from the long are placed in it.  The high order
     * 32 bits from the long are placed in the (odd numbered) register that follows it.
     *
     * @param reg Register to set the value of.  Must be even register of even/odd pair.
     * @param val The desired long value containing the 64 bits for the register pair.
     * @throws InvalidRegisterAccessException if register ID is invalid or odd-numbered.
     **/
    public static void setRegisterPairToLong(String reg, long val)
            throws InvalidRegisterAccessException {
        setRegisterPairToLong(getRegisterNumber(reg), val);
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
     * Gets the double value stored in the given FPU register.  The register
     * must be even-numbered.
     *
     * @param reg Register to get the value of. Must be even number of even/odd pair.
     * @throws InvalidRegisterAccessException if register ID is invalid or odd-numbered.
     **/

    public static double getDoubleFromRegisterPair(int reg)
            throws InvalidRegisterAccessException {
        if (reg % 2 != 0) {
            throw new InvalidRegisterAccessException();
        }
        long bits = Binary.twoIntsToLong(registers[reg + 1].getValue(), registers[reg].getValue());
        return Double.longBitsToDouble(bits);
    }


    /**
     * Gets the double value stored in the given FPU register.  The register
     * must be even-numbered.
     *
     * @param reg Register to get the value of. Must be even number of even/odd pair.
     * @throws InvalidRegisterAccessException if register ID is invalid or odd-numbered.
     **/

    public static double getDoubleFromRegisterPair(String reg)
            throws InvalidRegisterAccessException {
        return getDoubleFromRegisterPair(getRegisterNumber(reg));
    }


    /**
     * Gets a long representing the double value stored in the given double
     * precision FPU register.
     * The register must be even-numbered.
     *
     * @param reg Register to get the value of. Must be even number of even/odd pair.
     * @throws InvalidRegisterAccessException if register ID is invalid or odd-numbered.
     **/

    public static long getLongFromRegisterPair(int reg)
            throws InvalidRegisterAccessException {
        if (reg % 2 != 0) {
            throw new InvalidRegisterAccessException();
        }
        return Binary.twoIntsToLong(registers[reg + 1].getValue(), registers[reg].getValue());
    }


    /**
     * Gets the double value stored in the given FPU register.  The register
     * must be even-numbered.
     *
     * @param reg Register to get the value of. Must be even number of even/odd pair.
     * @throws InvalidRegisterAccessException if register ID is invalid or odd-numbered.
     **/

    public static long getLongFromRegisterPair(String reg)
            throws InvalidRegisterAccessException {
        return getLongFromRegisterPair(getRegisterNumber(reg));
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
        for (int i = 0; i < registers.length; i++) {
            if (registers[i].getNumber() == num) {
                old = (Globals.getSettings().getBackSteppingEnabled())
                        ? Globals.program.getBackStepper().addCoprocessor1Restore(num, registers[i].setValue(val))
                        : registers[i].setValue(val);
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
        for (int i = 0; i < registers.length; i++) {
            if (registers[i].getName().equals(n)) {
                j = registers[i].getNumber();
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
     * @param rName The FPU register name, must be "$f0" through "$f31".
     * @return The register object,or null if not found.
     **/

    public static Register getRegister(String rName) {
        Register reg = null;
        if (rName.charAt(0) == '$' && rName.length() > 1 && rName.charAt(1) == 'f') {
            try {
                // check for register number 0-31.
                reg = registers[Binary.stringToInt(rName.substring(2))];    // KENV 1/6/05
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
        for (int i = 0; i < registers.length; i++)
            registers[i].resetValue();
        clearConditionFlags();
    }


    /**
     * Each individual register is a separate object and Observable.  This handy method
     * will add the given Observer to each one.
     */
    public static void addRegistersObserver(Observer observer) {
        for (int i = 0; i < registers.length; i++) {
            registers[i].addObserver(observer);
        }
    }


    /**
     * Each individual register is a separate object and Observable.  This handy method
     * will delete the given Observer from each one.
     */
    public static void deleteRegistersObserver(Observer observer) {
        for (int i = 0; i < registers.length; i++) {
            registers[i].deleteObserver(observer);
        }
    }

    /**
     * Set condition flag to 1 (true).
     *
     * @param flag condition flag number (0-7)
     * @return previous flag setting (0 or 1)
     */
    public static int setConditionFlag(int flag) {
        int old = 0;
        if (flag >= 0 && flag < numConditionFlags) {
            old = getConditionFlag(flag);
            condition.setValue(Binary.setBit(condition.getValue(), flag));
            if (Globals.getSettings().getBackSteppingEnabled())
                if (old == 0) {
                    Globals.program.getBackStepper().addConditionFlagClear(flag);
                } else {
                    Globals.program.getBackStepper().addConditionFlagSet(flag);
                }
        }
        return old;
    }

    /**
     * Set condition flag to 0 (false).
     *
     * @param flag condition flag number (0-7)
     * @return previous flag setting (0 or 1)
     */
    public static int clearConditionFlag(int flag) {
        int old = 0;
        if (flag >= 0 && flag < numConditionFlags) {
            old = getConditionFlag(flag);
            condition.setValue(Binary.clearBit(condition.getValue(), flag));
            if (Globals.getSettings().getBackSteppingEnabled())
                if (old == 0) {
                    Globals.program.getBackStepper().addConditionFlagClear(flag);
                } else {
                    Globals.program.getBackStepper().addConditionFlagSet(flag);
                }
        }
        return old;
    }


    /**
     * Get value of specified condition flag (0-7).
     *
     * @param flag condition flag number (0-7)
     * @return 0 if condition is false, 1 if condition is true
     */
    public static int getConditionFlag(int flag) {
        if (flag < 0 || flag >= numConditionFlags)
            flag = 0;
        return Binary.bitValue(condition.getValue(), flag);
    }


    /**
     * Get array of condition flags (0-7).
     *
     * @return array of int condition flags
     */
    public static int getConditionFlags() {
        return condition.getValue();
    }


    /**
     * Clear all condition flags (0-7).
     */
    public static void clearConditionFlags() {
        condition.setValue(0);  // sets all 32 bits to 0.
    }

    /**
     * Set all condition flags (0-7).
     */
    public static void setConditionFlags() {
        condition.setValue(-1);  // sets all 32 bits to 1.
    }

    /**
     * Get count of condition flags.
     *
     * @return number of condition flags
     */
    public static int getConditionFlagCount() {
        return numConditionFlags;
    }
}
