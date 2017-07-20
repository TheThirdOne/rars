package mars.riscv.instructions;

import mars.ProgramStatement;
import mars.assembler.DataTypes;
import mars.riscv.hardware.Coprocessor0;
import mars.riscv.hardware.Coprocessor1;
import mars.riscv.hardware.RegisterFile;
import mars.riscv.BasicInstruction;
import mars.riscv.BasicInstructionFormat;

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

public class FCVTWS extends BasicInstruction {
    public FCVTWS() {
        super("fcvt.w.s t1, f1", "Convert integer from float: Assigns the value of f1 (rounded) to t1",
                BasicInstructionFormat.I_FORMAT, "1100000 00000 sssss " + Floating.ROUNDING_MODE + " fffff 1010011");
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        float in = Coprocessor1.getFloatFromRegister(operands[1]);
        if (Float.isNaN(in) || in > DataTypes.MAX_WORD_VALUE) {
            Coprocessor0.orRegister("fcsr", 0x10); // Set invalid flag
            RegisterFile.updateRegister(operands[0], DataTypes.MAX_WORD_VALUE);
        } else if (in < DataTypes.MIN_WORD_VALUE) {
            Coprocessor0.orRegister("fcsr", 0x10); // Set invalid flag
            RegisterFile.updateRegister(operands[0], DataTypes.MIN_WORD_VALUE);
        } else {
            RegisterFile.updateRegister(operands[0], Math.round(in));
        }

    }
}

