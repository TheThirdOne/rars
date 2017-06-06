package mars.mips.instructions;

import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.RegisterFile;

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
public abstract class Load extends BasicInstruction {
    public Load(String usage, String description, String funct) {
        super(usage, description, BasicInstructionFormat.I_FORMAT,
                "ssssssssssss ttttt " + funct + " fffff 0000011");
    }

    public void simulate(ProgramStatement statement) throws ProcessingException {
        int[] operands = statement.getOperands();
        try {
            RegisterFile.updateRegister(operands[0],
                    load(RegisterFile.getValue(operands[2]) + operands[1]));
        } catch (AddressErrorException e) {
            throw new ProcessingException(statement, e);
        }
    }

    /**
     * @param address the address to load from
     * @return The value to store to the register
     */
    protected abstract int load(int address) throws AddressErrorException;
}