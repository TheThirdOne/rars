package rars.riscv.syscalls;

import rars.ExitingException;
import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.AbstractSyscall;
import rars.util.SystemIO;

/**
 * Service to read the bits of console input double into $f0 and $f1.
 * $f1 contains high order word of the double.
 */

public class SyscallReadDouble extends AbstractSyscall {
    /**
     * Build an instance of the Read Double syscall.  Default service number
     * is 7 and name is "ReadDouble".
     */
    public SyscallReadDouble() {
        super("ReadDouble","Reads a double from input console", "N/A","fa0 = the double");
    }

    /**
     * Performs syscall function to read the bits of input double into $f0 and $f1.
     */
    public void simulate(ProgramStatement statement) throws SimulationException {
        double doubleValue = 0;
        try {
            doubleValue = SystemIO.readDouble(this.getNumber());
        } catch (NumberFormatException e) {
            throw new ExitingException(statement,
                    "invalid double input (syscall " + this.getNumber() + ")");
        }

        FloatingPointRegisterFile.updateRegisterLong(10, Double.doubleToRawLongBits(doubleValue));
    }
}