package rars.riscv.syscalls;

import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.InstructionSet;
import rars.riscv.hardware.RegisterFile;
import rars.util.Binary;
import rars.util.SystemIO;

public class SyscallPrintIntUnsigned extends AbstractSyscall {
    public SyscallPrintIntUnsigned() {
        super("PrintIntUnsigned", "Prints an integer (unsigned)", "a0 = integer to print", "N/A");
    }

    public void simulate(ProgramStatement statement) {
        if (InstructionSet.rv64) {
            SystemIO.printString(Long.toUnsignedString(RegisterFile.getValueLong("a0")));
        } else {
            SystemIO.printString(Integer.toUnsignedString(RegisterFile.getValue("a0")));
        }
    }
}