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
 * Service to read console input string into buffer starting at address in $a0.
 */

public class SyscallReadString extends AbstractSyscall {
    /**
     * Build an instance of the Read String syscall.  Default service number
     * is 8 and name is "ReadString".
     */
    public SyscallReadString() {
        super(8, "ReadString");
    }

    /**
     * Performs syscall function to read console input string into buffer starting at address in $a0.
     * Follows semantics of UNIX 'fgets'.  For specified length n,
     * string can be no longer than n-1. If less than that, add
     * newline to end.  In either case, then pad with null byte.
     */
    public void simulate(ProgramStatement statement) throws ProcessingException {

        String inputString = "";
        int buf = RegisterFile.getValue(4); // buf addr in $a0
        int maxLength = RegisterFile.getValue(5) - 1; // $a1
        boolean addNullByte = true;
        // Guard against negative maxLength.  DPS 13-July-2011
        if (maxLength < 0) {
            maxLength = 0;
            addNullByte = false;
        }
        inputString = SystemIO.readString(this.getNumber(), maxLength);
        int stringLength = Math.min(maxLength, inputString.length());
        try {
            for (int index = 0; index < stringLength; index++) {
                Globals.memory.setByte(buf + index,
                        inputString.charAt(index));
            }
            if (stringLength < maxLength) {
                Globals.memory.setByte(buf + stringLength, '\n');
                stringLength++;
            }
            if (addNullByte) Globals.memory.setByte(buf + stringLength, 0);
        } catch (AddressErrorException e) {
            throw new ProcessingException(statement, e);
        }
    }
}