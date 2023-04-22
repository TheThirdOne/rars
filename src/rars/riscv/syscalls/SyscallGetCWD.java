package rars.riscv.syscalls;

import rars.ExitingException;
import rars.Globals;
import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.RegisterFile;

import java.nio.charset.StandardCharsets;

/*
Copyright (c) 20017,  Benjamin Landers

Developed by Benjamin Landers (benjaminrlanders@gmail.com)

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