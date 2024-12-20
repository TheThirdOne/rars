package rars.riscv.syscalls;

import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.RegisterFile;
import rars.util.SystemIO;

import java.nio.charset.StandardCharsets;

public class SyscallPrintChar extends AbstractSyscall {
    public SyscallPrintChar() {
        super("PrintChar", "Prints an ascii character",
                "a0 = character to print (only lowest byte is considered)", "N/A");
    }

    public void simulate(ProgramStatement statement) {
        byte[] t = new byte[1];
        t[0] = (byte) RegisterFile.getValue("a0");
        SystemIO.printString(new String(t, StandardCharsets.UTF_8));
    }

}