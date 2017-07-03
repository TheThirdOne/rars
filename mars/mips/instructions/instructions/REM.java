package mars.mips.instructions.instructions;

import mars.mips.hardware.Coprocessor0;
import mars.mips.instructions.Arithmetic;

/*
Copyright (c) 2017,  Benjamin Landers

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


public class REM extends Arithmetic {
    public REM() {
        super("rem t1,t2,t3", "Remainder: set t1 to the remainder of t2/t3",
                "0000001", "110");
    }

    public int compute(int value, int value2) {
        if (value2 == 0) {
            Coprocessor0.orRegister("fcsr", 0x8); // Set Divide by Zero flag
            return value;
        }
        // Overflow
        if (value == Integer.MIN_VALUE && value2 == -1) {
            Coprocessor0.orRegister("fcsr", 0x4); // Set Overflow flag
            return 0;
        }
        return value % value2;
    }
}
