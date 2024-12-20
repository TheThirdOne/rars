package rars.riscv.syscalls;

import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.InstructionSet;
import rars.riscv.hardware.RegisterFile;
import rars.util.Binary;
import rars.util.SystemIO;

public class SyscallPrintIntHex extends AbstractSyscall {
    public SyscallPrintIntHex() {
        super("PrintIntHex", "Prints an integer (in hexdecimal format left-padded with zeroes)", "a0 = integer to print", "N/A");
    }

    public void simulate(ProgramStatement statement) {
        if (InstructionSet.rv64) {
            SystemIO.printString(Binary.longToHexString(RegisterFile.getValueLong("a0")));
        } else {
            SystemIO.printString(Binary.intToHexString(RegisterFile.getValue("a0")));
        }
    }
}