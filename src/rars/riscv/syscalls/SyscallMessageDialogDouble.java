package rars.riscv.syscalls;

import rars.ExitingException;
import rars.Globals;
import rars.ProgramStatement;
import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.AbstractSyscall;

import javax.swing.*;

/**
 * Service to display a message to user.
 */

public class SyscallMessageDialogDouble extends AbstractSyscall {
    /**
     * Build an instance of the syscall with its default service number and name.
     */
    public SyscallMessageDialogDouble() {
        super("MessageDialogDouble", "Service to display message followed by a double",
                "a0 = address of null-terminated string that is the message to user <br> fa0 = the double","N/A");
    }

    /**
     * System call to display a message to user.
     */
    public void simulate(ProgramStatement statement) throws ExitingException {
        // TODO: maybe refactor this, other null strings are handled in a central place now
        String message = new String(); // = "";
        int byteAddress = RegisterFile.getValue("a0");
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

        JOptionPane.showMessageDialog(null,
                message + Double.longBitsToDouble(FloatingPointRegisterFile.getValueLong(10)),
                null,
                JOptionPane.INFORMATION_MESSAGE);
    }
}
