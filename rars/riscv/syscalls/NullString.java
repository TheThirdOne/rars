package rars.riscv.syscalls;

import rars.ExitingException;
import rars.Globals;
import rars.ProgramStatement;
import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.RegisterFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/*
Copyright (c) 2003-2017,  Pete Sanderson,Benjamin Landers and Kenneth Vollmar

Developed by Pete Sanderson (psanderson@otterbein.edu),
Benjamin Landers (benjaminrlanders@gmail.com),
and Kenneth Vollmar (kenvollmar@missouristate.edu)

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject
to the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(MIT license, http://www.opensource.org/licenses/mit-license.html)
 */

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
