package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.hardware.ControlAndStatusRegisterFile;
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
public class CSRRW extends BasicInstruction {
    public CSRRW() {
        super("csrrw t0, 0xFF, t1", "Atomic Read/Write CSR: read from the CSR into t0 and write t1 into the CSR",
                BasicInstructionFormat.I_FORMAT, "ssssssssssss ttttt 001 fffff 1110011");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        try {
            int csr = ControlAndStatusRegisterFile.getValue(operands[1]);
            ControlAndStatusRegisterFile.updateRegister(operands[1], RegisterFile.getValue(operands[2]));
            RegisterFile.updateRegister(operands[0], csr);
        } catch (NullPointerException e) {
            throw new SimulationException(statement, "Attempt to access unavailable CSR", SimulationException.ILLEGAL_INSTRUCTION);
        }
    }
}
