package rars.riscv.syscalls;

import rars.ExitingException;
import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.FloatingPointRegisterFile;

import javax.swing.*;

public class SyscallMessageDialogFloat extends AbstractSyscall {
    public SyscallMessageDialogFloat() {
        super("MessageDialogFloat", "Service to display a message followed by a float to user",
                "a0 = address of null-terminated string that is the message to user <br>" +
                        "fa1 = the float to display", "N/A");
    }

    public void simulate(ProgramStatement statement) throws ExitingException {
        String message = NullString.get(statement);

        // Display the dialog.
        JOptionPane.showMessageDialog(null,
                message + Float.toString(FloatingPointRegisterFile.getFloatFromRegister("fa1")),
                null,
                JOptionPane.INFORMATION_MESSAGE);
    }
}
