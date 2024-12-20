package rars.riscv.syscalls;

import rars.*;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.RegisterFile;
import rars.util.SystemIO;

import java.nio.charset.StandardCharsets;

/**
 * Service to read console input string into buffer starting at address in a0 for a1-1 bytes.
 * <p>
 * Performs syscall function to read console input string into buffer starting at address in $a0.
 * Follows semantics of UNIX 'fgets'.  For specified length n,
 * string can be no longer than n-1. If less than that, add
 * newline to end.  In either case, then pad with null byte.
 */
public class SyscallReadString extends AbstractSyscall {
    public SyscallReadString() {
        super("ReadString", "Reads a string from the console",
                "a0 = address of input buffer<br>a1 = size of the buffer", "N/A");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        String inputString = "";
        int buf = RegisterFile.getValue("a0"); // buf addr
        int maxLength = RegisterFile.getValue("a1") - 1;
        boolean addNullByte = true;
        // Guard against negative maxLength.  DPS 13-July-2011
        if (maxLength < 0) {
            maxLength = 0;
            addNullByte = false;
        }
        inputString = SystemIO.readString(this.getNumber(), maxLength);

        byte[] utf8BytesList = inputString.getBytes(StandardCharsets.UTF_8);
        // TODO: allow for utf-8 encoded strings
        int stringLength = Math.min(maxLength, utf8BytesList.length);
        try {
            for (int index = 0; index < stringLength; index++) {
                Globals.memory.setByte(buf + index,
                        utf8BytesList[index]);
            }
            if (stringLength < maxLength) {
                Globals.memory.setByte(buf + stringLength, '\n');
                stringLength++;
            }
            if (addNullByte) Globals.memory.setByte(buf + stringLength, 0);
        } catch (AddressErrorException e) {
            throw new ExitingException(statement, e);
        }
    }
}
