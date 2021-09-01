package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.hardware.AddressErrorException;
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

/**
 * Base class for all Store instructions
 *
 * @author Benjamin Landers
 * @version June 2017
 */
public abstract class Store extends BasicInstruction {
    public Store(String usage, String description, String funct) {
        super(usage, description, BasicInstructionFormat.S_FORMAT,
                "sssssss fffff ttttt " + funct + " sssss 0100011");
    }
    public Store(String usage, String description, String funct, boolean rv64) {
        super(usage, description, BasicInstructionFormat.S_FORMAT,
                "sssssss fffff ttttt " + funct + " sssss 0100011", rv64);
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        int hart = statement.getCurrentHart();
        operands[1] = (operands[1] << 20) >> 20;
        try {
            int base = (hart == -1)
                    ? RegisterFile.getValue(operands[2])
                    : RegisterFile.getValue(operands[2], hart);
            long value = (hart == -1)
                    ? RegisterFile.getValueLong(operands[0])
                    : RegisterFile.getValueLong(operands[0], hart);
            store(base + operands[1], value, hart);
        } catch (AddressErrorException e) {
            throw new SimulationException(statement, e);
        }
    }

    /**
     * @param address the address to store to
     * @param value   the value to store
     * @param hart    the hart to store from
     */
    protected abstract void store(int address, long value, int hart) throws AddressErrorException;
}
