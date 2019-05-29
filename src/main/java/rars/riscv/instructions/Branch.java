package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;
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

/**
 * Base class for all branching instructions
 * <p>
 * Created mainly for making the branch simulator code simpler, but also does help with code reuse
 *
 * @author Benjamin Landers
 * @version June 2017
 */
public abstract class Branch extends BasicInstruction {
    public Branch(String usage, String description, String funct) {
        super(usage, description, BasicInstructionFormat.B_FORMAT,
                "ttttttt sssss fffff " + funct + " ttttt 1100011 ");
    }

    public void simulate(ProgramStatement statement) {
        if (willBranch(statement)) {
            InstructionSet.processBranch(statement.getOperands()[2]);
        }
    }

    /**
     * @param statement the program statement that carries the operands for this instruction
     * @return true if the Branch instruction will branch
     */
    public abstract boolean willBranch(ProgramStatement statement);
}