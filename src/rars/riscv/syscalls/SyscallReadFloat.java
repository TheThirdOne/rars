package rars.riscv.syscalls;

import rars.ExitingException;
import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.util.SystemIO;

public class SyscallReadFloat extends AbstractSyscall {
    public SyscallReadFloat() {
        super("ReadFloat", "Reads a float from input console", "N/A", "fa0 = the float");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        float floatValue;
        try {
            floatValue = SystemIO.readFloat(this.getNumber());
        } catch (NumberFormatException e) {
            throw new ExitingException(statement,
                    "invalid float input (syscall " + this.getNumber() + ")");
        }
        FloatingPointRegisterFile.updateRegister(10, Float.floatToRawIntBits(floatValue)); // TODO: update to string fa0
    }
}