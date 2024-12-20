package rars.riscv.syscalls;

import rars.ExitingException;
import rars.Globals;
import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.RegisterFile;

public class SyscallSbrk extends AbstractSyscall {
    public SyscallSbrk() {
        super("Sbrk", "Allocate heap memory", "a0 = amount of memory in bytes", "a0 = address to the allocated block");
    }


    public void simulate(ProgramStatement statement) throws ExitingException {
        try {
            RegisterFile.updateRegister("a0", Globals.memory.allocateBytesFromHeap(RegisterFile.getValue("a0")));
        } catch (IllegalArgumentException iae) {
            throw new ExitingException(statement,
                    iae.getMessage() + " (syscall " + this.getNumber() + ")");
        }
    }
}