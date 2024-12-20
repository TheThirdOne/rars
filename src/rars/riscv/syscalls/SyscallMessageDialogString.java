package rars.riscv.syscalls;

import rars.ExitingException;
import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;

import javax.swing.*;

public class SyscallMessageDialogString extends AbstractSyscall {
    public SyscallMessageDialogString() {
        super("MessageDialogString", "Service to display a message followed by a string to user",
                "a0 = address of null-terminated string that is the message to user <br>" +
                        "a1 = address of the second string to display", "N/A");
    }

    public void simulate(ProgramStatement statement) throws ExitingException {
        // Display the dialog.
        JOptionPane.showMessageDialog(null,
                NullString.get(statement) + NullString.get(statement, "a1"),
                null,
                JOptionPane.INFORMATION_MESSAGE);
    }
}
