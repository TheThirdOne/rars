package rars.riscv.syscalls;

import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.InstructionSet;
import rars.riscv.hardware.RegisterFile;
import rars.util.Binary;
import rars.util.SystemIO;

public class SyscallPrintIntBinary extends AbstractSyscall {
    public SyscallPrintIntBinary() {
        super("PrintIntBinary", "Prints an integer (in binary format left-padded with zeroes) ", "a0 = integer to print", "N/A");
    }

    public void simulate(ProgramStatement statement) {
        if (InstructionSet.rv64) {
            SystemIO.printString(Binary.longToBinaryString(RegisterFile.getValueLong("a0")));
        } else {
            SystemIO.printString(Binary.intToBinaryString(RegisterFile.getValue("a0")));
        }
    }
}