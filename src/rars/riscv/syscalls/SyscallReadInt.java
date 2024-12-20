package rars.riscv.syscalls;

import rars.*;
import rars.riscv.InstructionSet;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.RegisterFile;
import rars.util.SystemIO;

public class SyscallReadInt extends AbstractSyscall {
    public SyscallReadInt() {
        super("ReadInt", "Reads an int from input console", "N/A", "a0 = the int");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        try {
            if (InstructionSet.rv64) {
                RegisterFile.updateRegister("a0", SystemIO.readLong(this.getNumber()));
            } else {
                RegisterFile.updateRegister("a0", SystemIO.readInteger(this.getNumber()));
            }
        } catch (NumberFormatException e) {
            if (e.getMessage().equals("Cancel"))
                throw new CancelException();
            throw new ExitingException(statement,
                    "invalid integer input (syscall " + this.getNumber() + ")");
        }
    }

}