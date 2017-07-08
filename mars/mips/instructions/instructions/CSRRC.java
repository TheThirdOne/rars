package mars.mips.instructions.instructions;

import mars.ProgramStatement;
import mars.mips.hardware.Coprocessor0;
import mars.mips.hardware.RegisterFile;
import mars.mips.instructions.BasicInstruction;
import mars.mips.instructions.BasicInstructionFormat;

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
public class CSRRC extends BasicInstruction {
    public CSRRC() {
        super("csrrc t0, t1, 0xFF", "Atomic Read/Clear CSR: read from the CSR into t0 and clear bits of the CSR according to t1",
                BasicInstructionFormat.I_FORMAT, "tttttttttttt sssss 011 fffff 1110011");
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        // TODO: throw error if the csr does not exist
        int csr = Coprocessor0.getValue(operands[2]);
        if (operands[1] != 0) Coprocessor0.clearRegister(operands[2], RegisterFile.getValue(operands[1]));
        RegisterFile.updateRegister(operands[0], csr);
    }
}
