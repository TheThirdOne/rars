package rars.riscv.syscalls;

import rars.ExitingException;
import rars.Globals;
import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;

public class SyscallExit extends AbstractSyscall {
    public SyscallExit() {
        super("Exit", "Exits the program with code 0");
    }

    public void simulate(ProgramStatement statement) throws ExitingException {
        Globals.exitCode = 0;
        throw new ExitingException();  // empty exception list.
    }
}
