package mars.riscv.instructions;

import mars.ProgramStatement;
import mars.riscv.hardware.ControlAndStatusRegisterFile;
import mars.riscv.hardware.FloatingPointRegisterFile;
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

/**
 * Base class for float to float operations
 *
 * @author Benjamin Landers
 * @version June 2017
 */
public abstract class Floating extends BasicInstruction {
    public static final String ROUNDING_MODE = "111";

    protected Floating(String name, String description, String funct) {
        this(name, description, funct, ROUNDING_MODE);
    }

    protected Floating(String name, String description, String funct, String rm) {
        super(name + " f1, f2, f3", description, BasicInstructionFormat.R_FORMAT, funct + "ttttt sssss " + rm + "fffff 1010011");
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        float result = compute(FloatingPointRegisterFile.getFloatFromRegister(operands[1]),
                FloatingPointRegisterFile.getFloatFromRegister(operands[2]));
        if (Float.isNaN(result)) {
            ControlAndStatusRegisterFile.orRegister("fcsr", 0x10); // Set invalid flag
        }
        if (Float.isInfinite(result)) {
            ControlAndStatusRegisterFile.orRegister("fcsr", 0x4); // Set Overflow flag
        }
        if (subnormal(result)) {
            ControlAndStatusRegisterFile.orRegister("fcsr", 0x2); // Set Underflow flag
        }
        FloatingPointRegisterFile.setRegisterToFloat(operands[0], result);
    }

    public abstract float compute(float f1, float f2);

    public static boolean subnormal(float f) {
        int bits = Float.floatToRawIntBits(f);
        return (bits & 0x7F800000) == 0 && (bits & 0x007FFFFF) > 0; // Exponent is minimum and the faction is non-zero
    }

}