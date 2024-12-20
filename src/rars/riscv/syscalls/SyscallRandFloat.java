package rars.riscv.syscalls;

import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.FloatingPointRegisterFile;

import java.util.Random;

public class SyscallRandFloat extends AbstractSyscall {
    public SyscallRandFloat() {
        super("RandFloat", "Get a random float", "a0 = index of pseudorandom number generator",
                "fa0 = uniformly randomly selected from from [0,1]");
    }

    public void simulate(ProgramStatement statement) {
        Random stream = RandomStreams.get("a0");
        FloatingPointRegisterFile.setRegisterToFloat(10, stream.nextFloat());// TODO: make this a string method fa0
    }
}