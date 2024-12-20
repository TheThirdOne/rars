package rars.riscv.syscalls;

import rars.ExitingException;
import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.RegisterFile;

import javax.swing.*;

public class SyscallConfirmDialog extends AbstractSyscall {
    public SyscallConfirmDialog() {
        super("ConfirmDialog", "Service to display a message to user",
                "a0 = address of null-terminated string that is the message to user",
                "a0 = Yes (0), No (1), or Cancel(2)");
    }

    public void simulate(ProgramStatement statement) throws ExitingException {
        String message = NullString.get(statement);
        int result = JOptionPane.showConfirmDialog(null, message);
        if (result == JOptionPane.CLOSED_OPTION) {
            result = JOptionPane.CANCEL_OPTION;
        }
        RegisterFile.updateRegister("a0", result);
    }
}
