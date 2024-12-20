package rars.riscv.syscalls;

import rars.ExitingException;
import rars.ProgramStatement;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.AbstractSyscall;

import java.util.Random;

/**
 * Service to return a random floating point value.
 */

public class SyscallRandDouble extends AbstractSyscall {
    /**
     * Build an instance of the syscall with its default service number and name.
     */
    public SyscallRandDouble() {
        super( "RandDouble","Get a random double from the range 0.0-1.0",
                "a0 = index of pseudorandom number generator","fa0 = the next pseudorandom");
    }
    public void simulate(ProgramStatement statement) throws ExitingException {
        Integer index = RegisterFile.getValue("a0");
        Random stream = RandomStreams.randomStreams.get(index);
        if (stream == null) {
            stream = new Random(); // create a non-seeded stream
            RandomStreams.randomStreams.put(index, stream);
        }
        FloatingPointRegisterFile.updateRegisterLong(10, Double.doubleToRawLongBits(stream.nextDouble()));
    }
}
