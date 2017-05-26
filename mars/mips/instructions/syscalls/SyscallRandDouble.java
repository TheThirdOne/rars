package mars.mips.instructions.syscalls;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.Coprocessor1;
import mars.mips.hardware.InvalidRegisterAccessException;
import mars.mips.hardware.RegisterFile;
import mars.simulator.Exceptions;

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
        super(44, "RandDouble");
    }

    /**
     * System call to the random number generator.
     * Return in $f0 the next pseudorandom, uniformly distributed double value between 0.0 and 1.0
     * from this random number generator's sequence.
     */
    public void simulate(ProgramStatement statement) throws ProcessingException {
        // Input arguments: $a0 = index of pseudorandom number generator
        // Return: $f0 = the next pseudorandom, uniformly distributed double value between 0.0 and 1.0
        // from this random number generator's sequence.
        Integer index = new Integer(RegisterFile.getValue(4));
        Random stream = (Random) RandomStreams.randomStreams.get(index);
        if (stream == null) {
            stream = new Random(); // create a non-seeded stream
            RandomStreams.randomStreams.put(index, stream);
        }
        try {
            Coprocessor1.setRegisterPairToDouble(0, stream.nextDouble());
        } catch (InvalidRegisterAccessException e) {   // register ID error in this method
            throw new ProcessingException(statement,
                    "Internal error storing double to register (syscall " + this.getNumber() + ")",
                    Exceptions.SYSCALL_EXCEPTION);
        }
    }

}
