package rars.riscv.hardware;

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

/**
 * A register which aliases a subset of another register
 */
public class LinkedRegister extends Register {
    private Register base;
    private long mask;
    private int shift;

    /**
     * @param name the name to assign
     * @param num  the number to assign
     * @param base the register to alias
     * @param mask the bits to use
     */
    public LinkedRegister(String name, int num, Register base, long mask) {
        super(name, num, 0); // reset value does not matter
        this.base = base;
        this.mask = mask;

        // Find the lowest 1 bit
        shift = 0;
        while (mask != 0 && (mask & 1) == 0) {
            shift++;
            mask >>>= 1;
        }
    }

    public synchronized long getValue() {
        super.getValue(); // to notify observers
        return getValueNoNotify();
    }

    public synchronized long getValueNoNotify() {
        return (base.getValueNoNotify() & mask) >>> shift;
    }

    public synchronized long setValue(long val) {
        long old = base.getValueNoNotify();
        base.setValue(((val << shift) & mask) | (old & ~mask));
        super.setValue(0); //value doesn't matter just notify
        return (old & mask) >>> shift;
    }

    public synchronized void resetValue() {
        base.resetValue(); // not completely correct, but registers are only reset all together, so it doesn't matter that the other subsets are reset too
    }
}
