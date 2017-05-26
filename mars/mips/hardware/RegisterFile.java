package mars.mips.hardware;

import mars.Globals;
import mars.assembler.SymbolTable;
import mars.mips.instructions.Instruction;
import mars.util.Binary;

import java.util.Observer;

/*
Copyright (c) 2003-2008,  Pete Sanderson and Kenneth Vollmar

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
 * Represents the collection of MIPS registers.
 *
 * @author Jason Bumgarner, Jason Shrewsbury
 * @version June 2003
 **/

public class RegisterFile {

    public static final int GLOBAL_POINTER_REGISTER = 28;
    public static final int STACK_POINTER_REGISTER = 29;

    private static Register[] regFile =
            {new Register("$zero", 0, 0), new Register("$at", 1, 0),
                    new Register("$v0", 2, 0), new Register("$v1", 3, 0),
                    new Register("$a0", 4, 0), new Register("$a1", 5, 0),
                    new Register("$a2", 6, 0), new Register("$a3", 7, 0),
                    new Register("$t0", 8, 0), new Register("$t1", 9, 0),
                    new Register("$t2", 10, 0), new Register("$t3", 11, 0),
                    new Register("$t4", 12, 0), new Register("$t5", 13, 0),
                    new Register("$t6", 14, 0), new Register("$t7", 15, 0),
                    new Register("$s0", 16, 0), new Register("$s1", 17, 0),
                    new Register("$s2", 18, 0), new Register("$s3", 19, 0),
                    new Register("$s4", 20, 0), new Register("$s5", 21, 0),
                    new Register("$s6", 22, 0), new Register("$s7", 23, 0),
                    new Register("$t8", 24, 0), new Register("$t9", 25, 0),
                    new Register("$k0", 26, 0), new Register("$k1", 27, 0),
                    new Register("$gp", GLOBAL_POINTER_REGISTER, Memory.globalPointer),
                    new Register("$sp", STACK_POINTER_REGISTER, Memory.stackPointer),
                    new Register("$fp", 30, 0), new Register("$ra", 31, 0)
            };

    private static Register programCounter = new Register("pc", 32, Memory.textBaseAddress);
    private static Register hi = new Register("hi", 33, 0);//this is an internal register with arbitrary number
    private static Register lo = new Register("lo", 34, 0);// this is an internal register with arbitrary number


    /**
     * Method for displaying the register values for debugging.
     **/

    public static void showRegisters() {
        for (int i = 0; i < regFile.length; i++) {
            System.out.println("Name: " + regFile[i].getName());
            System.out.println("Number: " + regFile[i].getNumber());
            System.out.println("Value: " + regFile[i].getValue());
            System.out.println("");
        }
    }


    /**
     * This method updates the register value who's number is num.  Also handles the lo and hi registers
     *
     * @param num Register to set the value of.
     * @param val The desired value for the register.
     **/

    public static int updateRegister(int num, int val) {
        int old = 0;
        if (num == 0) {
            //System.out.println("You can not change the value of the zero register.");
        } else {
            for (int i = 0; i < regFile.length; i++) {
                if (regFile[i].getNumber() == num) {
                    old = (Globals.getSettings().getBackSteppingEnabled())
                            ? Globals.program.getBackStepper().addRegisterFileRestore(num, regFile[i].setValue(val))
                            : regFile[i].setValue(val);
                    break;
                }
            }
        }
        if (num == 33) {//updates the hi register
            old = (Globals.getSettings().getBackSteppingEnabled())
                    ? Globals.program.getBackStepper().addRegisterFileRestore(num, hi.setValue(val))
                    : hi.setValue(val);
        } else if (num == 34) {// updates the low register
            old = (Globals.getSettings().getBackSteppingEnabled())
                    ? Globals.program.getBackStepper().addRegisterFileRestore(num, lo.setValue(val))
                    : lo.setValue(val);
        }
        return old;
    }

    /**
     * Sets the value of the register given to the value given.
     *
     * @param reg Name of register to set the value of.
     * @param val The desired value for the register.
     **/

    public static void updateRegister(String reg, int val) {
        if (reg.equals("zero")) {
            //System.out.println("You can not change the value of the zero register.");
        } else {
            for (int i = 0; i < regFile.length; i++) {
                if (regFile[i].getName().equals(reg)) {
                    updateRegister(i, val);
                    break;
                }
            }
        }
    }

    /**
     * Returns the value of the register who's number is num.
     *
     * @param num The register number.
     * @return The value of the given register.
     **/

    public static int getValue(int num) {
        if (num == 33) {
            return hi.getValue();
        } else if (num == 34) {
            return lo.getValue();
        } else
            return regFile[num].getValue();

    }

    /**
     * For getting the number representation of the register.
     *
     * @param n The string formatted register name to look for.
     * @return The number of the register represented by the string
     * or -1 if no match.
     **/

