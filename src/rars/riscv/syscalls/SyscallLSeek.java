package rars.riscv.syscalls;

import rars.ExitingException;
import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.RegisterFile;
import rars.util.SystemIO;

public class SyscallLSeek extends AbstractSyscall {
    public SyscallLSeek() {
        super("LSeek", "Seek to a position in a file",
                "a0 = the file descriptor <br> a1 = the offset for the base <br>a2 is the begining of the file (0)," +
                        " the current position (1), or the end of the file (2)}",
                "a0 = the selected position from the beginning of the file or -1 is an error occurred");
    }

    public void simulate(ProgramStatement statement) throws ExitingException {
        int result = SystemIO.seek(RegisterFile.getValue("a0"),
                RegisterFile.getValue("a1"),
                RegisterFile.getValue("a2"));
        RegisterFile.updateRegister("a0", result);
    }
}