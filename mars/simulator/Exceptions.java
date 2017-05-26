package mars.simulator;

import mars.mips.hardware.Coprocessor0;
import mars.mips.hardware.RegisterFile;
import mars.mips.instructions.Instruction;
import mars.util.Binary;

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
 * Represents an error/interrupt that occurs during execution (simulation).
 *
 * @author Pete Sanderson
 * @version August 2005
 **/

public class Exceptions {
    /**
     * The exception number is stored in coprocessor 0 cause register ($13)
     * Note: the codes for External Interrupts have been modified from MIPS
     * specs in order to encode two pieces of information.  According
     * to spec, there is one External Interrupt code, 0.  But then
     * how to distinguish keyboard interrupt from display interrupt?
     * The Cause register has Interupt Pending bits that can be set.
     * Bit 8 represents keyboard, bit 9 represents display.  Those
     * bits are included into this code, but shifted right two positions
     * since the interrupt code will be shifted left two positions
     * for inserting cause code into bit positions 2-6 in Cause register.
     * DPS 23 July 2008.
     */
    public static final int EXTERNAL_INTERRUPT_KEYBOARD = 0x00000040; // see comment above.
    public static final int EXTERNAL_INTERRUPT_DISPLAY = 0x00000080; // see comment above.
    public static final int ADDRESS_EXCEPTION_LOAD = 4;
    public static final int ADDRESS_EXCEPTION_STORE = 5;
    public static final int SYSCALL_EXCEPTION = 8;
    public static final int BREAKPOINT_EXCEPTION = 9;
    public static final int RESERVED_INSTRUCTION_EXCEPTION = 10;
    public static final int ARITHMETIC_OVERFLOW_EXCEPTION = 12;
    public static final int TRAP_EXCEPTION = 13;
    /* the following are from SPIM */
    public static final int DIVIDE_BY_ZERO_EXCEPTION = 15;
    public static final int FLOATING_POINT_OVERFLOW = 16;
    public static final int FLOATING_POINT_UNDERFLOW = 17;

    /**
     * Given MIPS exception cause code, will place that code into
     * coprocessor 0 CAUSE register ($13), set the EPC register to
     * "current" program counter, and set Exception Level bit in STATUS register.
     *
     * @param cause The cause code (see Exceptions for a list)
     */
    public static void setRegisters(int cause) {
        // Set CAUSE register bits 2 thru 6 to cause value.  The "& 0xFFFFFC83" will set bits 2-6 and 8-9 to 0 while
        // keeping all the others.  Left-shift by 2 to put cause value into position then OR it in.  Bits 8-9 used to
        // identify devices for External Interrupt (8=keyboard,9=display).
        Coprocessor0.updateRegister(Coprocessor0.CAUSE, (Coprocessor0.getValue(Coprocessor0.CAUSE) & 0xFFFFFC83 | (cause << 2)));
        // When exception occurred, PC had already been incremented so need to subtract 4 here.
        Coprocessor0.updateRegister(Coprocessor0.EPC, RegisterFile.getProgramCounter() - Instruction.INSTRUCTION_LENGTH);
        // Set EXL (Exception Level) bit, bit position 1, in STATUS register to 1.
        Coprocessor0.updateRegister(Coprocessor0.STATUS, Binary.setBit(Coprocessor0.getValue(Coprocessor0.STATUS), Coprocessor0.EXCEPTION_LEVEL));
    }

    /**
     * Given MIPS exception cause code and bad address, place the bad address into VADDR
     * register ($8) then call overloaded setRegisters with the cause code to do the rest.
     *
     * @param cause The cause code (see Exceptions for a list). Should be address exception.
     * @param addr  The address that caused the exception.
     */
    public static void setRegisters(int cause, int addr) {
        Coprocessor0.updateRegister(Coprocessor0.VADDR, addr);
        setRegisters(cause);
    }

}  // Exceptions