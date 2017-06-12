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
 * Service to read from file descriptor given in $a0.  $a1 specifies buffer
 * and $a2 specifies length.  Number of characters read is returned in $v0. <br>
 * <p>
 * Service Number: 14, Name: Read
 */

public class SyscallRead extends AbstractSyscall {
    public SyscallRead() {
        super(14, "Read");
    }

    public void simulate(ProgramStatement statement) throws ProcessingException {
        int byteAddress = RegisterFile.getValue("a1"); // destination of characters read from file
        int index = 0;
        int length = RegisterFile.getValue("a2");
        byte myBuffer[] = new byte[length]; // specified length
        // Call to SystemIO.xxxx.read(xxx,xxx,xxx)  returns actual length
        int retLength = SystemIO.readFromFile(
                RegisterFile.getValue("a0"), // fd
                myBuffer, // buffer
                length); // length
        RegisterFile.updateRegister("a0", retLength); // set returned value in register


        // Getting rid of processing exception.  It is the responsibility of the
        // user program to check the syscall's return value.  MARS should not
        // re-emptively terminate MIPS execution because of it.  Thanks to
        // UCLA student Duy Truong for pointing this out.  DPS 28-July-2009
         /*
         if (retLength < 0) // some error in opening file
         {
            throw new ProcessingException(statement,
                                    SystemIO.getFileErrorMessage()+" (syscall 14)",
                                    Exceptions.SYSCALL_EXCEPTION);
         }
			*/
        // copy bytes from returned buffer into MARS memory
        try {
            while (index < retLength) {
                Globals.memory.setByte(byteAddress++,
                        myBuffer[index++]);
            }
        } catch (AddressErrorException e) {
            throw new ProcessingException(statement, e);
        }
    }
}