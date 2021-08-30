package rars.riscv.syscalls;

import rars.ExitingException;
import rars.ProgramStatement;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.AbstractSyscall;

import java.util.Random;

/*
Copyright (c) 2003-2008,  Pete Sanderson and Kenneth Vollmar

Developed by Pete Sanderson (psanderson@otterbein.edu)
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
 * Service to return a random floating point value.
 */

public class SyscallRandDouble extends AbstractSyscall {
    /**
     * Build an instance of the syscall with its default service number and name.
     */
    public SyscallRandDouble() {
        super( "RandDouble","Get a random double from the range 0.0-1.0",
                "a0 = index of pseudorandom number generator","fa0 = the next pseudorandom");
    }
    public void simulate(ProgramStatement statement) throws ExitingException {
        Integer index;
        if(statement.getCurrentHart() == -1)
            index = RegisterFile.getValue("a0");
        else
            index = RegisterFile.getValue("a0", statement.getCurrentHart());
        Random stream = RandomStreams.randomStreams.get(index);
        if (stream == null) {
            stream = new Random(); // create a non-seeded stream
            RandomStreams.randomStreams.put(index, stream);
        }
        if(statement.getCurrentHart() == -1)
            FloatingPointRegisterFile.updateRegisterLong(10, Double.doubleToRawLongBits(stream.nextDouble()));
        else
            FloatingPointRegisterFile.updateRegisterLong(10, Double.doubleToRawLongBits(stream.nextDouble()), statement.getCurrentHart());
    }
}
