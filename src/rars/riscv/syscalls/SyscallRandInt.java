package rars.riscv.syscalls;

import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.RegisterFile;

import java.util.Random;

public class SyscallRandInt extends AbstractSyscall {
    public SyscallRandInt() {
        super("RandInt", "Get a random integer", "a0 = index of pseudorandom number generator", "a0 = random integer");
    }

    public void simulate(ProgramStatement statement) {
        Random stream = RandomStreams.get("a0");
        RegisterFile.updateRegister("a0", stream.nextInt());
    }
}

