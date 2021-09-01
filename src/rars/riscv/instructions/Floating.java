package rars.riscv.instructions;

import jsoftfloat.Flags;
import jsoftfloat.RoundingMode;
import jsoftfloat.types.Float32;
import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.hardware.ControlAndStatusRegisterFile;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;
import jsoftfloat.Environment;

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
 * Base class for float to float operations
 *
 * @author Benjamin Landers
 * @version June 2017
 */
public abstract class Floating extends BasicInstruction {
    protected Floating(String name, String description, String funct) {
        super(name + " f1, f2, f3, dyn", description, BasicInstructionFormat.R_FORMAT, funct + "ttttt sssss qqq fffff 1010011");
    }

    protected Floating(String name, String description, String funct, String rm) {
        super(name + " f1, f2, f3", description, BasicInstructionFormat.R_FORMAT, funct + "ttttt sssss " + rm + " fffff 1010011");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        int hart = statement.getCurrentHart();
        Environment e = new Environment();
        e.mode = getRoundingMode(operands[3], statement);
        Float32 result = compute(
                new Float32((hart == -1)
                        ? FloatingPointRegisterFile.getValue(operands[1])
                        : FloatingPointRegisterFile.getValue(operands[1], hart)
                    ),
                new Float32((hart == -1)
                        ? FloatingPointRegisterFile.getValue(operands[2])
                        : FloatingPointRegisterFile.getValue(operands[2], hart)
                    ),
                e);
        setfflags(e, hart);
        if (hart == -1)
            FloatingPointRegisterFile.updateRegister(operands[0], result.bits);
        else
            FloatingPointRegisterFile.updateRegister(operands[0], result.bits, hart);
    }

    public static void setfflags(Environment e, int hart){
        int fflags =(e.flags.contains(Flags.inexact)?1:0)+
                (e.flags.contains(Flags.underflow)?2:0)+
                (e.flags.contains(Flags.overflow)?4:0)+
                (e.flags.contains(Flags.divByZero)?8:0)+
                (e.flags.contains(Flags.invalid)?16:0);
        if (fflags != 0)
            if (hart == -1)
                ControlAndStatusRegisterFile.orRegister("fflags", fflags);
            else
                ControlAndStatusRegisterFile.orRegister("fflags", fflags, hart);
    }

    public static RoundingMode getRoundingMode(int RM, ProgramStatement statement) throws SimulationException {
        int rm = RM;
        int hart = statement.getCurrentHart();
        int frm = (hart == -1)
                ? ControlAndStatusRegisterFile.getValue("frm")
                : ControlAndStatusRegisterFile.getValue("frm", hart);
        if (rm == 7) rm = frm;
        switch (rm){
            case 0: // RNE
                return RoundingMode.even;
            case 1: // RTZ
                return RoundingMode.zero;
            case 2: // RDN
                return RoundingMode.min;
            case 3: // RUP
                return RoundingMode.max;
            case 4: // RMM
                return RoundingMode.away;
            default:
                throw new SimulationException(statement,"Invalid rounding mode. RM = " + RM +" and frm = " + frm);
        }
    }

    public abstract Float32 compute(Float32 f1, Float32 f2, Environment e);

    public static Float32 getFloat(int num) {
        return new Float32(FloatingPointRegisterFile.getValue(num));
    }

    public static Float32 getFloat(int num, int hart) {
        return new Float32(FloatingPointRegisterFile.getValue(num, hart));
    }
}
