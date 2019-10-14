package rars.riscv.instructions;

import rars.ProgramStatement;
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

public class FSGNJNS extends BasicInstruction {
    public FSGNJNS() {
        super("fsgnjn.s f1, f2, f3", "Floating point sign injection (inverted):  replace the sign bit of f2 with the opposite of sign bit of f3 and assign it to f1",
                BasicInstructionFormat.R_FORMAT, "0010000 ttttt sssss 001 fffff 1010011");
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        int result = (FloatingPointRegisterFile.getValue(operands[1]) & 0x7FFFFFFF) | ((~FloatingPointRegisterFile.getValue(operands[2])) & 0x80000000);
        FloatingPointRegisterFile.updateRegister(operands[0], result);
    }
}