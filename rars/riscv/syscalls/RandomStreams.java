package rars.riscv.syscalls;

import rars.riscv.hardware.RegisterFile;

import java.util.HashMap;
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
 * This small class serves only to hold a static HashMap for storing
 * random number generators for use by all the random number generator
 * syscalls.
 */

public class RandomStreams {
    /**
     * Collection of pseudorandom number streams available for use in Rand-type syscalls.
     * The streams are by default not seeded.
     */
    static final HashMap<Integer, Random> randomStreams = new HashMap<>();

    /**
     * Just a little helper method to initialize streams on stream being empty
     *
     * @param reg The name of the register that holds the stream index
     * @return the stream a that index
     */
    static Random get(String reg) {
        int index = RegisterFile.getValue(reg);
        Random stream = randomStreams.get(index);
        if (stream == null) {
            stream = new Random(); // create a non-seeded stream
            RandomStreams.randomStreams.put(index, stream);
        }
        return stream;
    }
}