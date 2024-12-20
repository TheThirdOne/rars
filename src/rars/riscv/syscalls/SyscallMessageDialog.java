package rars.riscv.syscalls;

import rars.ExitingException;
import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.RegisterFile;

import javax.swing.*;

public class SyscallMessageDialog extends AbstractSyscall {
    public SyscallMessageDialog() {
        super("MessageDialog", "Service to display a message to user",
                "a0 = address of null-terminated string that is the message to user <br>" +
                        "a1 = the type of the message to the user, which is one of:<br>"+
                        "0: error message <br>1: information message <br>2: warning message <br>3: question message <br>other: plain message", "N/A");
    }

    public void simulate(ProgramStatement statement) throws ExitingException {
        // Display the dialog.
        int msgType = RegisterFile.getValue("a1");
        if (msgType < 0 || msgType > 3)
            msgType = -1; // See values in http://java.sun.com/j2se/1.5.0/docs/api/constant-values.html
        JOptionPane.showMessageDialog(null, NullString.get(statement), null, msgType);
    }
}
