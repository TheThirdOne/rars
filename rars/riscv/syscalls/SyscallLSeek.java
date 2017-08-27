package rars.riscv.syscalls;

import rars.ExitingException;
import rars.ProgramStatement;
import rars.riscv.AbstractSyscall;
import rars.riscv.hardware.RegisterFile;
import rars.util.SystemIO;

/*
Copyright (c) 2017, Benjamin Landers

Developed by Benjamin Landers (benjminrlanders@gmail.com)

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

public class SyscallLSeek extends AbstractSyscall {
    public SyscallLSeek() {
        super("LSeek", "Seek to a position in a file",
                "a0 = the file descriptor <br> a1 = the offset for the base <br>a2 is the begining of the file (0)," +
                        " the current position (1), or the end of the file (2)}",
                "a0 = the selected position from the beginning of the file or -1 is an error occurred");
    }

    public void simulate(ProgramStatement statement) throws ExitingException {
        int result = SystemIO.seek(RegisterFile.getValue("a0"),
                RegisterFile.getValue("a1"),
                RegisterFile.getValue("a2"));
        RegisterFile.updateRegister("a0", result);
    }
}