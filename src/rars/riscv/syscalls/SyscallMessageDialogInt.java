package rars.riscv.syscalls;

import rars.ExitingException;
import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.RegisterFile;

import javax.swing.*;

public class SyscallMessageDialogInt extends AbstractSyscall {
    public SyscallMessageDialogInt() {
        super("MessageDialogInt", "Service to display a message followed by a int to user",
                "a0 = address of null-terminated string that is the message to user <br>" +
                        "a1 = the int to display", "N/A");
    }

    public void simulate(ProgramStatement statement) throws ExitingException {
        String message = NullString.get(statement);

        // Display the dialog.
        JOptionPane.showMessageDialog(null,
                message + Integer.toString(RegisterFile.getValue("a1")),
                null,
                JOptionPane.INFORMATION_MESSAGE);
    }
}
