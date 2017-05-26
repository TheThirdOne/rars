package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.RegisterFile;
import mars.simulator.Exceptions;
import mars.util.SystemIO;

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
 * Service to read a character from input console into $a0.
 */

public class SyscallReadChar extends AbstractSyscall {
    /**
     * Build an instance of the Read Char syscall.  Default service number
     * is 12 and name is "ReadChar".
     */
    public SyscallReadChar() {
        super(12, "ReadChar");
    }

    /**
     * Performs syscall function to read a character from input console into $a0
     */
    public void simulate(ProgramStatement statement) throws ProcessingException {
        int value = 0;
        try {
            value = SystemIO.readChar(this.getNumber());
        } catch (IndexOutOfBoundsException e) // means null input
        {
            throw new ProcessingException(statement,
                    "invalid char input (syscall " + this.getNumber() + ")",
                    Exceptions.SYSCALL_EXCEPTION);
        }
        // DPS 20 June 2008: changed from 4 ($a0) to 2 ($v0)
        RegisterFile.updateRegister(2, value);
    }

}