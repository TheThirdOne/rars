package rars.riscv.syscalls;

import rars.ExitingException;
import rars.ProgramStatement;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.AbstractSyscall;
import rars.util.SystemIO;

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
 * Service to read the bits of console input double into $f0 and $f1.
 * $f1 contains high order word of the double.
 */

public class SyscallReadDouble extends AbstractSyscall {
    /**
     * Build an instance of the Read Double syscall.  Default service number
     * is 7 and name is "ReadDouble".
     */
    public SyscallReadDouble() {
        super("ReadDouble","Reads a double from input console", "N/A","fa0 = the double");
    }

    /**
     * Performs syscall function to read the bits of input double into $f0 and $f1.
     */
    public void simulate(ProgramStatement statement) throws ExitingException {
        double doubleValue = 0;
        try {
            doubleValue = SystemIO.readDouble(this.getNumber());
        } catch (NumberFormatException e) {
            throw new ExitingException(statement,
                    "invalid double input (syscall " + this.getNumber() + ")");
        }
        if(statement.getCurrentHart() == -1)
            FloatingPointRegisterFile.updateRegisterLong(10, Double.doubleToRawLongBits(doubleValue));
        else
            FloatingPointRegisterFile.updateRegisterLong(10, Double.doubleToRawLongBits(doubleValue), statement.getCurrentHart());
    }
}