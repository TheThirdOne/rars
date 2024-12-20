package rars.riscv.syscalls;

import rars.*;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.RegisterFile;
import rars.util.SystemIO;

public class SyscallReadChar extends AbstractSyscall {
    public SyscallReadChar() {
        super("ReadChar", "Reads a character from input console", "N/A", "a0 = the character or -1 if end of input.");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int character = SystemIO.readChar(this.getNumber());
        if (character == SystemIO.NOTASCII) {
            throw new ExitingException(statement,
                    "invalid or non printable ASCII input (syscall " + this.getNumber() + ")");
        }
        RegisterFile.updateRegister("a0", character);
    }

}
