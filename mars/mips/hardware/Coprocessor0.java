package mars.mips.hardware;

import mars.Globals;

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
 * Represents Coprocessor 0.  We will use only its interrupt/exception registers.
 *
 * @author Pete Sanderson
 * @version August 2005
 **/

public class Coprocessor0 {
    /**
     * Coprocessor register names
     */
    public static final int VADDR = 8;
    public static final int STATUS = 12;
    public static final int CAUSE = 13;
    public static final int EPC = 14;

    public static final int EXCEPTION_LEVEL = 1;  // bit position in STATUS register
    // bits 8-15 (mask for interrupt levels) all set, bit 4 (user mode) set,
    // bit 1 (exception level) not set, bit 0 (interrupt enable) set.
    public static final int DEFAULT_STATUS_VALUE = 0x0000FF11;

    private static final Register[] registers =
            {new Register("$8 (vaddr)", 8, 0),
                    new Register("$12 (status)", 12, DEFAULT_STATUS_VALUE),
                    new Register("$13 (cause)", 13, 0),
                    new Register("$14 (epc)", 14, 0)
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
     * Sets the value of the register given to the value given.
     *
     * @param n   name of register to set the value of ($n, where n is reg number).
     * @param val The desired value for the register.
     * @return old value in register prior to update
     **/

    public static int updateRegister(String n, int val) {
        int oldValue = 0;
        for (Register register : registers) {
            if (("$" + register.getNumber()).equals(n) || register.getName().equals(n)) {
                oldValue = register.getValue();
                register.setValue(val);
                break;
            }
        }
        return oldValue;
    }

    /**
     * This method updates the register value who's number is num.
     *
     * @param num Number of register to set the value of.
     * @param val The desired value for the register.
     * @return old value in register prior to update
     **/
    public static int updateRegister(int num, int val) {
        int old = 0;
        for (Register register : registers) {
            if (register.getNumber() == num) {
                old = (Globals.getSettings().getBackSteppingEnabled())
                        ? Globals.program.getBackStepper().addCoprocessor0Restore(num, register.setValue(val))
                        : register.setValue(val);
                break;
            }
        }
        return old;
    }


    /**
     * Returns the value of the register who's number is num.
     *
     * @param num The register number.
     * @return The value of the given register.  0 for non-implemented registers
     **/

    public static int getValue(int num) {
        for (Register register : registers) {
            if (register.getNumber() == num) {
                return register.getValue();
            }
        }
        return 0;
    }

    /**
     * For getting the number representation of the register.
     *
     * @param n The string formatted register name to look for.
     * @return The number of the register represented by the string. -1 if no match.
     **/

    public static int getNumber(String n) {
        for (Register register : registers) {
            if (("$" + register.getNumber()).equals(n) || register.getName().equals(n)) {
                return register.getNumber();
            }
        }
        return -1;
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
     * Coprocessor0 implements only selected registers, so the register number
     * (8, 12, 13, 14) does not correspond to its position in the list of registers
     * (0, 1, 2, 3).
     *
     * @param r A coprocessor0 Register
     * @return the list position of given register, -1 if not found.
     **/

    public static int getRegisterPosition(Register r) {
        for (int i = 0; i < registers.length; i++) {
            if (registers[i] == r) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get register object corresponding to given name.  If no match, return null.
     *
     * @param rname The register name,  in $0 format.
     * @return The register object,or null if not found.
     **/

    public static Register getRegister(String rname) {
        for (Register register : registers) {
            if (("$" + register.getNumber()).equals(rname) || register.getName().equals(rname)) {
                return register;
            }
        }
        return null;
    }


    /**
     * Method to reinitialize the values of the registers.
     **/

    public static void resetRegisters() {
        for (Register register : registers) {
            register.resetValue();
        }
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
