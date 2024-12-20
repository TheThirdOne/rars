package rars.riscv.syscalls;

import rars.ExitingException;
import rars.Globals;
import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.RegisterFile;
import rars.util.SystemIO;

public class SyscallRead extends AbstractSyscall {
    public SyscallRead() {
        super("Read", "Read from a file descriptor into a buffer",
                "a0 = the file descriptor <br>a1 = address of the buffer <br>a2 = maximum length to read",
                "a0 = the length read or -1 if error");
    }

    public void simulate(ProgramStatement statement) throws ExitingException {
        int byteAddress = RegisterFile.getValue("a1"); // destination of characters read from file
        int index = 0;
        int length = RegisterFile.getValue("a2");
        byte myBuffer[] = new byte[length]; // specified length
        // Call to SystemIO.xxxx.read(xxx,xxx,xxx)  returns actual length
        int retLength = SystemIO.readFromFile(
                RegisterFile.getValue("a0"), // fd
                myBuffer, // buffer
                length); // length
        RegisterFile.updateRegister("a0", retLength); // set returned value in register

        // copy bytes from returned buffer into memory
        try {
            while (index < retLength) {
                Globals.memory.setByte(byteAddress++,
                        myBuffer[index++]);
            }
        } catch (AddressErrorException e) {
            throw new ExitingException(statement, e);
        }
    }
}