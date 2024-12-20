package rars.riscv.syscalls;

import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.util.SystemIO;

public class SyscallPrintFloat extends AbstractSyscall {
    public SyscallPrintFloat() {
        super("PrintFloat", "Prints a floating point number", "fa0 = float to print", "N/A");
    }

    public void simulate(ProgramStatement statement) {
        SystemIO.printString(Float.toString(Float.intBitsToFloat(
                FloatingPointRegisterFile.getValue("fa0"))));
    }
}