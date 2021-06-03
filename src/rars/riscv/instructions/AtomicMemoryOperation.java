package rars.riscv.instructions;

import rars.Globals;
import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.hardware.AddressErrorException;
import rars.riscv.hardware.RegisterFile;

/*
Copyright (c) 2021, Giancarlo Pernudi Segura

Developed by Giancarlo Pernudi Segura at the University of Alberta (pernudi@ualberta.ca)

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
 * Base class for all Atomic instructions
 *
 * @author Giancarlo Pernudi Segura
 * @version May 2017
 */
public abstract class AtomicMemoryOperation extends Atomic {
    public AtomicMemoryOperation(String usage, String description, String funct3, String funct5) {
        super(usage, description, funct5, funct3);
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        try {
            int rs1Data = Globals.memory.getWord(RegisterFile.getValue(operands[1]));
            int rs2Value = RegisterFile.getValue(operands[2]);
            RegisterFile.updateRegister(operands[0], rs1Data);
            rs1Data = binaryOperation(rs1Data, rs2Value);
            Globals.memory.setWord(RegisterFile.getValue(operands[1]), rs1Data);
        } catch (AddressErrorException e) {
            throw new SimulationException(statement, e);
        }
    }

    protected abstract int binaryOperation(int value1, int value2);
}
