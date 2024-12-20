package rars.riscv.syscalls;

import rars.ExitingException;
import rars.Globals;
import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.RegisterFile;
import rars.util.SystemIO;

/**
 * Service to write to file descriptor given in a0.  a1 specifies buffer
 * and a2 specifies length.  Number of characters written is returned in a0.
 */

public class SyscallWrite extends AbstractSyscall {
    public SyscallWrite() {
        super("Write", "Write to a filedescriptor from a buffer",
                "a0 = the file descriptor<br>a1 = the buffer address<br>a2 = the length to write",
                "a0 = the number of charcters written");
    }

    public void simulate(ProgramStatement statement) throws ExitingException {
        int byteAddress = RegisterFile.getValue("a1"); // source of characters to write to file
        int reqLength = RegisterFile.getValue("a2"); // user-requested length
        if (reqLength < 0) {
            RegisterFile.updateRegister("a0", -1);
            return;
        }
        int index = 0;
        byte myBuffer[] = new byte[reqLength];
        try {
            byte b = (byte) Globals.memory.getByte(byteAddress);
            while (index < reqLength) // Stop at requested length. Null bytes are included.
            {
                myBuffer[index++] = b;
                byteAddress++;
                b = (byte) Globals.memory.getByte(byteAddress);
            }
        } catch (AddressErrorException e) {
            throw new ExitingException(statement, e);
        }
        int retValue = SystemIO.writeToFile(
                RegisterFile.getValue("a0"), // fd
                myBuffer, // buffer
                RegisterFile.getValue("a2")); // length
        RegisterFile.updateRegister("a0", retValue); // set returned value in register
    }
}