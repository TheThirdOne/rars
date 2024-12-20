package rars.riscv.syscalls;

import rars.ExitingException;
import rars.ProgramStatement;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.AbstractSyscall;

import javax.swing.*;

/**
 * Service to input data.
 * <p>
 * Input arguments: a0 = address of null-terminated string that is the message to user <br>
 * Outputs:<br>
 * f0 contains value of float read                           <br>
 * a1 contains status value                                  <br>
 * 0: valid input data, correctly parsed                   <br>
 * -1: input data cannot be correctly parsed               <br>
 * -2: Cancel was chosen                                   <br>
 * -3: OK was chosen but no data had been input into field <br>
 */

public class SyscallInputDialogFloat extends AbstractSyscall {
    public SyscallInputDialogFloat() {
        super("InputDialogFloat");
    }

    public void simulate(ProgramStatement statement) throws ExitingException {
        String message = NullString.get(statement);

        // Values returned by Java's InputDialog:
        // A null return value means that "Cancel" was chosen rather than OK.
        // An empty string returned (that is, inputValue.length() of zero)
        // means that OK was chosen but no string was input.
        String inputValue = null;
        inputValue = JOptionPane.showInputDialog(message);

        try {
            FloatingPointRegisterFile.setRegisterToFloat(0, (float) 0.0);  // set f0 to zero
            if (inputValue == null)  // Cancel was chosen
            {
                RegisterFile.updateRegister("a1", -2);
            } else if (inputValue.length() == 0)  // OK was chosen but there was no input
            {
                RegisterFile.updateRegister("a1", -3);
            } else {

                float floatValue = Float.parseFloat(inputValue);

                //System.out.println("SyscallInputDialogFloat: floatValue is " + floatValue);

                // Successful parse of valid input data
                FloatingPointRegisterFile.setRegisterToFloat(0, floatValue);  // set f0 to input data
                RegisterFile.updateRegister("a1", 0);  // set to valid flag

            }

        } catch (NumberFormatException e)    // Unsuccessful parse of input data
        {
            RegisterFile.updateRegister("a1", -1);
        }
    }
}
