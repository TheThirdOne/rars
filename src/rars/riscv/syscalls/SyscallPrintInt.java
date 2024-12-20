package rars.riscv.syscalls;

import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.RegisterFile;
import rars.util.SystemIO;

public class SyscallPrintInt extends AbstractSyscall {
    public SyscallPrintInt() {
        super("PrintInt", "Prints an integer", "a0 = integer to print", "N/A");
    }

    public void simulate(ProgramStatement statement) {
        SystemIO.printString(Long.toString(RegisterFile.getValueLong("a0")));
    }
}