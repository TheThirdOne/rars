package mars.simulator;

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

    // Interrupts
    public static final int SOFTWARE_INTERRUPT = 0x80000000;
    public static final int TIMER_INTERRUPT = 0x80000004;
    public static final int EXTERNAL_INTERRUPT = 0x80000008;

    // Traps
    public static final int INSTRUCTION_ADDR_MISALIGNED = 0;
    public static final int INSTRUCTION_ACCESS_FAULT = 1;
    public static final int ILLEGAL_INSTRUCTION = 2;
    public static final int LOAD_ADDRESS_MISALIGNED = 4;
    public static final int LOAD_ACCESS_FAULT = 5;
    public static final int STORE_ADDRESS_MISALIGNED = 4;
    public static final int STORE_ACCESS_FAULT = 7;
    public static final int ENVIRONMENT_CALL = 8;
}  // Exceptions