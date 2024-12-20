package rars.riscv.syscalls;

import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.RegisterFile;
import rars.util.Binary;

public class SyscallTime extends AbstractSyscall {
    public SyscallTime() {
        super("Time", "Get the current time (milliseconds since 1 January 1970)", "N/A", "a0 = low order 32 bits<br>a1=high order 32 bits");
    }

    public void simulate(ProgramStatement statement) {
        long value = new java.util.Date().getTime();
        RegisterFile.updateRegister("a0", Binary.lowOrderLongToInt(value));
        RegisterFile.updateRegister("a1", Binary.highOrderLongToInt(value));
    }

}