package rars.riscv.instructions;

import jsoftfloat.types.Float32;
import rars.ProgramStatement;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

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

public class FCLASSS extends BasicInstruction {
    public FCLASSS() {
        super("fclass.s t1, f1", "Classify a floating point number",
                BasicInstructionFormat.I_FORMAT, "1110000 00000 sssss 001 fffff 1010011");
    }

    /* Sets the one bit in t1 for every number
     * 0 t1 is −infinity.
     * 1 t1 is a negative normal number.
     * 2 t1 is a negative subnormal number.
     * 3 t1 is −0.
     * 4 t1 is +0.
     * 5 t1 is a positive subnormal number.
     * 6 t1 is a positive normal number.
     * 7 t1 is +infinity.
     * 8 t1 is a signaling NaN (Not implemented due to Java).
     * 9 t1 is a quiet NaN.
     */
    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        Float32 in = new Float32(FloatingPointRegisterFile.getValue(operands[1]));
        fclass(in,operands[0]);
    }

    public static <T extends jsoftfloat.types.Floating<T>> void fclass(T in, int out){
        if (in.isNaN()) {
            RegisterFile.updateRegister(out, in.isSignalling() ? 0x100 : 0x200);
        } else {
            boolean negative = in.isSignMinus();
            if (in.isInfinite()) {
                RegisterFile.updateRegister(out, negative ? 0x001 : 0x080);
            } else if (in.isZero()) {
                RegisterFile.updateRegister(out, negative ? 0x008 : 0x010);
            } else if (in.isSubnormal()) {
                RegisterFile.updateRegister(out, negative ? 0x004 : 0x020);
            } else {
                RegisterFile.updateRegister(out, negative ? 0x002 : 0x040);
            }
        }
    }
}