    public static int getNumber(String n) {
        int j = -1;
        for (int i = 0; i < regFile.length; i++) {
            if (regFile[i].getName().equals(n)) {
                j = regFile[i].getNumber();
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
        return regFile;
    }

    /**
     * Get register object corresponding to given name.  If no match, return null.
     *
     * @param Rname The register name, either in $0 or $zero format.
     * @return The register object,or null if not found.
     **/

    public static Register getUserRegister(String Rname) {
        Register reg = null;
        if (Rname.charAt(0) == '$') {
            try {
                // check for register number 0-31.
                reg = regFile[Binary.stringToInt(Rname.substring(1))];    // KENV 1/6/05
            } catch (Exception e) {
                // handles both NumberFormat and ArrayIndexOutOfBounds
                // check for register mnemonic $zero thru $ra
                reg = null; // just to be sure
                // just do linear search; there aren't that many registers
                for (int i = 0; i < regFile.length; i++) {
                    if (Rname.equals(regFile[i].getName())) {
                        reg = regFile[i];
                        break;
                    }
                }
            }
        }
        return reg;
    }

    /**
     * For initializing the Program Counter.  Do not use this to implement jumps and
     * branches, as it will NOT record a backstep entry with the restore value.
     * If you need backstepping capability, use setProgramCounter instead.
     *
     * @param value The value to set the Program Counter to.
     **/

    public static void initializeProgramCounter(int value) {
        programCounter.setValue(value);
    }

    /**
     * Will initialize the Program Counter to either the default reset value, or the address
     * associated with source program global label "main", if it exists as a text segment label
     * and the global setting is set.
     *
     * @param startAtMain If true, will set program counter to address of statement labeled
     *                    'main' (or other defined start label) if defined.  If not defined, or if parameter false,
     *                    will set program counter to default reset value.
     **/

    public static void initializeProgramCounter(boolean startAtMain) {
        int mainAddr = Globals.symbolTable.getAddress(SymbolTable.getStartLabel());
        if (startAtMain && mainAddr != SymbolTable.NOT_FOUND && (Memory.inTextSegment(mainAddr) || Memory.inKernelTextSegment(mainAddr))) {
            initializeProgramCounter(mainAddr);
        } else {
            initializeProgramCounter(programCounter.getResetValue());
        }
    }

    /**
     * For setting the Program Counter.  Note that ordinary PC update should be done using
     * incrementPC() method. Use this only when processing jumps and branches.
     *
     * @param value The value to set the Program Counter to.
     * @return previous PC value
     **/

    public static int setProgramCounter(int value) {
        int old = programCounter.getValue();
        programCounter.setValue(value);
        if (Globals.getSettings().getBackSteppingEnabled()) {
            Globals.program.getBackStepper().addPCRestore(old);
        }
        return old;
    }

    /**
     * For returning the program counters value.
     *
     * @return The program counters value as an int.
     **/

    public static int getProgramCounter() {
        return programCounter.getValue();
    }

    /**
     * Returns Register object for program counter.  Use with caution.
     *
     * @return program counter's Register object.
     */
    public static Register getProgramCounterRegister() {
        return programCounter;
    }

    /**
     * For returning the program counter's initial (reset) value.
     *
     * @return The program counter's initial value
     **/

    public static int getInitialProgramCounter() {
        return programCounter.getResetValue();
    }

    /**
     * Method to reinitialize the values of the registers.
     * <b>NOTE:</b> Should <i>not</i> be called from command-mode MARS because this
     * this method uses global settings from the registry.  Command-mode must operate
     * using only the command switches, not registry settings.  It can be called
     * from tools running stand-alone, and this is done in
     * <code>AbstractMarsToolAndApplication</code>.
     **/

    public static void resetRegisters() {
        for (int i = 0; i < regFile.length; i++) {
            regFile[i].resetValue();
        }
        initializeProgramCounter(Globals.getSettings().getStartAtMain());// replaces "programCounter.resetValue()", DPS 3/3/09
        hi.resetValue();
        lo.resetValue();
    }

    /**
     * Method to increment the Program counter in the general case (not a jump or branch).
     **/

    public static void incrementPC() {
        programCounter.setValue(programCounter.getValue() + Instruction.INSTRUCTION_LENGTH);
    }

    /**
     * Each individual register is a separate object and Observable.  This handy method
     * will add the given Observer to each one.  Currently does not apply to Program
     * Counter.
     */
    public static void addRegistersObserver(Observer observer) {
        for (int i = 0; i < regFile.length; i++) {
            regFile[i].addObserver(observer);
        }
        hi.addObserver(observer);
        lo.addObserver(observer);
    }

    /**
     * Each individual register is a separate object and Observable.  This handy method
     * will delete the given Observer from each one.  Currently does not apply to Program
     * Counter.
     */
    public static void deleteRegistersObserver(Observer observer) {
        for (int i = 0; i < regFile.length; i++) {
            regFile[i].deleteObserver(observer);
        }
        hi.deleteObserver(observer);
        lo.deleteObserver(observer);
    }
}
