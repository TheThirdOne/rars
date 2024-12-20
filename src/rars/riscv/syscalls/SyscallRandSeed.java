package rars.riscv.syscalls;

import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.RegisterFile;

import java.util.Random;

public class SyscallRandSeed extends AbstractSyscall {
    public SyscallRandSeed() {
        super("RandSeed", "Set seed for the underlying Java pseudorandom number generator",
                "a0 = index of pseudorandom number generator<br>a1 = the seed", "N/A");
    }

    public void simulate(ProgramStatement statement) {
        Integer index = RegisterFile.getValue("a0");
        Random stream = RandomStreams.randomStreams.get(index);
        if (stream == null) {
            RandomStreams.randomStreams.put(index, new Random(RegisterFile.getValue("a1")));
        } else {
            stream.setSeed(RegisterFile.getValue("a1"));
        }
    }
}

