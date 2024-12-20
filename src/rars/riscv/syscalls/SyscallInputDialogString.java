package rars.riscv.syscalls;

import rars.ExitingException;
import rars.Globals;
import rars.ProgramStatement;
import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.AbstractSyscall;

import javax.swing.*;
import java.nio.charset.StandardCharsets;

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
        super("InputDialogString","Service to display a message to a user and request a string input",
                "a0 = address of null-terminated string that is the message to user<br>a1 = address of input buffer<br>"+
                        "a2 = maximum number of characters to read (including the terminating null)",
                "a1 contains status value.<br> 0: OK status. Buffer contains the input string.<br>-2: Cancel was chosen. No change to buffer.<br>"+
                        "-3: OK was chosen but no data had been input into field. No change to buffer.<br>-4: length of the input string exceeded the specified maximum. Buffer contains the maximum allowable input string terminated with null.");
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
                byte[] utf8BytesList = inputString.getBytes(StandardCharsets.UTF_8);
                // The buffer will contain characters, a '\n' character, and the null character
                // Copy the input data to buffer as space permits
                int stringLength = Math.min(maxLength-1, utf8BytesList.length);
                for (int index = 0; index < stringLength; index++) {
                    Globals.memory.setByte(byteAddress+ index,
                            utf8BytesList[index]);
                }
                if (stringLength < maxLength-1) {
                    Globals.memory.setByte(byteAddress + stringLength, '\n');
                    stringLength++;
                }
                Globals.memory.setByte(byteAddress + stringLength, 0);

                if (utf8BytesList.length > maxLength - 1) {
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
