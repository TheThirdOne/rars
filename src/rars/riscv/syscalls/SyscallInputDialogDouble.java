package rars.riscv.syscalls;

import rars.Globals;
import rars.ExitingException;
import rars.ProgramStatement;
import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.AbstractSyscall;

import javax.swing.*;

/**
 * Service to input data.
 */
//TODO: Fill in desc, in and out for all input dialogs
public class SyscallInputDialogDouble extends AbstractSyscall {
    /**
     * Build an instance of the syscall with its default service number and name.
     */
    public SyscallInputDialogDouble() {
        super("InputDialogDouble");
    }

    /**
     * System call to input data.
     */
    public void simulate(ProgramStatement statement) throws ExitingException {
        // Input arguments: $a0 = address of null-terminated string that is the message to user
        // Outputs:
        //    fa0 value of double read. $f1 contains high order word of the double.
        //    $a1 contains status value
        //       0: valid input data, correctly parsed
        //       -1: input data cannot be correctly parsed
        //       -2: Cancel was chosen
        //       -3: OK was chosen but no data had been input into field


        String message = new String(); // = "";
        int byteAddress = RegisterFile.getValue(4);
        char ch[] = {' '}; // Need an array to convert to String
        try {
            ch[0] = (char) Globals.memory.getByte(byteAddress);
            while (ch[0] != 0) // only uses single location ch[0]
            {
                message = message.concat(new String(ch)); // parameter to String constructor is a char[] array
                byteAddress++;
                ch[0] = (char) Globals.memory.getByte(byteAddress);
            }
        } catch (AddressErrorException e) {
            throw new ExitingException(statement, e);
        }

        // Values returned by Java's InputDialog:
        // A null return value means that "Cancel" was chosen rather than OK.
        // An empty string returned (that is, inputValue.length() of zero)
        // means that OK was chosen but no string was input.
        String inputValue = null;
        inputValue = JOptionPane.showInputDialog(message);

        try {
            FloatingPointRegisterFile.updateRegisterLong(0, 0); // set $f0 to zero
            if (inputValue == null)  // Cancel was chosen
            {
                RegisterFile.updateRegister("a1", -2);  // set $a1 to -2 flag
            } else if (inputValue.length() == 0)  // OK was chosen but there was no input
            {
                RegisterFile.updateRegister("a1", -3);  // set $a1 to -3 flag
            } else {
                double doubleValue = Double.parseDouble(inputValue);

                // Successful parse of valid input data
                FloatingPointRegisterFile.updateRegisterLong(10, Double.doubleToRawLongBits(doubleValue));
                RegisterFile.updateRegister("a1", 0);  // set $a1 to valid flag

            }
        }catch (NumberFormatException e)    // Unsuccessful parse of input data
        {
            RegisterFile.updateRegister("a1", -1);  // set $a1 to -1 flag
        }
    }
}
