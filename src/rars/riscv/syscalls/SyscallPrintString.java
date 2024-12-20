package rars.riscv.syscalls;

import rars.ExitingException;
import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.util.SystemIO;

public class SyscallPrintString extends AbstractSyscall {
    public SyscallPrintString() {
        super("PrintString", "Prints a null-terminated string to the console",
                "a0 = the address of the string", "N/A");
    }

    public void simulate(ProgramStatement statement) throws ExitingException {
        SystemIO.printString(NullString.get(statement));
    }
}