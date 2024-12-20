package rars.riscv.syscalls;

import rars.ExitingException;
import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.util.Binary;
import rars.util.SystemIO;

public class SyscallPrintDouble extends AbstractSyscall {
    /**
     * Build an instance of the Print Double syscall.  Default service number
     * is 3 and name is "PrintDouble".
     */
    public SyscallPrintDouble() {
        super("PrintDouble","Prints a double precision floating point number","fa0 = double to print","N/A");
    }

    /**
     * Performs syscall function to print double whose bits are stored in fa0
     */
    public void simulate(ProgramStatement statement) throws ExitingException {
        // Note: Higher numbered reg contains high order word so concat 13-12.
        SystemIO.printString(Double.toString(Double.longBitsToDouble(FloatingPointRegisterFile.getValueLong(10))));
    }
}