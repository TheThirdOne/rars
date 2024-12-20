package rars.riscv.syscalls;

import rars.ExitingException;
import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.RegisterFile;

import java.util.Random;

public class SyscallRandIntRange extends AbstractSyscall {
    public SyscallRandIntRange() {
        super("RandIntRange", "Get a random bounded integer", "a0 = index of pseudorandom number generator<br>a1 = upper bound for random number",
                "a0 = uniformly selectect from [0,bound]");
    }

    public void simulate(ProgramStatement statement) throws ExitingException {
        Random stream = RandomStreams.get("a0");
        try {
            RegisterFile.updateRegister("a0", stream.nextInt(RegisterFile.getValue("a1")));
        } catch (IllegalArgumentException iae) {
            throw new ExitingException(statement,
                    "Upper bound of range cannot be negative (syscall " + this.getNumber() + ")");
        }
    }
}
