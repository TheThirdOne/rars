package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;
import rars.riscv.Instruction;
import rars.riscv.InstructionSet;

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

public class JAL extends BasicInstruction {
    public JAL() {
        super("jal t1, target", "Jump and link : Set t1 to Program Counter (return address) then jump to statement at target address",
                BasicInstructionFormat.U_JUMP_FORMAT, "s ssssssssss s ssssssss fffff 1101111 ");
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        InstructionSet.processReturnAddress(operands[0]);// RegisterFile.updateRegister(31, RegisterFile.getProgramCounter());
        InstructionSet.processJump(RegisterFile.getProgramCounter() - Instruction.INSTRUCTION_LENGTH + (operands[1] << 1));
    }
}