package rars.riscv.instructions;

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


import java.math.BigInteger;

public class MULHU extends Arithmetic {
    public MULHU() {
        super("mulhu t1,t2,t3", "Multiplication: set t1 to the upper 32 bits of t2*t3 using unsigned multiplication",
                "0000001", "011");
    }
    public long compute(long value, long value2) {
        BigInteger unsigned = BigInteger.valueOf(value);
        if (value < 0) {
            unsigned = unsigned.add(BigInteger.ONE.shiftLeft(64));
        }
        BigInteger unsigned2 = BigInteger.valueOf(value2);
        if (value2 < 0) {
            unsigned2 = unsigned2.add(BigInteger.ONE.shiftLeft(64));
        }
        return unsigned.multiply(unsigned2).shiftRight(64).longValue();
    }
    public int computeW(int value, int value2) {
        // Don't sign extend both arguments
        long ext = ((long) value) & 0xFFFFFFFFL, ext2 = ((long) value2) & 0xFFFFFFFFL;
        // Return the top 32 bits of the mutliplication
        return (int) ((ext * ext2) >> 32);
    }
}
