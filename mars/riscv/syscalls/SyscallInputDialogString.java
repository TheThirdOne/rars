package mars.riscv.syscalls;

import mars.ExitingException;
import mars.Globals;
import mars.ProgramStatement;
import mars.riscv.hardware.AddressErrorException;
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
 * Service to input data.
 * <p>
 * Input arguments:<br>
 * a0 = address of null-terminated string that is the message to user <br>
 * a1 = address of input buffer for the input string                  <br>
 * a2 = maximum number of characters to read                          <br>
 * Outputs:<br>
 * a1 contains status value                                   <br>
 * 0: valid input data, correctly parsed                   <br>
 * -1: input data cannot be correctly parsed               <br>
 * -2: Cancel was chosen                                   <br>
 * -3: OK was chosen but no data had been input into field <br>
 */

public class SyscallInputDialogString extends AbstractSyscall {
    public SyscallInputDialogString() {
        super("InputDialogString");
    }

    public void simulate(ProgramStatement statement) throws ExitingException {
        String message = NullString.get(statement);

        // Values returned by Java's InputDialog:
        // A null return value means that "Cancel" was chosen rather than OK.
        // An empty string returned (that is, inputString.length() of zero)
        // means that OK was chosen but no string was input.
        String inputString = null;
        inputString = JOptionPane.showInputDialog(message);
        int byteAddress = RegisterFile.getValue("a1"); // byteAddress of string is in a1
        int maxLength = RegisterFile.getValue("a2"); // input buffer size for input string is in a2

        try {
            if (inputString == null)  // Cancel was chosen
            {
                RegisterFile.updateRegister("a1", -2);
            } else if (inputString.length() == 0)  // OK was chosen but there was no input
            {
                RegisterFile.updateRegister("a1", -3);
            } else {
                // The buffer will contain characters, a '\n' character, and the null character
                // Copy the input data to buffer as space permits
                for (int index = 0; (index < inputString.length()) && (index < maxLength - 1); index++) {
                    Globals.memory.setByte(byteAddress + index,
                            inputString.charAt(index));
                }
                if (inputString.length() < maxLength - 1) {
                    Globals.memory.setByte(byteAddress + Math.min(inputString.length(), maxLength - 2), '\n');  // newline at string end
                }
                Globals.memory.setByte(byteAddress + Math.min((inputString.length() + 1), maxLength - 1), 0);  // null char to end string

                if (inputString.length() > maxLength - 1) {
                    //  length of the input string exceeded the specified maximum
                    RegisterFile.updateRegister("a1", -4);
                } else {
                    RegisterFile.updateRegister("a1", 0);
                }
            } // end else

        } // end try
        catch (AddressErrorException e) {
            throw new ExitingException(statement, e);
        }


    }

}
