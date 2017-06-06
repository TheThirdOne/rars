package mars.mips.instructions.instructions;

import mars.ProcessingException;
import mars.ProgramStatement;
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

public class SUB extends BasicInstruction {
    public SUB() {
        super("sub $t1,$t2,$t3", "Subtraction: set $t1 to ($t2 minus $t3)",
                BasicInstructionFormat.R_FORMAT, "0100000 ttttt sssss 000 fffff 0110011");
    }

    public void simulate(ProgramStatement statement) throws ProcessingException {
        int[] operands = statement.getOperands();
        int sub1 = RegisterFile.getValue(operands[1]);
        int sub2 = RegisterFile.getValue(operands[2]);
        int dif = sub1 - sub2;
        RegisterFile.updateRegister(operands[0], dif);
    }
}
