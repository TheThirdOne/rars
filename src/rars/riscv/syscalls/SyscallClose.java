package rars.riscv.syscalls;

import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.RegisterFile;
import rars.util.SystemIO;

public class SyscallClose extends AbstractSyscall {
    public SyscallClose() {
        super("Close", "Close a file", "a0 = the file descriptor to close", "N/A");
    }

    public void simulate(ProgramStatement statement) {
        SystemIO.closeFile(RegisterFile.getValue("a0"));
    }
}