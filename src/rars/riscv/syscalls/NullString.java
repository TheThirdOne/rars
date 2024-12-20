package rars.riscv.syscalls;

import rars.ExitingException;
import rars.Globals;
import rars.ProgramStatement;
import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.RegisterFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Small helper class to wrap getting null terminated strings from memory
 */
public class NullString {
    /**
     * Just a wrapper around #String get(ProgramStatement, String) which passes in the default "a0"
     */
    public static String get(ProgramStatement statement) throws ExitingException {
        return get(statement, "a0");
    }

    /**
     * Reads a NULL terminated string from memory starting at the address in reg
     *
     * @param statement the program statement this was called from (used for error handling)
     * @param reg       The name of the register for the address of the string
     * @return the string read from memory
     * @throws ExitingException if it hits a #AddressErrorException
     */
    public static String get(ProgramStatement statement, String reg) throws ExitingException {
        int byteAddress = RegisterFile.getValue(reg);
        ArrayList<Byte> utf8BytesList = new ArrayList<>(); // Need an array to hold bytes
        try {
            utf8BytesList.add((byte) Globals.memory.getByte(byteAddress));
            while (utf8BytesList.get(utf8BytesList.size() - 1) != 0) // until null terminator
            {
                byteAddress++;
                utf8BytesList.add((byte) Globals.memory.getByte(byteAddress));
            }
        } catch (AddressErrorException e) {
            throw new ExitingException(statement, e);
        }

        int size = utf8BytesList.size() - 1;    //size - 1 so we dont include the null terminator in the utf8Bytes array
        byte[] utf8Bytes = new byte[size];  
        for (int i = 0; i < size; i++){ 
            utf8Bytes[i] = utf8BytesList.get(i);
        }

        return new String(utf8Bytes, StandardCharsets.UTF_8);
    }
}
