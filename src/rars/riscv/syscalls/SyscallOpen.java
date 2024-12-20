package rars.riscv.syscalls;

import rars.ExitingException;
import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.RegisterFile;
import rars.util.SystemIO;

public class SyscallOpen extends AbstractSyscall {
    public SyscallOpen() {
        super("Open", "Opens a file from a path <br>Only supported flags (a1) are read-only (0), write-only (1) and" +
                        " write-append (9). write-only flag creates file if it does not exist, so it is technically" +
                        " write-create.  write-append will start writing at end of existing file.",
                "a0 = Null terminated string for the path <br>a1 = flags", "a0 = the file decriptor or -1 if an error occurred");
    }

    public void simulate(ProgramStatement statement) throws ExitingException {
        int retValue = SystemIO.openFile(NullString.get(statement),
                RegisterFile.getValue("a1"));
        RegisterFile.updateRegister("a0", retValue); // set returned fd value in register
    }
}