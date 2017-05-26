package mars.mips.instructions.syscalls;

import mars.Globals;
import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.RegisterFile;
import mars.mips.instructions.AbstractSyscall;
import mars.util.SystemIO;

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
 * Service to write to file descriptor given in $a0.  $a1 specifies buffer
 * and $a2 specifies length.  Number of characters written is returned in $v0
 * (this was changed from $a0 in MARS 3.7 for SPIM compatibility.  The table
 * in COD erroneously shows $a0).
 */

public class SyscallWrite extends AbstractSyscall {
    /**
     * Build an instance of the Write file syscall.  Default service number
     * is 15 and name is "Write".
     */
    public SyscallWrite() {
        super(15, "Write");
    }

    /**
     * Performs syscall function to write to file descriptor given in $a0.  $a1 specifies buffer
     * and $a2 specifies length.  Number of characters written is returned in $v0, starting in MARS 3.7.
     */
    public void simulate(ProgramStatement statement) throws ProcessingException {
        int byteAddress = RegisterFile.getValue(5); // source of characters to write to file
        byte b = 0;
        int reqLength = RegisterFile.getValue(6); // user-requested length
        int index = 0;
        byte myBuffer[] = new byte[RegisterFile.getValue(6) + 1]; // specified length plus null termination
        try {
            b = (byte) Globals.memory.getByte(byteAddress);
            while (index < reqLength) // Stop at requested length. Null bytes are included.
            // while (index < reqLength && b != 0) // Stop at requested length OR null byte
            {
                myBuffer[index++] = b;
                byteAddress++;
                b = (byte) Globals.memory.getByte(byteAddress);
            }

            myBuffer[index] = 0; // Add string termination
        } // end try
        catch (AddressErrorException e) {
            throw new ProcessingException(statement, e);
        }
        int retValue = SystemIO.writeToFile(
                RegisterFile.getValue(4), // fd
                myBuffer, // buffer
                RegisterFile.getValue(6)); // length
        RegisterFile.updateRegister(2, retValue); // set returned value in register

        // Getting rid of processing exception.  It is the responsibility of the
        // user program to check the syscall's return value.  MARS should not
        // re-emptively terminate MIPS execution because of it.  Thanks to
        // UCLA student Duy Truong for pointing this out.  DPS 28-July-2009
         /*
         if (retValue < 0) // some error in opening file
         {
            throw new ProcessingException(statement,
                                    SystemIO.getFileErrorMessage());
         }
			*/
    }
}