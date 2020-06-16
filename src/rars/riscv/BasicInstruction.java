package rars.riscv;

/*
Copyright (c) 2003-2013,  Pete Sanderson and Kenneth Vollmar

Developed by Pete Sanderson (psanderson@otterbein.edu)
and Kenneth Vollmar (kenvollmar@missouristate.edu)

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

import rars.ProgramStatement;
import rars.SimulationException;

/**
 * Class to represent a basic instruction in the MIPS instruction set.
 * Basic instruction means it translates directly to a 32-bit binary machine
 * instruction.
 *
 * @author Pete Sanderson and Ken Vollmar
 * @version August 2003
 */
public abstract class BasicInstruction extends Instruction {

    private String instructionName;
    private BasicInstructionFormat instructionFormat;
    private String operationMask;

    private int opcodeMask;  // integer with 1's where constants required (0/1 become 1, f/s/t become 0)
    private int opcodeMatch; // integer matching constants required (0/1 become 0/1, f/s/t become 0)

    /**
     * BasicInstruction constructor.
     *
     * @param example     An example usage of the instruction, as a String.
     * @param instrFormat The format is R, I, I-branch or J.
     * @param operMask    The opcode mask is a 32 character string that contains the opcode in binary in the appropriate bit positions and codes for operand positions ('f', 's', 't') in the remainding positions.
     **/
    /* codes for operand positions are:
     * f == First operand
	 * s == Second operand
	 * t == Third operand
	 * example: "add rd,rs,rt" is R format with fields in this order: opcode, rs, rt, rd, shamt, funct.
	 *          Its opcode is 0, shamt is 0, funct is 0x40.  Based on operand order, its mask is
	 *          "000000ssssstttttfffff00000100000", split into
	 *          opcode |  rs   |  rt   |  rd   | shamt | funct
	 *          000000 | sssss | ttttt | fffff | 00000 | 100000
	 * This mask can be used at code generation time to map the assembly component to its
	 * correct bit positions in the binary machine instruction.
	 * It can also be used at runtime to match a binary machine instruction to the correct
	 * instruction simulator -- it needs to match all and only the 0's and 1's.
	 */
    public BasicInstruction(String example, String description, BasicInstructionFormat instrFormat,
                            String operMask) {
        this.exampleFormat = example;
        this.mnemonic = this.extractOperator(example);
        this.description = description;
        this.instructionFormat = instrFormat;
        this.operationMask = operMask.replaceAll(" ", ""); // squeeze out any/all spaces
        if (operationMask.length() != Instruction.INSTRUCTION_LENGTH_BITS) {
            System.out.println(example + " mask not " + Instruction.INSTRUCTION_LENGTH_BITS + " bits!");
        }

        this.opcodeMask = (int) Long.parseLong(this.operationMask.replaceAll("[01]", "1").replaceAll("[^01]", "0"), 2);
        this.opcodeMatch = (int) Long.parseLong(this.operationMask.replaceAll("[^1]", "0"), 2);
    }

    public BasicInstruction(String example, String description, BasicInstructionFormat instrFormat,
                            String operMask, boolean onlyinrv64) {
        this(example, description, instrFormat, operMask);
        if (InstructionSet.rv64 != onlyinrv64) {
            throw new NullPointerException("rv64");
        }
    }

    // Temporary constructor so that instructions without description yet will compile.

    public BasicInstruction(String example, BasicInstructionFormat instrFormat,
                            String operMask) {
        this(example, "", instrFormat, operMask);
    }

    /**
     * Gets the 32-character operation mask.  Each mask position represents a
     * bit position in the 32-bit machine instruction.  Operation codes and
     * unused bits are represented in the mask by 1's and 0's.  Operand codes
     * are represented by 'f', 's', and 't' for bits occupied by first, secon
     * and third operand, respectively.
     *
     * @return The 32 bit mask, as a String
     */
    public String getOperationMask() {
        return operationMask;
    }

    /**
     * Gets the operand format of the instruction.  MIPS defines 3 of these
     * R-format, I-format, and J-format.  R-format is all registers.  I-format
     * is address formed from register base with immediate offset.  J-format
     * is for jump destination addresses.  I have added one more:
     * I-branch-format, for branch destination addresses.  These are a variation
     * of the I-format in that the computed value is address relative to the
     * Program Counter.  All four formats are represented by static objects.
     *
     * @return The machine instruction format, R, I, J or I-branch.
     */
    public BasicInstructionFormat getInstructionFormat() {
        return instructionFormat;
    }

    public int getOpcodeMask() {
        return this.opcodeMask;
    }

    public int getOpcodeMatch() {
        return this.opcodeMatch;
    }

    /**
     * Method to simulate the execution of a specific MIPS basic instruction.
     *
     * @param statement A ProgramStatement representing the MIPS instruction to simulate.
     * @throws SimulationException This is a run-time exception generated during simulation.
     **/

    public abstract void simulate(ProgramStatement statement) throws SimulationException;
}
