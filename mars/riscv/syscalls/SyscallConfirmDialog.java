package mars.riscv.syscalls;

import mars.ExitingException;
import mars.ProgramStatement;
import mars.riscv.hardware.RegisterFile;
import mars.riscv.AbstractSyscall;

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

/**
 * Service to display a message to user.
 * <p>
 * Input arguments: a0 = address of null-terminated string that is the message to user<br>
 * Output: a0 contains value of user-chosen option<br>
 * 0: Yes    <br>
 * 1: No     <br>
 * 2: Cancel <br>
 */

public class SyscallConfirmDialog extends AbstractSyscall {
    public SyscallConfirmDialog() {
        super("ConfirmDialog");
    }

    public void simulate(ProgramStatement statement) throws ExitingException {
        String message = NullString.get(statement);
        // update register a0 with the value from showConfirmDialog.
        // showConfirmDialog returns an int with one of three possible values:
        //    0 ---> meaning Yes
        //    1 ---> meaning No
        //    2 ---> meaning Cancel
        RegisterFile.updateRegister("a0", JOptionPane.showConfirmDialog(null, message));

    }

}
