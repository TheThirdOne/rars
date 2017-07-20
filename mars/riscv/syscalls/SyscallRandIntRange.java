package mars.riscv.syscalls;

import mars.ExitingException;
import mars.ProgramStatement;
import mars.riscv.hardware.RegisterFile;
import mars.riscv.AbstractSyscall;

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
 * System call to the random number generator.
 * <p>
 * Input arguments:<br>
 * a0 = index of pseudorandom number generator <br>
 * a1 = upper bound for random number <br>
 * Return: a0 = the next pseudorandom, uniformly distributed int value between 0 and the value in a1
 */

public class SyscallRandIntRange extends AbstractSyscall {
    public SyscallRandIntRange() {
        super("RandIntRange");
    }

    public void simulate(ProgramStatement statement) throws ExitingException {
        Random stream = RandomStreams.get("a0");
        try {
            RegisterFile.updateRegister("a0", stream.nextInt(RegisterFile.getValue("a1")));
        } catch (IllegalArgumentException iae) {
            throw new ExitingException(statement,
                    "Upper bound of range cannot be negative (syscall " + this.getNumber() + ")");
        }
    }
}
