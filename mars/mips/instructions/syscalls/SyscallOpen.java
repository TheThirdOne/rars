package mars.mips.instructions.syscalls;

import mars.Globals;
import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.RegisterFile;
import mars.util.SystemIO;

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
 * Service to open file name specified by $a0. File descriptor returned in $v0.
 * (this was changed from $a0 in MARS 3.7 for SPIM compatibility.  The table
 * in COD erroneously shows $a0).
 */

public class SyscallOpen extends AbstractSyscall {
    /**
     * Build an instance of the Open file syscall.  Default service number
     * is 13 and name is "Open".
     */
    public SyscallOpen() {
        super(13, "Open");
    }

    /**
     * Performs syscall function to open file name specified by $a0. File descriptor returned
     * in $v0.  Only supported flags ($a1) are read-only (0), write-only (1) and
     * write-append (9). write-only flag creates file if it does not exist, so it is technically
     * write-create.  write-append will start writing at end of existing file.
     * Mode ($a2) is ignored.
     */
    public void simulate(ProgramStatement statement) throws ProcessingException {
        // NOTE: with MARS 3.7, return changed from $a0 to $v0 and the terminology
        // of 'flags' and 'mode' was corrected (they had been reversed).
        //
        // Arguments: $a0 = filename (string), $a1 = flags, $a2 = mode
        // Result: file descriptor (in $v0)
        // This code implements the flags:
        // Read          flag = 0
        // Write         flag = 1
        // Read/Write    NOT IMPLEMENTED
        // Write/append  flag = 9
        // This code implements the modes:
        // NO MODES IMPLEMENTED  -- MODE IS IGNORED
        // Returns in $v0: a "file descriptor" in the range 0 to SystemIO.SYSCALL_MAXFILES-1,
        // or -1 if error
        String filename = new String(); // = "";
        int byteAddress = RegisterFile.getValue(4);
        char ch[] = {' '}; // Need an array to convert to String
        try {
            ch[0] = (char) Globals.memory.getByte(byteAddress);
            while (ch[0] != 0) // only uses single location ch[0]
            {
                filename = filename.concat(new String(ch)); // parameter to String constructor is a char[] array
                byteAddress++;
                ch[0] = (char) Globals.memory.getByte(
                        byteAddress);
            }
        } catch (AddressErrorException e) {
            throw new ProcessingException(statement, e);
        }
        int retValue = SystemIO.openFile(filename,
                RegisterFile.getValue(5));
        RegisterFile.updateRegister(2, retValue); // set returned fd value in register

        // GETTING RID OF PROCESSING EXCEPTION.  IT IS THE RESPONSIBILITY OF THE
        // USER PROGRAM TO CHECK FOR BAD FILE OPEN.  MARS SHOULD NOT PRE-EMPTIVELY
        // TERMINATE MIPS EXECUTION BECAUSE OF IT.  Thanks to UCLA student
        // Duy Truong for pointing this out.  DPS 28-July-2009.
         /*
            if (retValue < 0) // some error in opening file
         {
            throw new ProcessingException(statement,
                SystemIO.getFileErrorMessage()+" (syscall "+this.getNumber()+")", 
					 Exceptions.SYSCALL_EXCEPTION);
         } 
			*/
    }
}