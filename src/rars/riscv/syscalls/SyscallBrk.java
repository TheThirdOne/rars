package rars.riscv.syscalls;

import rars.ExitingException;
import rars.Globals;
import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.RegisterFile;

public class SyscallBrk extends AbstractSyscall {
    public SyscallBrk() {
        super("Brk", "Set and get heap address. Is compatible with the low level Linux brk syscall (not the POSIX one).", "a0 = wanted heap address, or 0 to get it", "a0 = resulting heap address");
    }

    public void simulate(ProgramStatement statement) {
        int val = RegisterFile.getValue("a0");
        if (val >= Globals.memory.heapBaseAddress && val < Globals.memory.dataSegmentLimitAddress ) {
            // no alignment, just a plain address to use now
            Globals.memory.heapAddress = val;
        }
        RegisterFile.updateRegister("a0", Globals.memory.heapAddress);
    }
}