package rars.riscv.syscalls;

import rars.ExitingException;
import rars.Globals;
import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.RegisterFile;

import java.nio.charset.StandardCharsets;

public class SyscallGetCWD extends AbstractSyscall {
    public SyscallGetCWD() {
        super("GetCWD", "Writes the path of the current working directory into a buffer",
                "a0 = the buffer to write into <br>a1 = the length of the buffer",
                "a0 = -1 if the path is longer than the buffer");
    }

    public void simulate(ProgramStatement statement) throws ExitingException {
        String path = System.getProperty("user.dir");
        int buf = RegisterFile.getValue("a0");
        int length = RegisterFile.getValue("a1");

        byte[] utf8BytesList = path.getBytes(StandardCharsets.UTF_8);
        if(length < utf8BytesList.length+1){
            // This should be -34 (ERANGE) for compatibility with spike, but until other syscalls are ready with compatable
            // error codes, lets keep internal consitency.
            RegisterFile.updateRegister("a0",-1);
            return;
        }
        try {
            for (int index = 0; index < utf8BytesList.length; index++) {
                Globals.memory.setByte(buf + index,
                        utf8BytesList[index]);
            }
            Globals.memory.setByte(buf + utf8BytesList.length, 0);
        } catch (AddressErrorException e) {
            throw new ExitingException(statement, e);
        }
    }
}