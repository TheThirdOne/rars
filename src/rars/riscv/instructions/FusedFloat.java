package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.RoundingMode;
import jsoftfloat.types.Float32;
import rars.ProgramStatement;
import rars.SimulationException;
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

/**
 * Helper class for 4 argument floating point instructions
 */
public abstract class FusedFloat extends BasicInstruction {
    public FusedFloat(String usage, String description, String op) {
        super(usage+", dyn", description, BasicInstructionFormat.R4_FORMAT,
                "qqqqq 00 ttttt sssss " + "ppp" + " fffff 100" + op + "11");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        int hart = statement.getCurrentHart();
        Environment e = new Environment();
        e.mode = Floating.getRoundingMode(operands[4],statement);
        Float32 result = compute(new Float32((hart == -1)
                ? FloatingPointRegisterFile.getValue(operands[1])
                : FloatingPointRegisterFile.getValue(operands[1], hart)),
                new Float32((hart == -1)
                    ? FloatingPointRegisterFile.getValue(operands[2])
                    : FloatingPointRegisterFile.getValue(operands[2])),
                new Float32((hart == -1)
                    ? FloatingPointRegisterFile.getValue(operands[3])
                    : FloatingPointRegisterFile.getValue(operands[2])),
                    e);
        Floating.setfflags(e, hart);
        if (hart == 1)
            FloatingPointRegisterFile.updateRegister(operands[0],result.bits);
        else
            FloatingPointRegisterFile.updateRegisterLong(operands[0], result.bits, hart);
    }

    public static void flipRounding(Environment e){
        if(e.mode == RoundingMode.max){
            e.mode = RoundingMode.min;
        }else if(e.mode == RoundingMode.min){
            e.mode = RoundingMode.max;
        }
    }
    /**
     * @param r1 The first register
     * @param r2 The second register
     * @param r3 The third register
     * @return The value to store to the destination
     */
    protected abstract Float32 compute(Float32 r1, Float32 r2, Float32 r3,Environment e);
}
