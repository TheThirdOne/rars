package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.hardware.ControlAndStatusRegisterFile;
import rars.riscv.hardware.FloatingPointRegisterFile;
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

public class FSQRTS extends BasicInstruction {
    public FSQRTS() {
        super("fsqrt.s f1, f2, dyn", "Floating SQuare RooT: Assigns f1 to the square root of f2",
                BasicInstructionFormat.I_FORMAT, "0101100 00000 sssss ttt fffff 1010011");
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        float in = FloatingPointRegisterFile.getFloatFromRegister(operands[1]);
        if (in < 0.0f) {
            ControlAndStatusRegisterFile.orRegister("fcsr", 0x10); // Set invalid flag
            FloatingPointRegisterFile.setRegisterToFloat(operands[0], Float.NaN);
            return;
        }
        float result = (float) Math.sqrt(in);
        FloatingPointRegisterFile.setRegisterToFloat(operands[0], result);
    }
}