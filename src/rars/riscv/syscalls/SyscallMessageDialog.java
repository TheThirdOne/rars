package rars.riscv.syscalls;

import rars.ExitingException;
import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.RegisterFile;

import javax.swing.*;

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

public class SyscallMessageDialog extends AbstractSyscall {
    public SyscallMessageDialog() {
        super("MessageDialog", "Service to display a message to user",
                "a0 = address of null-terminated string that is the message to user <br>" +
                        "a1 = the type of the message to the user, which is one of:<br>"+
                        "0: error message <br>1: information message <br>2: warning message <br>3: question message <br>other: plain message", "N/A");
    }

    public void simulate(ProgramStatement statement) throws ExitingException {
        // Display the dialog.
        int hart = statement.getCurrentHart(), msgType;
        if(hart == -1)
            msgType = RegisterFile.getValue("a1");
        else
            msgType = RegisterFile.getValue("a1", hart);
        if (msgType < 0 || msgType > 3)
            msgType = -1; // See values in http://java.sun.com/j2se/1.5.0/docs/api/constant-values.html
        JOptionPane.showMessageDialog(null, NullString.get(statement), null, msgType);
    }
}
