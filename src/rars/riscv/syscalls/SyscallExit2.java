package rars.riscv.syscalls;

import rars.ExitingException;
import rars.Globals;
import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.RegisterFile;

public class SyscallExit2 extends AbstractSyscall {
    public SyscallExit2() {
        super("Exit2", "Exits the program with a code", "a0 = the number to exit with", "N/A");
    }

    public void simulate(ProgramStatement statement) throws ExitingException {
        Globals.exitCode = RegisterFile.getValue("a0");
        throw new ExitingException(); // empty error list
    }
}