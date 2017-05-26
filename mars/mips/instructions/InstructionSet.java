package mars.mips.instructions;

import mars.Globals;
import mars.ProcessingException;
import mars.ProgramStatement;
import mars.mips.hardware.AddressErrorException;
import mars.mips.hardware.Coprocessor0;
import mars.mips.hardware.Coprocessor1;
import mars.mips.hardware.RegisterFile;
import mars.simulator.DelayedBranch;
import mars.simulator.Exceptions;
import mars.util.Binary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

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

/**
 * The list of Instruction objects, each of which represents a MIPS instruction.
 * The instruction may either be basic (translates into binary machine code) or
 * extended (translates into sequence of one or more basic instructions).
 *
 * @author Pete Sanderson and Ken Vollmar
 * @version August 2003-5
 */

public class InstructionSet {
    private ArrayList<Instruction> instructionList;
    private ArrayList<MatchMap> opcodeMatchMaps;
    private SyscallLoader syscallLoader;

    /**
     * Creates a new InstructionSet object.
     */
    public InstructionSet() {
        instructionList = new ArrayList<>();

    }

    /**
     * Retrieve the current instruction set.
     */
    public ArrayList<Instruction> getInstructionList() {
        return instructionList;

    }

    /**
     * Adds all instructions to the set.  A given extended instruction may have
     * more than one Instruction object, depending on how many formats it can have.
     *
     * @see Instruction
     * @see BasicInstruction
     * @see ExtendedInstruction
     */
    public void populate() {
        /* Here is where the parade begins.  Every instruction is added to the set here.*/

        // ////////////////////////////////////   BASIC INSTRUCTIONS START HERE ////////////////////////////////

        instructionList.add(
                new BasicInstruction("nop",
                        "Null operation : machine code is all zeroes",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 00000 00000 00000 00000 000000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                // Hey I like this so far!
                            }
                        }));
        instructionList.add(
                new BasicInstruction("add $t1,$t2,$t3",
                        "Addition with overflow : set $t1 to ($t2 plus $t3)",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss ttttt fffff 00000 100000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int add1 = RegisterFile.getValue(operands[1]);
                                int add2 = RegisterFile.getValue(operands[2]);
                                int sum = add1 + add2;
                                // overflow on A+B detected when A and B have same sign and A+B has other sign.
                                if ((add1 >= 0 && add2 >= 0 && sum < 0)
                                        || (add1 < 0 && add2 < 0 && sum >= 0)) {
                                    throw new ProcessingException(statement,
                                            "arithmetic overflow", Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                                }
                                RegisterFile.updateRegister(operands[0], sum);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("sub $t1,$t2,$t3",
                        "Subtraction with overflow : set $t1 to ($t2 minus $t3)",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss ttttt fffff 00000 100010",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int sub1 = RegisterFile.getValue(operands[1]);
                                int sub2 = RegisterFile.getValue(operands[2]);
                                int dif = sub1 - sub2;
                                // overflow on A-B detected when A and B have opposite signs and A-B has B's sign
                                if ((sub1 >= 0 && sub2 < 0 && dif < 0)
                                        || (sub1 < 0 && sub2 >= 0 && dif >= 0)) {
                                    throw new ProcessingException(statement,
                                            "arithmetic overflow", Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                                }
                                RegisterFile.updateRegister(operands[0], dif);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("addi $t1,$t2,-100",
                        "Addition immediate with overflow : set $t1 to ($t2 plus signed 16-bit immediate)",
                        BasicInstructionFormat.I_FORMAT,
                        "001000 sssss fffff tttttttttttttttt",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int add1 = RegisterFile.getValue(operands[1]);
                                int add2 = operands[2] << 16 >> 16;
                                int sum = add1 + add2;
                                // overflow on A+B detected when A and B have same sign and A+B has other sign.
                                if ((add1 >= 0 && add2 >= 0 && sum < 0)
                                        || (add1 < 0 && add2 < 0 && sum >= 0)) {
                                    throw new ProcessingException(statement,
                                            "arithmetic overflow", Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                                }
                                RegisterFile.updateRegister(operands[0], sum);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("addu $t1,$t2,$t3",
                        "Addition unsigned without overflow : set $t1 to ($t2 plus $t3), no overflow",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss ttttt fffff 00000 100001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                RegisterFile.updateRegister(operands[0],
                                        RegisterFile.getValue(operands[1])
                                                + RegisterFile.getValue(operands[2]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("subu $t1,$t2,$t3",
                        "Subtraction unsigned without overflow : set $t1 to ($t2 minus $t3), no overflow",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss ttttt fffff 00000 100011",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                RegisterFile.updateRegister(operands[0],
                                        RegisterFile.getValue(operands[1])
                                                - RegisterFile.getValue(operands[2]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("addiu $t1,$t2,-100",
                        "Addition immediate unsigned without overflow : set $t1 to ($t2 plus signed 16-bit immediate), no overflow",
                        BasicInstructionFormat.I_FORMAT,
                        "001001 sssss fffff tttttttttttttttt",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                RegisterFile.updateRegister(operands[0],
                                        RegisterFile.getValue(operands[1])
                                                + (operands[2] << 16 >> 16));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("mult $t1,$t2",
                        "Multiplication : Set hi to high-order 32 bits, lo to low-order 32 bits of the product of $t1 and $t2 (use mfhi to access hi, mflo to access lo)",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff sssss 00000 00000 011000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                long product = (long) RegisterFile.getValue(operands[0])
                                        * (long) RegisterFile.getValue(operands[1]);
                                // Register 33 is HIGH and 34 is LOW
                                RegisterFile.updateRegister(33, (int) (product >> 32));
                                RegisterFile.updateRegister(34, (int) ((product << 32) >> 32));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("multu $t1,$t2",
                        "Multiplication unsigned : Set HI to high-order 32 bits, LO to low-order 32 bits of the product of unsigned $t1 and $t2 (use mfhi to access HI, mflo to access LO)",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff sssss 00000 00000 011001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                long product = (((long) RegisterFile.getValue(operands[0])) << 32 >>> 32)
                                        * (((long) RegisterFile.getValue(operands[1])) << 32 >>> 32);
                                // Register 33 is HIGH and 34 is LOW
                                RegisterFile.updateRegister(33, (int) (product >> 32));
                                RegisterFile.updateRegister(34, (int) ((product << 32) >> 32));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("mul $t1,$t2,$t3",
                        "Multiplication without overflow  : Set HI to high-order 32 bits, LO and $t1 to low-order 32 bits of the product of $t2 and $t3 (use mfhi to access HI, mflo to access LO)",
                        BasicInstructionFormat.R_FORMAT,
                        "011100 sssss ttttt fffff 00000 000010",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                long product = (long) RegisterFile.getValue(operands[1])
                                        * (long) RegisterFile.getValue(operands[2]);
                                RegisterFile.updateRegister(operands[0],
                                        (int) ((product << 32) >> 32));
                                // Register 33 is HIGH and 34 is LOW.  Not required by MIPS; SPIM does it.
                                RegisterFile.updateRegister(33, (int) (product >> 32));
                                RegisterFile.updateRegister(34, (int) ((product << 32) >> 32));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("madd $t1,$t2",
                        "Multiply add : Multiply $t1 by $t2 then increment HI by high-order 32 bits of product, increment LO by low-order 32 bits of product (use mfhi to access HI, mflo to access LO)",
                        BasicInstructionFormat.R_FORMAT,
                        "011100 fffff sssss 00000 00000 000000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                long product = (long) RegisterFile.getValue(operands[0])
                                        * (long) RegisterFile.getValue(operands[1]);
                                // Register 33 is HIGH and 34 is LOW.
                                long contentsHiLo = Binary.twoIntsToLong(
                                        RegisterFile.getValue(33), RegisterFile.getValue(34));
                                long sum = contentsHiLo + product;
                                RegisterFile.updateRegister(33, Binary.highOrderLongToInt(sum));
                                RegisterFile.updateRegister(34, Binary.lowOrderLongToInt(sum));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("maddu $t1,$t2",
                        "Multiply add unsigned : Multiply $t1 by $t2 then increment HI by high-order 32 bits of product, increment LO by low-order 32 bits of product, unsigned (use mfhi to access HI, mflo to access LO)",
                        BasicInstructionFormat.R_FORMAT,
                        "011100 fffff sssss 00000 00000 000001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                long product = (((long) RegisterFile.getValue(operands[0])) << 32 >>> 32)
                                        * (((long) RegisterFile.getValue(operands[1])) << 32 >>> 32);
                                // Register 33 is HIGH and 34 is LOW.
                                long contentsHiLo = Binary.twoIntsToLong(
                                        RegisterFile.getValue(33), RegisterFile.getValue(34));
                                long sum = contentsHiLo + product;
                                RegisterFile.updateRegister(33, Binary.highOrderLongToInt(sum));
                                RegisterFile.updateRegister(34, Binary.lowOrderLongToInt(sum));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("msub $t1,$t2",
                        "Multiply subtract : Multiply $t1 by $t2 then decrement HI by high-order 32 bits of product, decrement LO by low-order 32 bits of product (use mfhi to access HI, mflo to access LO)",
                        BasicInstructionFormat.R_FORMAT,
                        "011100 fffff sssss 00000 00000 000100",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                long product = (long) RegisterFile.getValue(operands[0])
                                        * (long) RegisterFile.getValue(operands[1]);
                                // Register 33 is HIGH and 34 is LOW.
                                long contentsHiLo = Binary.twoIntsToLong(
                                        RegisterFile.getValue(33), RegisterFile.getValue(34));
                                long diff = contentsHiLo - product;
                                RegisterFile.updateRegister(33, Binary.highOrderLongToInt(diff));
                                RegisterFile.updateRegister(34, Binary.lowOrderLongToInt(diff));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("msubu $t1,$t2",
                        "Multiply subtract unsigned : Multiply $t1 by $t2 then decrement HI by high-order 32 bits of product, decement LO by low-order 32 bits of product, unsigned (use mfhi to access HI, mflo to access LO)",
                        BasicInstructionFormat.R_FORMAT,
                        "011100 fffff sssss 00000 00000 000101",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                long product = (((long) RegisterFile.getValue(operands[0])) << 32 >>> 32)
                                        * (((long) RegisterFile.getValue(operands[1])) << 32 >>> 32);
                                // Register 33 is HIGH and 34 is LOW.
                                long contentsHiLo = Binary.twoIntsToLong(
                                        RegisterFile.getValue(33), RegisterFile.getValue(34));
                                long diff = contentsHiLo - product;
                                RegisterFile.updateRegister(33, Binary.highOrderLongToInt(diff));
                                RegisterFile.updateRegister(34, Binary.lowOrderLongToInt(diff));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("div $t1,$t2",
                        "Division with overflow : Divide $t1 by $t2 then set LO to quotient and HI to remainder (use mfhi to access HI, mflo to access LO)",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff sssss 00000 00000 011010",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[1]) == 0) {
                                    // Note: no exceptions and undefined results for zero div
                                    // COD3 Appendix A says "with overflow" but MIPS 32 instruction set
                                    // specification says "no arithmetic exception under any circumstances".
                                    return;
                                }

                                // Register 33 is HIGH and 34 is LOW
                                RegisterFile.updateRegister(33,
                                        RegisterFile.getValue(operands[0])
                                                % RegisterFile.getValue(operands[1]));
                                RegisterFile.updateRegister(34,
                                        RegisterFile.getValue(operands[0])
                                                / RegisterFile.getValue(operands[1]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("divu $t1,$t2",
                        "Division unsigned without overflow : Divide unsigned $t1 by $t2 then set LO to quotient and HI to remainder (use mfhi to access HI, mflo to access LO)",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff sssss 00000 00000 011011",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[1]) == 0) {
                                    // Note: no exceptions, and undefined results for zero divide
                                    return;
                                }
                                long oper1 = ((long) RegisterFile.getValue(operands[0])) << 32 >>> 32;
                                long oper2 = ((long) RegisterFile.getValue(operands[1])) << 32 >>> 32;
                                // Register 33 is HIGH and 34 is LOW
                                RegisterFile.updateRegister(33,
                                        (int) (((oper1 % oper2) << 32) >> 32));
                                RegisterFile.updateRegister(34,
                                        (int) (((oper1 / oper2) << 32) >> 32));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("mfhi $t1",
                        "Move from HI register : Set $t1 to contents of HI (see multiply and divide operations)",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 00000 00000 fffff 00000 010000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                RegisterFile.updateRegister(operands[0],
                                        RegisterFile.getValue(33));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("mflo $t1",
                        "Move from LO register : Set $t1 to contents of LO (see multiply and divide operations)",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 00000 00000 fffff 00000 010010",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                RegisterFile.updateRegister(operands[0],
                                        RegisterFile.getValue(34));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("mthi $t1",
                        "Move to HI registerr : Set HI to contents of $t1 (see multiply and divide operations)",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff 00000 00000 00000 010001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                RegisterFile.updateRegister(33,
                                        RegisterFile.getValue(operands[0]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("mtlo $t1",
                        "Move to LO register : Set LO to contents of $t1 (see multiply and divide operations)",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff 00000 00000 00000 010011",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                RegisterFile.updateRegister(34,
                                        RegisterFile.getValue(operands[0]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("and $t1,$t2,$t3",
                        "Bitwise AND : Set $t1 to bitwise AND of $t2 and $t3",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss ttttt fffff 00000 100100",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                RegisterFile.updateRegister(operands[0],
                                        RegisterFile.getValue(operands[1])
                                                & RegisterFile.getValue(operands[2]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("or $t1,$t2,$t3",
                        "Bitwise OR : Set $t1 to bitwise OR of $t2 and $t3",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss ttttt fffff 00000 100101",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                RegisterFile.updateRegister(operands[0],
                                        RegisterFile.getValue(operands[1])
                                                | RegisterFile.getValue(operands[2]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("andi $t1,$t2,100",
                        "Bitwise AND immediate : Set $t1 to bitwise AND of $t2 and zero-extended 16-bit immediate",
                        BasicInstructionFormat.I_FORMAT,
                        "001100 sssss fffff tttttttttttttttt",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                // ANDing with 0x0000FFFF zero-extends the immediate (high 16 bits always 0).
                                RegisterFile.updateRegister(operands[0],
                                        RegisterFile.getValue(operands[1])
                                                & (operands[2] & 0x0000FFFF));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("ori $t1,$t2,100",
                        "Bitwise OR immediate : Set $t1 to bitwise OR of $t2 and zero-extended 16-bit immediate",
                        BasicInstructionFormat.I_FORMAT,
                        "001101 sssss fffff tttttttttttttttt",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                // ANDing with 0x0000FFFF zero-extends the immediate (high 16 bits always 0).
                                RegisterFile.updateRegister(operands[0],
                                        RegisterFile.getValue(operands[1])
                                                | (operands[2] & 0x0000FFFF));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("nor $t1,$t2,$t3",
                        "Bitwise NOR : Set $t1 to bitwise NOR of $t2 and $t3",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss ttttt fffff 00000 100111",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                RegisterFile.updateRegister(operands[0],
                                        ~(RegisterFile.getValue(operands[1])
                                                | RegisterFile.getValue(operands[2])));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("xor $t1,$t2,$t3",
                        "Bitwise XOR (exclusive OR) : Set $t1 to bitwise XOR of $t2 and $t3",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss ttttt fffff 00000 100110",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                RegisterFile.updateRegister(operands[0],
                                        RegisterFile.getValue(operands[1])
                                                ^ RegisterFile.getValue(operands[2]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("xori $t1,$t2,100",
                        "Bitwise XOR immediate : Set $t1 to bitwise XOR of $t2 and zero-extended 16-bit immediate",
                        BasicInstructionFormat.I_FORMAT,
                        "001110 sssss fffff tttttttttttttttt",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                // ANDing with 0x0000FFFF zero-extends the immediate (high 16 bits always 0).
                                RegisterFile.updateRegister(operands[0],
                                        RegisterFile.getValue(operands[1])
                                                ^ (operands[2] & 0x0000FFFF));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("sll $t1,$t2,10",
                        "Shift left logical : Set $t1 to result of shifting $t2 left by number of bits specified by immediate",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 00000 sssss fffff ttttt 000000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                RegisterFile.updateRegister(operands[0],
                                        RegisterFile.getValue(operands[1]) << operands[2]);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("sllv $t1,$t2,$t3",
                        "Shift left logical variable : Set $t1 to result of shifting $t2 left by number of bits specified by value in low-order 5 bits of $t3",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 ttttt sssss fffff 00000 000100",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                // Mask all but low 5 bits of register containing shamt.
                                RegisterFile.updateRegister(operands[0],
                                        RegisterFile.getValue(operands[1]) <<
                                                (RegisterFile.getValue(operands[2]) & 0x0000001F));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("srl $t1,$t2,10",
                        "Shift right logical : Set $t1 to result of shifting $t2 right by number of bits specified by immediate",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 00000 sssss fffff ttttt 000010",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                // must zero-fill, so use ">>>" instead of ">>".
                                RegisterFile.updateRegister(operands[0],
                                        RegisterFile.getValue(operands[1]) >>> operands[2]);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("sra $t1,$t2,10",
                        "Shift right arithmetic : Set $t1 to result of sign-extended shifting $t2 right by number of bits specified by immediate",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 00000 sssss fffff ttttt 000011",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                // must sign-fill, so use ">>".
                                RegisterFile.updateRegister(operands[0],
                                        RegisterFile.getValue(operands[1]) >> operands[2]);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("srav $t1,$t2,$t3",
                        "Shift right arithmetic variable : Set $t1 to result of sign-extended shifting $t2 right by number of bits specified by value in low-order 5 bits of $t3",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 ttttt sssss fffff 00000 000111",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                // Mask all but low 5 bits of register containing shamt.Use ">>" to sign-fill.
                                RegisterFile.updateRegister(operands[0],
                                        RegisterFile.getValue(operands[1]) >>
                                                (RegisterFile.getValue(operands[2]) & 0x0000001F));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("srlv $t1,$t2,$t3",
                        "Shift right logical variable : Set $t1 to result of shifting $t2 right by number of bits specified by value in low-order 5 bits of $t3",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 ttttt sssss fffff 00000 000110",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                // Mask all but low 5 bits of register containing shamt.Use ">>>" to zero-fill.
                                RegisterFile.updateRegister(operands[0],
                                        RegisterFile.getValue(operands[1]) >>>
                                                (RegisterFile.getValue(operands[2]) & 0x0000001F));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("lw $t1,-100($t2)",
                        "Load word : Set $t1 to contents of effective memory word address",
                        BasicInstructionFormat.I_FORMAT,
                        "100011 ttttt fffff ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                try {
                                    RegisterFile.updateRegister(operands[0],
                                            Globals.memory.getWord(
                                                    RegisterFile.getValue(operands[2]) + operands[1]));
                                } catch (AddressErrorException e) {
                                    throw new ProcessingException(statement, e);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("ll $t1,-100($t2)",
                        "Load linked : Paired with Store Conditional (sc) to perform atomic read-modify-write.  Treated as equivalent to Load Word (lw) because MARS does not simulate multiple processors.",
                        BasicInstructionFormat.I_FORMAT,
                        "110000 ttttt fffff ssssssssssssssss",
                        // The ll (load link) command is supposed to be the front end of an atomic
                        // operation completed by sc (store conditional), with success or failure
                        // of the store depending on whether the memory block containing the
                        // loaded word is modified in the meantime by a different processor.
                        // Since MARS, like SPIM simulates only a single processor, the store
                        // conditional will always succeed so there is no need to do anything
                        // special here.  In that case, ll is same as lw.  And sc does the same
                        // thing as sw except in addition it writes 1 into the source register.
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                try {
                                    RegisterFile.updateRegister(operands[0],
                                            Globals.memory.getWord(
                                                    RegisterFile.getValue(operands[2]) + operands[1]));
                                } catch (AddressErrorException e) {
                                    throw new ProcessingException(statement, e);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("lwl $t1,-100($t2)",
                        "Load word left : Load from 1 to 4 bytes left-justified into $t1, starting with effective memory byte address and continuing through the low-order byte of its word",
                        BasicInstructionFormat.I_FORMAT,
                        "100010 ttttt fffff ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                try {
                                    int address = RegisterFile.getValue(operands[2]) + operands[1];
                                    int result = RegisterFile.getValue(operands[0]);
                                    for (int i = 0; i <= address % Globals.memory.WORD_LENGTH_BYTES; i++) {
                                        result = Binary.setByte(result, 3 - i, Globals.memory.getByte(address - i));
                                    }
                                    RegisterFile.updateRegister(operands[0], result);
                                } catch (AddressErrorException e) {
                                    throw new ProcessingException(statement, e);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("lwr $t1,-100($t2)",
                        "Load word right : Load from 1 to 4 bytes right-justified into $t1, starting with effective memory byte address and continuing through the high-order byte of its word",
                        BasicInstructionFormat.I_FORMAT,
                        "100110 ttttt fffff ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                try {
                                    int address = RegisterFile.getValue(operands[2]) + operands[1];
                                    int result = RegisterFile.getValue(operands[0]);
                                    for (int i = 0; i <= 3 - (address % Globals.memory.WORD_LENGTH_BYTES); i++) {
                                        result = Binary.setByte(result, i, Globals.memory.getByte(address + i));
                                    }
                                    RegisterFile.updateRegister(operands[0], result);
                                } catch (AddressErrorException e) {
                                    throw new ProcessingException(statement, e);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("sw $t1,-100($t2)",
                        "Store word : Store contents of $t1 into effective memory word address",
                        BasicInstructionFormat.I_FORMAT,
                        "101011 ttttt fffff ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                try {
                                    Globals.memory.setWord(
                                            RegisterFile.getValue(operands[2]) + operands[1],
                                            RegisterFile.getValue(operands[0]));
                                } catch (AddressErrorException e) {
                                    throw new ProcessingException(statement, e);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("sc $t1,-100($t2)",
                        "Store conditional : Paired with Load Linked (ll) to perform atomic read-modify-write.  Stores $t1 value into effective address, then sets $t1 to 1 for success.  Always succeeds because MARS does not simulate multiple processors.",
                        BasicInstructionFormat.I_FORMAT,
                        "111000 ttttt fffff ssssssssssssssss",
                        // See comments with "ll" instruction above.  "sc" is implemented
                        // like "sw", except that 1 is placed in the source register.
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                try {
                                    Globals.memory.setWord(
                                            RegisterFile.getValue(operands[2]) + operands[1],
                                            RegisterFile.getValue(operands[0]));
                                } catch (AddressErrorException e) {
                                    throw new ProcessingException(statement, e);
                                }
                                RegisterFile.updateRegister(operands[0], 1); // always succeeds
                            }
                        }));
        instructionList.add(
                new BasicInstruction("swl $t1,-100($t2)",
                        "Store word left : Store high-order 1 to 4 bytes of $t1 into memory, starting with effective byte address and continuing through the low-order byte of its word",
                        BasicInstructionFormat.I_FORMAT,
                        "101010 ttttt fffff ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                try {
                                    int address = RegisterFile.getValue(operands[2]) + operands[1];
                                    int source = RegisterFile.getValue(operands[0]);
                                    for (int i = 0; i <= address % Globals.memory.WORD_LENGTH_BYTES; i++) {
                                        Globals.memory.setByte(address - i, Binary.getByte(source, 3 - i));
                                    }
                                } catch (AddressErrorException e) {
                                    throw new ProcessingException(statement, e);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("swr $t1,-100($t2)",
                        "Store word right : Store low-order 1 to 4 bytes of $t1 into memory, starting with high-order byte of word containing effective byte address and continuing through that byte address",
                        BasicInstructionFormat.I_FORMAT,
                        "101110 ttttt fffff ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                try {
                                    int address = RegisterFile.getValue(operands[2]) + operands[1];
                                    int source = RegisterFile.getValue(operands[0]);
                                    for (int i = 0; i <= 3 - (address % Globals.memory.WORD_LENGTH_BYTES); i++) {
                                        Globals.memory.setByte(address + i, Binary.getByte(source, i));
                                    }
                                } catch (AddressErrorException e) {
                                    throw new ProcessingException(statement, e);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("lui $t1,100",
                        "Load upper immediate : Set high-order 16 bits of $t1 to 16-bit immediate and low-order 16 bits to 0",
                        BasicInstructionFormat.I_FORMAT,
                        "001111 00000 fffff ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                RegisterFile.updateRegister(operands[0], operands[1] << 16);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("beq $t1,$t2,label",
                        "Branch if equal : Branch to statement at label's address if $t1 and $t2 are equal",
                        BasicInstructionFormat.I_BRANCH_FORMAT,
                        "000100 fffff sssss tttttttttttttttt",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();

                                if (RegisterFile.getValue(operands[0])
                                        == RegisterFile.getValue(operands[1])) {
                                    processBranch(operands[2]);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("bne $t1,$t2,label",
                        "Branch if not equal : Branch to statement at label's address if $t1 and $t2 are not equal",
                        BasicInstructionFormat.I_BRANCH_FORMAT,
                        "000101 fffff sssss tttttttttttttttt",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[0])
                                        != RegisterFile.getValue(operands[1])) {
                                    processBranch(operands[2]);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("bgez $t1,label",
                        "Branch if greater than or equal to zero : Branch to statement at label's address if $t1 is greater than or equal to zero",
                        BasicInstructionFormat.I_BRANCH_FORMAT,
                        "000001 fffff 00001 ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[0]) >= 0) {
                                    processBranch(operands[1]);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("bgezal $t1,label",
                        "Branch if greater then or equal to zero and link : If $t1 is greater than or equal to zero, then set $ra to the Program Counter and branch to statement at label's address",
                        BasicInstructionFormat.I_BRANCH_FORMAT,
                        "000001 fffff 10001 ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[0]) >= 0) {  // the "and link" part
                                    processReturnAddress(31);//RegisterFile.updateRegister("$ra",RegisterFile.getProgramCounter());
                                    processBranch(operands[1]);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("bgtz $t1,label",
                        "Branch if greater than zero : Branch to statement at label's address if $t1 is greater than zero",
                        BasicInstructionFormat.I_BRANCH_FORMAT,
                        "000111 fffff 00000 ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[0]) > 0) {
                                    processBranch(operands[1]);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("blez $t1,label",
                        "Branch if less than or equal to zero : Branch to statement at label's address if $t1 is less than or equal to zero",
                        BasicInstructionFormat.I_BRANCH_FORMAT,
                        "000110 fffff 00000 ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[0]) <= 0) {
                                    processBranch(operands[1]);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("bltz $t1,label",
                        "Branch if less than zero : Branch to statement at label's address if $t1 is less than zero",
                        BasicInstructionFormat.I_BRANCH_FORMAT,
                        "000001 fffff 00000 ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[0]) < 0) {
                                    processBranch(operands[1]);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("bltzal $t1,label",
                        "Branch if less than zero and link : If $t1 is less than or equal to zero, then set $ra to the Program Counter and branch to statement at label's address",
                        BasicInstructionFormat.I_BRANCH_FORMAT,
                        "000001 fffff 10000 ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[0]) < 0) {  // the "and link" part
                                    processReturnAddress(31);//RegisterFile.updateRegister("$ra",RegisterFile.getProgramCounter());
                                    processBranch(operands[1]);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("slt $t1,$t2,$t3",
                        "Set less than : If $t2 is less than $t3, then set $t1 to 1 else set $t1 to 0",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss ttttt fffff 00000 101010",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                RegisterFile.updateRegister(operands[0],
                                        (RegisterFile.getValue(operands[1])
                                                < RegisterFile.getValue(operands[2]))
                                                ? 1
                                                : 0);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("sltu $t1,$t2,$t3",
                        "Set less than unsigned : If $t2 is less than $t3 using unsigned comparision, then set $t1 to 1 else set $t1 to 0",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss ttttt fffff 00000 101011",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int first = RegisterFile.getValue(operands[1]);
                                int second = RegisterFile.getValue(operands[2]);
                                if (first >= 0 && second >= 0 || first < 0 && second < 0) {
                                    RegisterFile.updateRegister(operands[0],
                                            (first < second) ? 1 : 0);
                                } else {
                                    RegisterFile.updateRegister(operands[0],
                                            (first >= 0) ? 1 : 0);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("slti $t1,$t2,-100",
                        "Set less than immediate : If $t2 is less than sign-extended 16-bit immediate, then set $t1 to 1 else set $t1 to 0",
                        BasicInstructionFormat.I_FORMAT,
                        "001010 sssss fffff tttttttttttttttt",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                // 16 bit immediate value in operands[2] is sign-extended
                                RegisterFile.updateRegister(operands[0],
                                        (RegisterFile.getValue(operands[1])
                                                < (operands[2] << 16 >> 16))
                                                ? 1
                                                : 0);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("sltiu $t1,$t2,-100",
                        "Set less than immediate unsigned : If $t2 is less than  sign-extended 16-bit immediate using unsigned comparison, then set $t1 to 1 else set $t1 to 0",
                        BasicInstructionFormat.I_FORMAT,
                        "001011 sssss fffff tttttttttttttttt",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int first = RegisterFile.getValue(operands[1]);
                                // 16 bit immediate value in operands[2] is sign-extended
                                int second = operands[2] << 16 >> 16;
                                if (first >= 0 && second >= 0 || first < 0 && second < 0) {
                                    RegisterFile.updateRegister(operands[0],
                                            (first < second) ? 1 : 0);
                                } else {
                                    RegisterFile.updateRegister(operands[0],
                                            (first >= 0) ? 1 : 0);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("movn $t1,$t2,$t3",
                        "Move conditional not zero : Set $t1 to $t2 if $t3 is not zero",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss ttttt fffff 00000 001011",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[2]) != 0)
                                    RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("movz $t1,$t2,$t3",
                        "Move conditional zero : Set $t1 to $t2 if $t3 is zero",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss ttttt fffff 00000 001010",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[2]) == 0)
                                    RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("movf $t1,$t2",
                        "Move if FP condition flag 0 false : Set $t1 to $t2 if FPU (Coprocessor 1) condition flag 0 is false (zero)",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss 000 00 fffff 00000 000001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (Coprocessor1.getConditionFlag(0) == 0)
                                    RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("movf $t1,$t2,1",
                        "Move if specified FP condition flag false : Set $t1 to $t2 if FPU (Coprocessor 1) condition flag specified by the immediate is false (zero)",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss ttt 00 fffff 00000 000001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (Coprocessor1.getConditionFlag(operands[2]) == 0)
                                    RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("movt $t1,$t2",
                        "Move if FP condition flag 0 true : Set $t1 to $t2 if FPU (Coprocessor 1) condition flag 0 is true (one)",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss 000 01 fffff 00000 000001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (Coprocessor1.getConditionFlag(0) == 1)
                                    RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("movt $t1,$t2,1",
                        "Move if specfied FP condition flag true : Set $t1 to $t2 if FPU (Coprocessor 1) condition flag specified by the immediate is true (one)",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss ttt 01 fffff 00000 000001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (Coprocessor1.getConditionFlag(operands[2]) == 1)
                                    RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("break 100",
                        "Break execution with code : Terminate program execution with specified exception code",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 ffffffffffffffffffff 001101",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {  // At this time I don't have exception processing or trap handlers
                                // so will just halt execution with a message.
                                int[] operands = statement.getOperands();
                                throw new ProcessingException(statement, "break instruction executed; code = " +
                                        operands[0] + ".", Exceptions.BREAKPOINT_EXCEPTION);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("break",
                        "Break execution : Terminate program execution with exception",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 00000 00000 00000 00000 001101",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {  // At this time I don't have exception processing or trap handlers
                                // so will just halt execution with a message.
                                throw new ProcessingException(statement, "break instruction executed; no code given.",
                                        Exceptions.BREAKPOINT_EXCEPTION);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("syscall",
                        "Issue a system call : Execute the system call specified by value in $v0",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 00000 00000 00000 00000 001100",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                findAndSimulateSyscall(RegisterFile.getValue(2), statement);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("j target",
                        "Jump unconditionally : Jump to statement at target address",
                        BasicInstructionFormat.J_FORMAT,
                        "000010 ffffffffffffffffffffffffff",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                processJump(
                                        ((RegisterFile.getProgramCounter() & 0xF0000000)
                                                | (operands[0] << 2)));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("jr $t1",
                        "Jump register unconditionally : Jump to statement whose address is in $t1",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff 00000 00000 00000 001000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                processJump(RegisterFile.getValue(operands[0]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("jal target",
                        "Jump and link : Set $ra to Program Counter (return address) then jump to statement at target address",
                        BasicInstructionFormat.J_FORMAT,
                        "000011 ffffffffffffffffffffffffff",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                processReturnAddress(31);// RegisterFile.updateRegister(31, RegisterFile.getProgramCounter());
                                processJump(
                                        (RegisterFile.getProgramCounter() & 0xF0000000)
                                                | (operands[0] << 2));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("jalr $t1,$t2",
                        "Jump and link register : Set $t1 to Program Counter (return address) then jump to statement whose address is in $t2",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss 00000 fffff 00000 001001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                processReturnAddress(operands[0]);//RegisterFile.updateRegister(operands[0], RegisterFile.getProgramCounter());
                                processJump(RegisterFile.getValue(operands[1]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("jalr $t1",
                        "Jump and link register : Set $ra to Program Counter (return address) then jump to statement whose address is in $t1",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff 00000 11111 00000 001001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                processReturnAddress(31);//RegisterFile.updateRegister(31, RegisterFile.getProgramCounter());
                                processJump(RegisterFile.getValue(operands[0]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("lb $t1,-100($t2)",
                        "Load byte : Set $t1 to sign-extended 8-bit value from effective memory byte address",
                        BasicInstructionFormat.I_FORMAT,
                        "100000 ttttt fffff ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                try {
                                    RegisterFile.updateRegister(operands[0],
                                            Globals.memory.getByte(
                                                    RegisterFile.getValue(operands[2])
                                                            + (operands[1] << 16 >> 16))
                                                    << 24
                                                    >> 24);
                                } catch (AddressErrorException e) {
                                    throw new ProcessingException(statement, e);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("lh $t1,-100($t2)",
                        "Load halfword : Set $t1 to sign-extended 16-bit value from effective memory halfword address",
                        BasicInstructionFormat.I_FORMAT,
                        "100001 ttttt fffff ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                try {
                                    RegisterFile.updateRegister(operands[0],
                                            Globals.memory.getHalf(
                                                    RegisterFile.getValue(operands[2])
                                                            + (operands[1] << 16 >> 16))
                                                    << 16
                                                    >> 16);
                                } catch (AddressErrorException e) {
                                    throw new ProcessingException(statement, e);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("lhu $t1,-100($t2)",
                        "Load halfword unsigned : Set $t1 to zero-extended 16-bit value from effective memory halfword address",
                        BasicInstructionFormat.I_FORMAT,
                        "100101 ttttt fffff ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                try {
                                    // offset is sign-extended and loaded halfword value is zero-extended
                                    RegisterFile.updateRegister(operands[0],
                                            Globals.memory.getHalf(
                                                    RegisterFile.getValue(operands[2])
                                                            + (operands[1] << 16 >> 16))
                                                    & 0x0000ffff);
                                } catch (AddressErrorException e) {
                                    throw new ProcessingException(statement, e);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("lbu $t1,-100($t2)",
                        "Load byte unsigned : Set $t1 to zero-extended 8-bit value from effective memory byte address",
                        BasicInstructionFormat.I_FORMAT,
                        "100100 ttttt fffff ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                try {
                                    RegisterFile.updateRegister(operands[0],
                                            Globals.memory.getByte(
                                                    RegisterFile.getValue(operands[2])
                                                            + (operands[1] << 16 >> 16))
                                                    & 0x000000ff);
                                } catch (AddressErrorException e) {
                                    throw new ProcessingException(statement, e);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("sb $t1,-100($t2)",
                        "Store byte : Store the low-order 8 bits of $t1 into the effective memory byte address",
                        BasicInstructionFormat.I_FORMAT,
                        "101000 ttttt fffff ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                try {
                                    Globals.memory.setByte(
                                            RegisterFile.getValue(operands[2])
                                                    + (operands[1] << 16 >> 16),
                                            RegisterFile.getValue(operands[0])
                                                    & 0x000000ff);
                                } catch (AddressErrorException e) {
                                    throw new ProcessingException(statement, e);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("sh $t1,-100($t2)",
                        "Store halfword : Store the low-order 16 bits of $t1 into the effective memory halfword address",
                        BasicInstructionFormat.I_FORMAT,
                        "101001 ttttt fffff ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                try {
                                    Globals.memory.setHalf(
                                            RegisterFile.getValue(operands[2])
                                                    + (operands[1] << 16 >> 16),
                                            RegisterFile.getValue(operands[0])
                                                    & 0x0000ffff);
                                } catch (AddressErrorException e) {
                                    throw new ProcessingException(statement, e);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("clo $t1,$t2",
                        "Count number of leading ones : Set $t1 to the count of leading one bits in $t2 starting at most significant bit position",
                        BasicInstructionFormat.R_FORMAT,
                        // MIPS32 requires rd (first) operand to appear twice in machine code.
                        // It has to be same as rt (third) operand in machine code, but the
                        // source statement does not have or permit third operand.
                        // In the machine code, rd and rt are adjacent, but my mask
                        // substitution cannot handle adjacent placement of the same source
                        // operand (e.g. "... sssss fffff fffff ...") because it would interpret
                        // the mask to be the total length of both (10 bits).  I could code it
                        // to have 3 operands then define a pseudo-instruction of two operands
                        // to translate into this, but then both would show up in instruction set
                        // list and I don't want that.  So I will use the convention of Computer
                        // Organization and Design 3rd Edition, Appendix A, and code the rt bits
                        // as 0's.  The generated code does not match SPIM and would not run
                        // on a real MIPS machine but since I am providing no means of storing
                        // the binary code that is not really an issue.
                        "011100 sssss 00000 fffff 00000 100001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int value = RegisterFile.getValue(operands[1]);
                                int leadingOnes = 0;
                                int bitPosition = 31;
                                while (Binary.bitValue(value, bitPosition) == 1 && bitPosition >= 0) {
                                    leadingOnes++;
                                    bitPosition--;
                                }
                                RegisterFile.updateRegister(operands[0], leadingOnes);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("clz $t1,$t2",
                        "Count number of leading zeroes : Set $t1 to the count of leading zero bits in $t2 starting at most significant bit positio",
                        BasicInstructionFormat.R_FORMAT,
                        // See comments for "clo" instruction above.  They apply here too.
                        "011100 sssss 00000 fffff 00000 100000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int value = RegisterFile.getValue(operands[1]);
                                int leadingZeros = 0;
                                int bitPosition = 31;
                                while (Binary.bitValue(value, bitPosition) == 0 && bitPosition >= 0) {
                                    leadingZeros++;
                                    bitPosition--;
                                }
                                RegisterFile.updateRegister(operands[0], leadingZeros);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("mfc0 $t1,$8",
                        "Move from Coprocessor 0 : Set $t1 to the value stored in Coprocessor 0 register $8",
                        BasicInstructionFormat.R_FORMAT,
                        "010000 00000 fffff sssss 00000 000000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                RegisterFile.updateRegister(operands[0],
                                        Coprocessor0.getValue(operands[1]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("mtc0 $t1,$8",
                        "Move to Coprocessor 0 : Set Coprocessor 0 register $8 to value stored in $t1",
                        BasicInstructionFormat.R_FORMAT,
                        "010000 00100 fffff sssss 00000 000000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                Coprocessor0.updateRegister(operands[1],
                                        RegisterFile.getValue(operands[0]));
                            }
                        }));

        /////////////////////// Floating Point Instructions Start Here ////////////////
        instructionList.add(
                new BasicInstruction("add.s $f0,$f1,$f3",
                        "Floating point addition single precision : Set $f0 to single-precision floating point value of $f1 plus $f3",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 ttttt sssss fffff 000000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                float add1 = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                                float add2 = Float.intBitsToFloat(Coprocessor1.getValue(operands[2]));
                                float sum = add1 + add2;
                                // overflow detected when sum is positive or negative infinity.
                  /*
                  if (sum == Float.NEGATIVE_INFINITY || sum == Float.POSITIVE_INFINITY) {
                    throw new ProcessingException(statement,"arithmetic overflow");
                  }
                  */
                                Coprocessor1.updateRegister(operands[0], Float.floatToIntBits(sum));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("sub.s $f0,$f1,$f3",
                        "Floating point subtraction single precision : Set $f0 to single-precision floating point value of $f1  minus $f3",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 ttttt sssss fffff 000001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                float sub1 = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                                float sub2 = Float.intBitsToFloat(Coprocessor1.getValue(operands[2]));
                                float diff = sub1 - sub2;
                                Coprocessor1.updateRegister(operands[0], Float.floatToIntBits(diff));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("mul.s $f0,$f1,$f3",
                        "Floating point multiplication single precision : Set $f0 to single-precision floating point value of $f1 times $f3",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 ttttt sssss fffff 000010",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                float mul1 = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                                float mul2 = Float.intBitsToFloat(Coprocessor1.getValue(operands[2]));
                                float prod = mul1 * mul2;
                                Coprocessor1.updateRegister(operands[0], Float.floatToIntBits(prod));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("div.s $f0,$f1,$f3",
                        "Floating point division single precision : Set $f0 to single-precision floating point value of $f1 divided by $f3",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 ttttt sssss fffff 000011",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                float div1 = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                                float div2 = Float.intBitsToFloat(Coprocessor1.getValue(operands[2]));
                                float quot = div1 / div2;
                                Coprocessor1.updateRegister(operands[0], Float.floatToIntBits(quot));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("sqrt.s $f0,$f1",
                        "Square root single precision : Set $f0 to single-precision floating point square root of $f1",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 00000 sssss fffff 000100",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                float value = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                                int floatSqrt = 0;
                                if (value < 0.0f) {
                                    // This is subject to refinement later.  Release 4.0 defines floor, ceil, trunc, round
                                    // to act silently rather than raise Invalid Operation exception, so sqrt should do the
                                    // same.  An intermediate step would be to define a setting for FCSR Invalid Operation
                                    // flag, but the best solution is to simulate the FCSR register itself.
                                    // FCSR = Floating point unit Control and Status Register.  DPS 10-Aug-2010
                                    floatSqrt = Float.floatToIntBits(Float.NaN);
                                    //throw new ProcessingException(statement, "Invalid Operation: sqrt of negative number");
                                } else {
                                    floatSqrt = Float.floatToIntBits((float) Math.sqrt(value));
                                }
                                Coprocessor1.updateRegister(operands[0], floatSqrt);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("floor.w.s $f0,$f1",
                        "Floor single precision to word : Set $f0 to 32-bit integer floor of single-precision float in $f1",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 00000 sssss fffff 001111",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                float floatValue = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                                int floor = (int) Math.floor(floatValue);
                                // DPS 28-July-2010: Since MARS does not simulate the FSCR, I will take the default
                                // action of setting the result to 2^31-1, if the value is outside the 32 bit range.
                                if (Float.isNaN(floatValue)
                                        || Float.isInfinite(floatValue)
                                        || floatValue < (float) Integer.MIN_VALUE
                                        || floatValue > (float) Integer.MAX_VALUE) {
                                    floor = Integer.MAX_VALUE;
                                }
                                Coprocessor1.updateRegister(operands[0], floor);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("ceil.w.s $f0,$f1",
                        "Ceiling single precision to word : Set $f0 to 32-bit integer ceiling of single-precision float in $f1",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 00000 sssss fffff 001110",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                float floatValue = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                                int ceiling = (int) Math.ceil(floatValue);
                                // DPS 28-July-2010: Since MARS does not simulate the FSCR, I will take the default
                                // action of setting the result to 2^31-1, if the value is outside the 32 bit range.
                                if (Float.isNaN(floatValue)
                                        || Float.isInfinite(floatValue)
                                        || floatValue < (float) Integer.MIN_VALUE
                                        || floatValue > (float) Integer.MAX_VALUE) {
                                    ceiling = Integer.MAX_VALUE;
                                }
                                Coprocessor1.updateRegister(operands[0], ceiling);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("round.w.s $f0,$f1",
                        "Round single precision to word : Set $f0 to 32-bit integer round of single-precision float in $f1",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 00000 sssss fffff 001100",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException { // MIPS32 documentation (and IEEE 754) states that round rounds to the nearest but when
                                // both are equally near it rounds to the even one!  SPIM rounds -4.5, -5.5,
                                // 4.5 and 5.5 to (-4, -5, 5, 6).  Curiously, it rounds -5.1 to -4 and -5.6 to -5.
                                // Until MARS 3.5, I used Math.round, which rounds to nearest but when both are
                                // equal it rounds toward positive infinity.  With Release 3.5, I painstakingly
                                // carry out the MIPS and IEEE 754 standard.
                                int[] operands = statement.getOperands();
                                float floatValue = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                                int below = 0, above = 0, round = Math.round(floatValue);
                                // According to MIPS32 spec, if any of these conditions is true, set
                                // Invalid Operation in the FCSR (Floating point Control/Status Register) and
                                // set result to be 2^31-1.  MARS does not implement this register (as of release 3.4.1).
                                // It also mentions the "Invalid Operation Enable bit" in FCSR, that, if set, results
                                // in immediate exception instead of default value.
                                if (Float.isNaN(floatValue)
                                        || Float.isInfinite(floatValue)
                                        || floatValue < (float) Integer.MIN_VALUE
                                        || floatValue > (float) Integer.MAX_VALUE) {
                                    round = Integer.MAX_VALUE;
                                } else {
                                    Float floatObj = new Float(floatValue);
                                    // If we are EXACTLY in the middle, then round to even!  To determine this,
                                    // find next higher integer and next lower integer, then see if distances
                                    // are exactly equal.
                                    if (floatValue < 0.0F) {
                                        above = floatObj.intValue(); // truncates
                                        below = above - 1;
                                    } else {
                                        below = floatObj.intValue(); // truncates
                                        above = below + 1;
                                    }
                                    if (floatValue - below == above - floatValue) { // exactly in the middle?
                                        round = (above % 2 == 0) ? above : below;
                                    }
                                }
                                Coprocessor1.updateRegister(operands[0], round);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("trunc.w.s $f0,$f1",
                        "Truncate single precision to word : Set $f0 to 32-bit integer truncation of single-precision float in $f1",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 00000 sssss fffff 001101",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                float floatValue = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                                int truncate = (int) floatValue;// Typecasting will round toward zero, the correct action
                                // DPS 28-July-2010: Since MARS does not simulate the FSCR, I will take the default
                                // action of setting the result to 2^31-1, if the value is outside the 32 bit range.
                                if (Float.isNaN(floatValue)
                                        || Float.isInfinite(floatValue)
                                        || floatValue < (float) Integer.MIN_VALUE
                                        || floatValue > (float) Integer.MAX_VALUE) {
                                    truncate = Integer.MAX_VALUE;
                                }
                                Coprocessor1.updateRegister(operands[0], truncate);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("add.d $f2,$f4,$f6",
                        "Floating point addition double precision : Set $f2 to double-precision floating point value of $f4 plus $f6",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 ttttt sssss fffff 000000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[0] % 2 == 1 || operands[1] % 2 == 1 || operands[2] % 2 == 1) {
                                    throw new ProcessingException(statement, "all registers must be even-numbered");
                                }
                                double add1 = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                                double add2 = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[2] + 1), Coprocessor1.getValue(operands[2])));
                                double sum = add1 + add2;
                                long longSum = Double.doubleToLongBits(sum);
                                Coprocessor1.updateRegister(operands[0] + 1, Binary.highOrderLongToInt(longSum));
                                Coprocessor1.updateRegister(operands[0], Binary.lowOrderLongToInt(longSum));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("sub.d $f2,$f4,$f6",
                        "Floating point subtraction double precision : Set $f2 to double-precision floating point value of $f4 minus $f6",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 ttttt sssss fffff 000001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[0] % 2 == 1 || operands[1] % 2 == 1 || operands[2] % 2 == 1) {
                                    throw new ProcessingException(statement, "all registers must be even-numbered");
                                }
                                double sub1 = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                                double sub2 = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[2] + 1), Coprocessor1.getValue(operands[2])));
                                double diff = sub1 - sub2;
                                long longDiff = Double.doubleToLongBits(diff);
                                Coprocessor1.updateRegister(operands[0] + 1, Binary.highOrderLongToInt(longDiff));
                                Coprocessor1.updateRegister(operands[0], Binary.lowOrderLongToInt(longDiff));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("mul.d $f2,$f4,$f6",
                        "Floating point multiplication double precision : Set $f2 to double-precision floating point value of $f4 times $f6",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 ttttt sssss fffff 000010",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[0] % 2 == 1 || operands[1] % 2 == 1 || operands[2] % 2 == 1) {
                                    throw new ProcessingException(statement, "all registers must be even-numbered");
                                }
                                double mul1 = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                                double mul2 = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[2] + 1), Coprocessor1.getValue(operands[2])));
                                double prod = mul1 * mul2;
                                long longProd = Double.doubleToLongBits(prod);
                                Coprocessor1.updateRegister(operands[0] + 1, Binary.highOrderLongToInt(longProd));
                                Coprocessor1.updateRegister(operands[0], Binary.lowOrderLongToInt(longProd));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("div.d $f2,$f4,$f6",
                        "Floating point division double precision : Set $f2 to double-precision floating point value of $f4 divided by $f6",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 ttttt sssss fffff 000011",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[0] % 2 == 1 || operands[1] % 2 == 1 || operands[2] % 2 == 1) {
                                    throw new ProcessingException(statement, "all registers must be even-numbered");
                                }
                                double div1 = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                                double div2 = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[2] + 1), Coprocessor1.getValue(operands[2])));
                                double quot = div1 / div2;
                                long longQuot = Double.doubleToLongBits(quot);
                                Coprocessor1.updateRegister(operands[0] + 1, Binary.highOrderLongToInt(longQuot));
                                Coprocessor1.updateRegister(operands[0], Binary.lowOrderLongToInt(longQuot));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("sqrt.d $f2,$f4",
                        "Square root double precision : Set $f2 to double-precision floating point square root of $f4",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 00000 sssss fffff 000100",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[0] % 2 == 1 || operands[1] % 2 == 1 || operands[2] % 2 == 1) {
                                    throw new ProcessingException(statement, "both registers must be even-numbered");
                                }
                                double value = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                                long longSqrt = 0;
                                if (value < 0.0) {
                                    // This is subject to refinement later.  Release 4.0 defines floor, ceil, trunc, round
                                    // to act silently rather than raise Invalid Operation exception, so sqrt should do the
                                    // same.  An intermediate step would be to define a setting for FCSR Invalid Operation
                                    // flag, but the best solution is to simulate the FCSR register itself.
                                    // FCSR = Floating point unit Control and Status Register.  DPS 10-Aug-2010
                                    longSqrt = Double.doubleToLongBits(Double.NaN);
                                    //throw new ProcessingException(statement, "Invalid Operation: sqrt of negative number");
                                } else {
                                    longSqrt = Double.doubleToLongBits(Math.sqrt(value));
                                }
                                Coprocessor1.updateRegister(operands[0] + 1, Binary.highOrderLongToInt(longSqrt));
                                Coprocessor1.updateRegister(operands[0], Binary.lowOrderLongToInt(longSqrt));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("floor.w.d $f1,$f2",
                        "Floor double precision to word : Set $f1 to 32-bit integer floor of double-precision float in $f2",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 00000 sssss fffff 001111",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[1] % 2 == 1) {
                                    throw new ProcessingException(statement, "second register must be even-numbered");
                                }
                                double doubleValue = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                                // DPS 27-July-2010: Since MARS does not simulate the FSCR, I will take the default
                                // action of setting the result to 2^31-1, if the value is outside the 32 bit range.
                                int floor = (int) Math.floor(doubleValue);
                                if (Double.isNaN(doubleValue)
                                        || Double.isInfinite(doubleValue)
                                        || doubleValue < (double) Integer.MIN_VALUE
                                        || doubleValue > (double) Integer.MAX_VALUE) {
                                    floor = Integer.MAX_VALUE;
                                }
                                Coprocessor1.updateRegister(operands[0], floor);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("ceil.w.d $f1,$f2",
                        "Ceiling double precision to word : Set $f1 to 32-bit integer ceiling of double-precision float in $f2",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 00000 sssss fffff 001110",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[1] % 2 == 1) {
                                    throw new ProcessingException(statement, "second register must be even-numbered");
                                }
                                double doubleValue = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                                // DPS 27-July-2010: Since MARS does not simulate the FSCR, I will take the default
                                // action of setting the result to 2^31-1, if the value is outside the 32 bit range.
                                int ceiling = (int) Math.ceil(doubleValue);
                                if (Double.isNaN(doubleValue)
                                        || Double.isInfinite(doubleValue)
                                        || doubleValue < (double) Integer.MIN_VALUE
                                        || doubleValue > (double) Integer.MAX_VALUE) {
                                    ceiling = Integer.MAX_VALUE;
                                }
                                Coprocessor1.updateRegister(operands[0], ceiling);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("round.w.d $f1,$f2",
                        "Round double precision to word : Set $f1 to 32-bit integer round of double-precision float in $f2",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 00000 sssss fffff 001100",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException { // See comments in round.w.s above, concerning MIPS and IEEE 754 standard.
                                // Until MARS 3.5, I used Math.round, which rounds to nearest but when both are
                                // equal it rounds toward positive infinity.  With Release 3.5, I painstakingly
                                // carry out the MIPS and IEEE 754 standard (round to nearest/even).
                                int[] operands = statement.getOperands();
                                if (operands[1] % 2 == 1) {
                                    throw new ProcessingException(statement, "second register must be even-numbered");
                                }
                                double doubleValue = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                                int below = 0, above = 0;
                                int round = (int) Math.round(doubleValue);
                                // See comments in round.w.s above concerning FSCR...
                                if (Double.isNaN(doubleValue)
                                        || Double.isInfinite(doubleValue)
                                        || doubleValue < (double) Integer.MIN_VALUE
                                        || doubleValue > (double) Integer.MAX_VALUE) {
                                    round = Integer.MAX_VALUE;
                                } else {
                                    Double doubleObj = new Double(doubleValue);
                                    // If we are EXACTLY in the middle, then round to even!  To determine this,
                                    // find next higher integer and next lower integer, then see if distances
                                    // are exactly equal.
                                    if (doubleValue < 0.0) {
                                        above = doubleObj.intValue(); // truncates
                                        below = above - 1;
                                    } else {
                                        below = doubleObj.intValue(); // truncates
                                        above = below + 1;
                                    }
                                    if (doubleValue - below == above - doubleValue) { // exactly in the middle?
                                        round = (above % 2 == 0) ? above : below;
                                    }
                                }
                                Coprocessor1.updateRegister(operands[0], round);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("trunc.w.d $f1,$f2",
                        "Truncate double precision to word : Set $f1 to 32-bit integer truncation of double-precision float in $f2",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 00000 sssss fffff 001101",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[1] % 2 == 1) {
                                    throw new ProcessingException(statement, "second register must be even-numbered");
                                }
                                double doubleValue = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                                // DPS 27-July-2010: Since MARS does not simulate the FSCR, I will take the default
                                // action of setting the result to 2^31-1, if the value is outside the 32 bit range.
                                int truncate = (int) doubleValue; // Typecasting will round toward zero, the correct action.
                                if (Double.isNaN(doubleValue)
                                        || Double.isInfinite(doubleValue)
                                        || doubleValue < (double) Integer.MIN_VALUE
                                        || doubleValue > (double) Integer.MAX_VALUE) {
                                    truncate = Integer.MAX_VALUE;
                                }
                                Coprocessor1.updateRegister(operands[0], truncate);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("bc1t label",
                        "Branch if FP condition flag 0 true (BC1T, not BCLT) : If Coprocessor 1 condition flag 0 is true (one) then branch to statement at label's address",
                        BasicInstructionFormat.I_BRANCH_FORMAT,
                        "010001 01000 00001 ffffffffffffffff",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (Coprocessor1.getConditionFlag(0) == 1) {
                                    processBranch(operands[0]);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("bc1t 1,label",
                        "Branch if specified FP condition flag true (BC1T, not BCLT) : If Coprocessor 1 condition flag specified by immediate is true (one) then branch to statement at label's address",
                        BasicInstructionFormat.I_BRANCH_FORMAT,
                        "010001 01000 fff 01 ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (Coprocessor1.getConditionFlag(operands[0]) == 1) {
                                    processBranch(operands[1]);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("bc1f label",
                        "Branch if FP condition flag 0 false (BC1F, not BCLF) : If Coprocessor 1 condition flag 0 is false (zero) then branch to statement at label's address",
                        BasicInstructionFormat.I_BRANCH_FORMAT,
                        "010001 01000 00000 ffffffffffffffff",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (Coprocessor1.getConditionFlag(0) == 0) {
                                    processBranch(operands[0]);
                                }

                            }
                        }));
        instructionList.add(
                new BasicInstruction("bc1f 1,label",
                        "Branch if specified FP condition flag false (BC1F, not BCLF) : If Coprocessor 1 condition flag specified by immediate is false (zero) then branch to statement at label's address",
                        BasicInstructionFormat.I_BRANCH_FORMAT,
                        "010001 01000 fff 00 ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (Coprocessor1.getConditionFlag(operands[0]) == 0) {
                                    processBranch(operands[1]);
                                }

                            }
                        }));
        instructionList.add(
                new BasicInstruction("c.eq.s $f0,$f1",
                        "Compare equal single precision : If $f0 is equal to $f1, set Coprocessor 1 condition flag 0 true else set it false",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 sssss fffff 00000 110010",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                float op1 = Float.intBitsToFloat(Coprocessor1.getValue(operands[0]));
                                float op2 = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                                if (op1 == op2)
                                    Coprocessor1.setConditionFlag(0);
                                else
                                    Coprocessor1.clearConditionFlag(0);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("c.eq.s 1,$f0,$f1",
                        "Compare equal single precision : If $f0 is equal to $f1, set Coprocessor 1 condition flag specied by immediate to true else set it to false",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 ttttt sssss fff 00 11 0010",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                float op1 = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                                float op2 = Float.intBitsToFloat(Coprocessor1.getValue(operands[2]));
                                if (op1 == op2)
                                    Coprocessor1.setConditionFlag(operands[0]);
                                else
                                    Coprocessor1.clearConditionFlag(operands[0]);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("c.le.s $f0,$f1",
                        "Compare less or equal single precision : If $f0 is less than or equal to $f1, set Coprocessor 1 condition flag 0 true else set it false",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 sssss fffff 00000 111110",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                float op1 = Float.intBitsToFloat(Coprocessor1.getValue(operands[0]));
                                float op2 = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                                if (op1 <= op2)
                                    Coprocessor1.setConditionFlag(0);
                                else
                                    Coprocessor1.clearConditionFlag(0);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("c.le.s 1,$f0,$f1",
                        "Compare less or equal single precision : If $f0 is less than or equal to $f1, set Coprocessor 1 condition flag specified by immediate to true else set it to false",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 ttttt sssss fff 00 111110",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                float op1 = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                                float op2 = Float.intBitsToFloat(Coprocessor1.getValue(operands[2]));
                                if (op1 <= op2)
                                    Coprocessor1.setConditionFlag(operands[0]);
                                else
                                    Coprocessor1.clearConditionFlag(operands[0]);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("c.lt.s $f0,$f1",
                        "Compare less than single precision : If $f0 is less than $f1, set Coprocessor 1 condition flag 0 true else set it false",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 sssss fffff 00000 111100",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                float op1 = Float.intBitsToFloat(Coprocessor1.getValue(operands[0]));
                                float op2 = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                                if (op1 < op2)
                                    Coprocessor1.setConditionFlag(0);
                                else
                                    Coprocessor1.clearConditionFlag(0);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("c.lt.s 1,$f0,$f1",
                        "Compare less than single precision : If $f0 is less than $f1, set Coprocessor 1 condition flag specified by immediate to true else set it to false",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 ttttt sssss fff 00 111100",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                float op1 = Float.intBitsToFloat(Coprocessor1.getValue(operands[1]));
                                float op2 = Float.intBitsToFloat(Coprocessor1.getValue(operands[2]));
                                if (op1 < op2)
                                    Coprocessor1.setConditionFlag(operands[0]);
                                else
                                    Coprocessor1.clearConditionFlag(operands[0]);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("c.eq.d $f2,$f4",
                        "Compare equal double precision : If $f2 is equal to $f4 (double-precision), set Coprocessor 1 condition flag 0 true else set it false",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 sssss fffff 00000 110010",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                                    throw new ProcessingException(statement, "both registers must be even-numbered");
                                }
                                double op1 = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[0] + 1), Coprocessor1.getValue(operands[0])));
                                double op2 = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                                if (op1 == op2)
                                    Coprocessor1.setConditionFlag(0);
                                else
                                    Coprocessor1.clearConditionFlag(0);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("c.eq.d 1,$f2,$f4",
                        "Compare equal double precision : If $f2 is equal to $f4 (double-precision), set Coprocessor 1 condition flag specified by immediate to true else set it to false",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 ttttt sssss fff 00 110010",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[1] % 2 == 1 || operands[2] % 2 == 1) {
                                    throw new ProcessingException(statement, "both registers must be even-numbered");
                                }
                                double op1 = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                                double op2 = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[2] + 1), Coprocessor1.getValue(operands[2])));
                                if (op1 == op2)
                                    Coprocessor1.setConditionFlag(operands[0]);
                                else
                                    Coprocessor1.clearConditionFlag(operands[0]);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("c.le.d $f2,$f4",
                        "Compare less or equal double precision : If $f2 is less than or equal to $f4 (double-precision), set Coprocessor 1 condition flag 0 true else set it false",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 sssss fffff 00000 111110",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                                    throw new ProcessingException(statement, "both registers must be even-numbered");
                                }
                                double op1 = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[0] + 1), Coprocessor1.getValue(operands[0])));
                                double op2 = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                                if (op1 <= op2)
                                    Coprocessor1.setConditionFlag(0);
                                else
                                    Coprocessor1.clearConditionFlag(0);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("c.le.d 1,$f2,$f4",
                        "Compare less or equal double precision : If $f2 is less than or equal to $f4 (double-precision), set Coprocessor 1 condition flag specfied by immediate true else set it false",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 ttttt sssss fff 00 111110",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[1] % 2 == 1 || operands[2] % 2 == 1) {
                                    throw new ProcessingException(statement, "both registers must be even-numbered");
                                }
                                double op1 = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                                double op2 = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[2] + 1), Coprocessor1.getValue(operands[2])));
                                if (op1 <= op2)
                                    Coprocessor1.setConditionFlag(operands[0]);
                                else
                                    Coprocessor1.clearConditionFlag(operands[0]);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("c.lt.d $f2,$f4",
                        "Compare less than double precision : If $f2 is less than $f4 (double-precision), set Coprocessor 1 condition flag 0 true else set it false",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 sssss fffff 00000 111100",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                                    throw new ProcessingException(statement, "both registers must be even-numbered");
                                }
                                double op1 = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[0] + 1), Coprocessor1.getValue(operands[0])));
                                double op2 = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                                if (op1 < op2)
                                    Coprocessor1.setConditionFlag(0);
                                else
                                    Coprocessor1.clearConditionFlag(0);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("c.lt.d 1,$f2,$f4",
                        "Compare less than double precision : If $f2 is less than $f4 (double-precision), set Coprocessor 1 condition flag specified by immediate to true else set it to false",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 ttttt sssss fff 00 111100",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[1] % 2 == 1 || operands[2] % 2 == 1) {
                                    throw new ProcessingException(statement, "both registers must be even-numbered");
                                }
                                double op1 = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                                double op2 = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[2] + 1), Coprocessor1.getValue(operands[2])));
                                if (op1 < op2)
                                    Coprocessor1.setConditionFlag(operands[0]);
                                else
                                    Coprocessor1.clearConditionFlag(operands[0]);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("abs.s $f0,$f1",
                        "Floating point absolute value single precision : Set $f0 to absolute value of $f1, single precision",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 00000 sssss fffff 000101",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                // I need only clear the high order bit!
                                Coprocessor1.updateRegister(operands[0],
                                        Coprocessor1.getValue(operands[1]) & Integer.MAX_VALUE);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("abs.d $f2,$f4",
                        "Floating point absolute value double precision : Set $f2 to absolute value of $f4, double precision",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 00000 sssss fffff 000101",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                                    throw new ProcessingException(statement, "both registers must be even-numbered");
                                }
                                // I need only clear the high order bit of high word register!
                                Coprocessor1.updateRegister(operands[0] + 1,
                                        Coprocessor1.getValue(operands[1] + 1) & Integer.MAX_VALUE);
                                Coprocessor1.updateRegister(operands[0],
                                        Coprocessor1.getValue(operands[1]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("cvt.d.s $f2,$f1",
                        "Convert from single precision to double precision : Set $f2 to double precision equivalent of single precision value in $f1",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 00000 sssss fffff 100001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[0] % 2 == 1) {
                                    throw new ProcessingException(statement, "first register must be even-numbered");
                                }
                                // convert single precision in $f1 to double stored in $f2
                                long result = Double.doubleToLongBits(
                                        (double) Float.intBitsToFloat(Coprocessor1.getValue(operands[1])));
                                Coprocessor1.updateRegister(operands[0] + 1, Binary.highOrderLongToInt(result));
                                Coprocessor1.updateRegister(operands[0], Binary.lowOrderLongToInt(result));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("cvt.d.w $f2,$f1",
                        "Convert from word to double precision : Set $f2 to double precision equivalent of 32-bit integer value in $f1",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10100 00000 sssss fffff 100001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[0] % 2 == 1) {
                                    throw new ProcessingException(statement, "first register must be even-numbered");
                                }
                                // convert integer to double (interpret $f1 value as int?)
                                long result = Double.doubleToLongBits(
                                        (double) Coprocessor1.getValue(operands[1]));
                                Coprocessor1.updateRegister(operands[0] + 1, Binary.highOrderLongToInt(result));
                                Coprocessor1.updateRegister(operands[0], Binary.lowOrderLongToInt(result));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("cvt.s.d $f1,$f2",
                        "Convert from double precision to single precision : Set $f1 to single precision equivalent of double precision value in $f2",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 00000 sssss fffff 100000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                // convert double precision in $f2 to single stored in $f1
                                if (operands[1] % 2 == 1) {
                                    throw new ProcessingException(statement, "second register must be even-numbered");
                                }
                                double val = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                                Coprocessor1.updateRegister(operands[0], Float.floatToIntBits((float) val));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("cvt.s.w $f0,$f1",
                        "Convert from word to single precision : Set $f0 to single precision equivalent of 32-bit integer value in $f2",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10100 00000 sssss fffff 100000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                // convert integer to single (interpret $f1 value as int?)
                                Coprocessor1.updateRegister(operands[0],
                                        Float.floatToIntBits((float) Coprocessor1.getValue(operands[1])));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("cvt.w.d $f1,$f2",
                        "Convert from double precision to word : Set $f1 to 32-bit integer equivalent of double precision value in $f2",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 00000 sssss fffff 100100",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                // convert double precision in $f2 to integer stored in $f1
                                if (operands[1] % 2 == 1) {
                                    throw new ProcessingException(statement, "second register must be even-numbered");
                                }
                                double val = Double.longBitsToDouble(Binary.twoIntsToLong(
                                        Coprocessor1.getValue(operands[1] + 1), Coprocessor1.getValue(operands[1])));
                                Coprocessor1.updateRegister(operands[0], (int) val);
                            }
                        }));
        instructionList.add(
                new BasicInstruction("cvt.w.s $f0,$f1",
                        "Convert from single precision to word : Set $f0 to 32-bit integer equivalent of single precision value in $f1",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 00000 sssss fffff 100100",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                // convert single precision in $f1 to integer stored in $f0
                                Coprocessor1.updateRegister(operands[0],
                                        (int) Float.intBitsToFloat(Coprocessor1.getValue(operands[1])));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("mov.d $f2,$f4",
                        "Move floating point double precision : Set double precision $f2 to double precision value in $f4",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 00000 sssss fffff 000110",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                                    throw new ProcessingException(statement, "both registers must be even-numbered");
                                }
                                Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                                Coprocessor1.updateRegister(operands[0] + 1, Coprocessor1.getValue(operands[1] + 1));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("movf.d $f2,$f4",
                        "Move floating point double precision : If condition flag 0 false, set double precision $f2 to double precision value in $f4",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 000 00 sssss fffff 010001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                                    throw new ProcessingException(statement, "both registers must be even-numbered");
                                }
                                if (Coprocessor1.getConditionFlag(0) == 0) {
                                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                                    Coprocessor1.updateRegister(operands[0] + 1, Coprocessor1.getValue(operands[1] + 1));
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("movf.d $f2,$f4,1",
                        "Move floating point double precision : If condition flag specified by immediate is false, set double precision $f2 to double precision value in $f4",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 ttt 00 sssss fffff 010001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                                    throw new ProcessingException(statement, "both registers must be even-numbered");
                                }
                                if (Coprocessor1.getConditionFlag(operands[2]) == 0) {
                                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                                    Coprocessor1.updateRegister(operands[0] + 1, Coprocessor1.getValue(operands[1] + 1));
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("movt.d $f2,$f4",
                        "Move floating point double precision : If condition flag 0 true, set double precision $f2 to double precision value in $f4",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 000 01 sssss fffff 010001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                                    throw new ProcessingException(statement, "both registers must be even-numbered");
                                }
                                if (Coprocessor1.getConditionFlag(0) == 1) {
                                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                                    Coprocessor1.updateRegister(operands[0] + 1, Coprocessor1.getValue(operands[1] + 1));
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("movt.d $f2,$f4,1",
                        "Move floating point double precision : If condition flag specified by immediate is true, set double precision $f2 to double precision value in $f4e",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 ttt 01 sssss fffff 010001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                                    throw new ProcessingException(statement, "both registers must be even-numbered");
                                }
                                if (Coprocessor1.getConditionFlag(operands[2]) == 1) {
                                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                                    Coprocessor1.updateRegister(operands[0] + 1, Coprocessor1.getValue(operands[1] + 1));
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("movn.d $f2,$f4,$t3",
                        "Move floating point double precision : If $t3 is not zero, set double precision $f2 to double precision value in $f4",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 ttttt sssss fffff 010011",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                                    throw new ProcessingException(statement, "both registers must be even-numbered");
                                }
                                if (RegisterFile.getValue(operands[2]) != 0) {
                                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                                    Coprocessor1.updateRegister(operands[0] + 1, Coprocessor1.getValue(operands[1] + 1));
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("movz.d $f2,$f4,$t3",
                        "Move floating point double precision : If $t3 is zero, set double precision $f2 to double precision value in $f4",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 ttttt sssss fffff 010010",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                                    throw new ProcessingException(statement, "both registers must be even-numbered");
                                }
                                if (RegisterFile.getValue(operands[2]) == 0) {
                                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                                    Coprocessor1.updateRegister(operands[0] + 1, Coprocessor1.getValue(operands[1] + 1));
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("mov.s $f0,$f1",
                        "Move floating point single precision : Set single precision $f0 to single precision value in $f1",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 00000 sssss fffff 000110",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("movf.s $f0,$f1",
                        "Move floating point single precision : If condition flag 0 is false, set single precision $f0 to single precision value in $f1",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 000 00 sssss fffff 010001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (Coprocessor1.getConditionFlag(0) == 0)
                                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("movf.s $f0,$f1,1",
                        "Move floating point single precision : If condition flag specified by immediate is false, set single precision $f0 to single precision value in $f1e",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 ttt 00 sssss fffff 010001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (Coprocessor1.getConditionFlag(operands[2]) == 0)
                                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("movt.s $f0,$f1",
                        "Move floating point single precision : If condition flag 0 is true, set single precision $f0 to single precision value in $f1e",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 000 01 sssss fffff 010001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (Coprocessor1.getConditionFlag(0) == 1)
                                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("movt.s $f0,$f1,1",
                        "Move floating point single precision : If condition flag specified by immediate is true, set single precision $f0 to single precision value in $f1e",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 ttt 01 sssss fffff 010001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (Coprocessor1.getConditionFlag(operands[2]) == 1)
                                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("movn.s $f0,$f1,$t3",
                        "Move floating point single precision : If $t3 is not zero, set single precision $f0 to single precision value in $f1",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 ttttt sssss fffff 010011",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[2]) != 0)
                                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("movz.s $f0,$f1,$t3",
                        "Move floating point single precision : If $t3 is zero, set single precision $f0 to single precision value in $f1",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 ttttt sssss fffff 010010",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[2]) == 0)
                                    Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("mfc1 $t1,$f1",
                        "Move from Coprocessor 1 (FPU) : Set $t1 to value in Coprocessor 1 register $f1",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 00000 fffff sssss 00000 000000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                RegisterFile.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("mtc1 $t1,$f1",
                        "Move to Coprocessor 1 (FPU) : Set Coprocessor 1 register $f1 to value in $t1",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 00100 fffff sssss 00000 000000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                Coprocessor1.updateRegister(operands[1], RegisterFile.getValue(operands[0]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("neg.d $f2,$f4",
                        "Floating point negate double precision : Set double precision $f2 to negation of double precision value in $f4",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10001 00000 sssss fffff 000111",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[0] % 2 == 1 || operands[1] % 2 == 1) {
                                    throw new ProcessingException(statement, "both registers must be even-numbered");
                                }
                                // flip the sign bit of the second register (high order word) of the pair
                                int value = Coprocessor1.getValue(operands[1] + 1);
                                Coprocessor1.updateRegister(operands[0] + 1,
                                        ((value < 0) ? (value & Integer.MAX_VALUE) : (value | Integer.MIN_VALUE)));
                                Coprocessor1.updateRegister(operands[0], Coprocessor1.getValue(operands[1]));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("neg.s $f0,$f1",
                        "Floating point negate single precision : Set single precision $f0 to negation of single precision value in $f1",
                        BasicInstructionFormat.R_FORMAT,
                        "010001 10000 00000 sssss fffff 000111",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int value = Coprocessor1.getValue(operands[1]);
                                // flip the sign bit
                                Coprocessor1.updateRegister(operands[0],
                                        ((value < 0) ? (value & Integer.MAX_VALUE) : (value | Integer.MIN_VALUE)));
                            }
                        }));
        instructionList.add(
                new BasicInstruction("lwc1 $f1,-100($t2)",
                        "Load word into Coprocessor 1 (FPU) : Set $f1 to 32-bit value from effective memory word address",
                        BasicInstructionFormat.I_FORMAT,
                        "110001 ttttt fffff ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                try {
                                    Coprocessor1.updateRegister(operands[0],
                                            Globals.memory.getWord(
                                                    RegisterFile.getValue(operands[2]) + operands[1]));
                                } catch (AddressErrorException e) {
                                    throw new ProcessingException(statement, e);
                                }
                            }
                        }));
        instructionList.add(// no printed reference, got opcode from SPIM
                new BasicInstruction("ldc1 $f2,-100($t2)",
                        "Load double word Coprocessor 1 (FPU)) : Set $f2 to 64-bit value from effective memory doubleword address",
                        BasicInstructionFormat.I_FORMAT,
                        "110101 ttttt fffff ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[0] % 2 == 1) {
                                    throw new ProcessingException(statement, "first register must be even-numbered");
                                }
                                // IF statement added by DPS 13-July-2011.
                                if (!Globals.memory.doublewordAligned(RegisterFile.getValue(operands[2]) + operands[1])) {
                                    throw new ProcessingException(statement,
                                            new AddressErrorException("address not aligned on doubleword boundary ",
                                                    Exceptions.ADDRESS_EXCEPTION_LOAD, RegisterFile.getValue(operands[2]) + operands[1]));
                                }

                                try {
                                    Coprocessor1.updateRegister(operands[0],
                                            Globals.memory.getWord(
                                                    RegisterFile.getValue(operands[2]) + operands[1]));
                                    Coprocessor1.updateRegister(operands[0] + 1,
                                            Globals.memory.getWord(
                                                    RegisterFile.getValue(operands[2]) + operands[1] + 4));
                                } catch (AddressErrorException e) {
                                    throw new ProcessingException(statement, e);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("swc1 $f1,-100($t2)",
                        "Store word from Coprocesor 1 (FPU) : Store 32 bit value in $f1 to effective memory word address",
                        BasicInstructionFormat.I_FORMAT,
                        "111001 ttttt fffff ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                try {
                                    Globals.memory.setWord(
                                            RegisterFile.getValue(operands[2]) + operands[1],
                                            Coprocessor1.getValue(operands[0]));
                                } catch (AddressErrorException e) {
                                    throw new ProcessingException(statement, e);
                                }
                            }
                        }));
        instructionList.add( // no printed reference, got opcode from SPIM
                new BasicInstruction("sdc1 $f2,-100($t2)",
                        "Store double word from Coprocessor 1 (FPU)) : Store 64 bit value in $f2 to effective memory doubleword address",
                        BasicInstructionFormat.I_FORMAT,
                        "111101 ttttt fffff ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (operands[0] % 2 == 1) {
                                    throw new ProcessingException(statement, "first register must be even-numbered");
                                }
                                // IF statement added by DPS 13-July-2011.
                                if (!Globals.memory.doublewordAligned(RegisterFile.getValue(operands[2]) + operands[1])) {
                                    throw new ProcessingException(statement,
                                            new AddressErrorException("address not aligned on doubleword boundary ",
                                                    Exceptions.ADDRESS_EXCEPTION_STORE, RegisterFile.getValue(operands[2]) + operands[1]));
                                }
                                try {
                                    Globals.memory.setWord(
                                            RegisterFile.getValue(operands[2]) + operands[1],
                                            Coprocessor1.getValue(operands[0]));
                                    Globals.memory.setWord(
                                            RegisterFile.getValue(operands[2]) + operands[1] + 4,
                                            Coprocessor1.getValue(operands[0] + 1));
                                } catch (AddressErrorException e) {
                                    throw new ProcessingException(statement, e);
                                }
                            }
                        }));
        ////////////////////////////  THE TRAP INSTRUCTIONS & ERET  ////////////////////////////
        instructionList.add(
                new BasicInstruction("teq $t1,$t2",
                        "Trap if equal : Trap if $t1 is equal to $t2",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff sssss 00000 00000 110100",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[0]) == RegisterFile.getValue(operands[1])) {
                                    throw new ProcessingException(statement,
                                            "trap", Exceptions.TRAP_EXCEPTION);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("teqi $t1,-100",
                        "Trap if equal to immediate : Trap if $t1 is equal to sign-extended 16 bit immediate",
                        BasicInstructionFormat.I_FORMAT,
                        "000001 fffff 01100 ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[0]) == (operands[1] << 16 >> 16)) {
                                    throw new ProcessingException(statement,
                                            "trap", Exceptions.TRAP_EXCEPTION);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("tne $t1,$t2",
                        "Trap if not equal : Trap if $t1 is not equal to $t2",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff sssss 00000 00000 110110",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[0]) != RegisterFile.getValue(operands[1])) {
                                    throw new ProcessingException(statement,
                                            "trap", Exceptions.TRAP_EXCEPTION);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("tnei $t1,-100",
                        "Trap if not equal to immediate : Trap if $t1 is not equal to sign-extended 16 bit immediate",
                        BasicInstructionFormat.I_FORMAT,
                        "000001 fffff 01110 ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[0]) != (operands[1] << 16 >> 16)) {
                                    throw new ProcessingException(statement,
                                            "trap", Exceptions.TRAP_EXCEPTION);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("tge $t1,$t2",
                        "Trap if greater or equal : Trap if $t1 is greater than or equal to $t2",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff sssss 00000 00000 110000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[0]) >= RegisterFile.getValue(operands[1])) {
                                    throw new ProcessingException(statement,
                                            "trap", Exceptions.TRAP_EXCEPTION);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("tgeu $t1,$t2",
                        "Trap if greater or equal unsigned : Trap if $t1 is greater than or equal to $t2 using unsigned comparision",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff sssss 00000 00000 110001",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int first = RegisterFile.getValue(operands[0]);
                                int second = RegisterFile.getValue(operands[1]);
                                // if signs same, do straight compare; if signs differ & first negative then first greater else second
                                if ((first >= 0 && second >= 0 || first < 0 && second < 0) ? (first >= second) : (first < 0)) {
                                    throw new ProcessingException(statement,
                                            "trap", Exceptions.TRAP_EXCEPTION);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("tgei $t1,-100",
                        "Trap if greater than or equal to immediate : Trap if $t1 greater than or equal to sign-extended 16 bit immediate",
                        BasicInstructionFormat.I_FORMAT,
                        "000001 fffff 01000 ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[0]) >= (operands[1] << 16 >> 16)) {
                                    throw new ProcessingException(statement,
                                            "trap", Exceptions.TRAP_EXCEPTION);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("tgeiu $t1,-100",
                        "Trap if greater or equal to immediate unsigned : Trap if $t1 greater than or equal to sign-extended 16 bit immediate, unsigned comparison",
                        BasicInstructionFormat.I_FORMAT,
                        "000001 fffff 01001 ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int first = RegisterFile.getValue(operands[0]);
                                // 16 bit immediate value in operands[1] is sign-extended
                                int second = operands[1] << 16 >> 16;
                                // if signs same, do straight compare; if signs differ & first negative then first greater else second
                                if ((first >= 0 && second >= 0 || first < 0 && second < 0) ? (first >= second) : (first < 0)) {
                                    throw new ProcessingException(statement,
                                            "trap", Exceptions.TRAP_EXCEPTION);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("tlt $t1,$t2",
                        "Trap if less than: Trap if $t1 less than $t2",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff sssss 00000 00000 110010",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[0]) < RegisterFile.getValue(operands[1])) {
                                    throw new ProcessingException(statement,
                                            "trap", Exceptions.TRAP_EXCEPTION);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("tltu $t1,$t2",
                        "Trap if less than unsigned : Trap if $t1 less than $t2, unsigned comparison",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff sssss 00000 00000 110011",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int first = RegisterFile.getValue(operands[0]);
                                int second = RegisterFile.getValue(operands[1]);
                                // if signs same, do straight compare; if signs differ & first positive then first is less else second
                                if ((first >= 0 && second >= 0 || first < 0 && second < 0) ? (first < second) : (first >= 0)) {
                                    throw new ProcessingException(statement,
                                            "trap", Exceptions.TRAP_EXCEPTION);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("tlti $t1,-100",
                        "Trap if less than immediate : Trap if $t1 less than sign-extended 16-bit immediate",
                        BasicInstructionFormat.I_FORMAT,
                        "000001 fffff 01010 ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[0]) < (operands[1] << 16 >> 16)) {
                                    throw new ProcessingException(statement,
                                            "trap", Exceptions.TRAP_EXCEPTION);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("tltiu $t1,-100",
                        "Trap if less than immediate unsigned : Trap if $t1 less than sign-extended 16-bit immediate, unsigned comparison",
                        BasicInstructionFormat.I_FORMAT,
                        "000001 fffff 01011 ssssssssssssssss",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int first = RegisterFile.getValue(operands[0]);
                                // 16 bit immediate value in operands[1] is sign-extended
                                int second = operands[1] << 16 >> 16;
                                // if signs same, do straight compare; if signs differ & first positive then first is less else second
                                if ((first >= 0 && second >= 0 || first < 0 && second < 0) ? (first < second) : (first >= 0)) {
                                    throw new ProcessingException(statement,
                                            "trap", Exceptions.TRAP_EXCEPTION);
                                }
                            }
                        }));
        instructionList.add(
                new BasicInstruction("eret",
                        "Exception return : Set Program Counter to Coprocessor 0 EPC register value, set Coprocessor Status register bit 1 (exception level) to zero",
                        BasicInstructionFormat.R_FORMAT,
                        "010000 1 0000000000000000000 011000",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                // set EXL bit (bit 1) in Status register to 0 and set PC to EPC
                                Coprocessor0.updateRegister(Coprocessor0.STATUS,
                                        Binary.clearBit(Coprocessor0.getValue(Coprocessor0.STATUS), Coprocessor0.EXCEPTION_LEVEL));
                                RegisterFile.setProgramCounter(Coprocessor0.getValue(Coprocessor0.EPC));
                            }
                        }));

        ////////////// READ PSEUDO-INSTRUCTION SPECS FROM DATA FILE AND ADD //////////////////////
        addPseudoInstructions();

        ////////////// GET AND CREATE LIST OF SYSCALL FUNCTION OBJECTS ////////////////////
        syscallLoader = new SyscallLoader();
        syscallLoader.loadSyscalls();

        // Initialization step.  Create token list for each instruction example.  This is
        // used by parser to determine user program correct syntax.
        for (Instruction inst : instructionList) {
            inst.createExampleTokenList();
        }

        HashMap<Integer, HashMap<Integer, BasicInstruction>> maskMap = new HashMap<>();
        ArrayList<MatchMap> matchMaps = new ArrayList<>();
        for (Instruction inst : instructionList) {
            if (inst instanceof BasicInstruction) {
                BasicInstruction basic = (BasicInstruction) inst;
                Integer mask = basic.getOpcodeMask();
                Integer match = basic.getOpcodeMatch();
                HashMap<Integer, BasicInstruction> matchMap = maskMap.get(mask);
                if (matchMap == null) {
                    matchMap = new HashMap<>();
                    maskMap.put(mask, matchMap);
                    matchMaps.add(new MatchMap(mask, matchMap));
                }
                matchMap.put(match, basic);
            }
        }
        Collections.sort(matchMaps);
        this.opcodeMatchMaps = matchMaps;
    }

    public BasicInstruction findByBinaryCode(int binaryInstr) {
        for (MatchMap map : this.opcodeMatchMaps) {
            BasicInstruction ret = map.find(binaryInstr);
            if (ret != null) return ret;
        }
        return null;
    }

    /*  METHOD TO ADD PSEUDO-INSTRUCTIONS
    */

    private void addPseudoInstructions() {
        InputStream is = null;
        BufferedReader in = null;
        try {
            // leading "/" prevents package name being prepended to filepath.
            is = this.getClass().getResourceAsStream("/PseudoOps.txt");
            in = new BufferedReader(new InputStreamReader(is));
        } catch (NullPointerException e) {
            System.out.println(
                    "Error: MIPS pseudo-instruction file PseudoOps.txt not found.");
            System.exit(0);
        }
        try {
            String line, pseudoOp, template, firstTemplate, token;
            String description;
            StringTokenizer tokenizer;
            while ((line = in.readLine()) != null) {
                // skip over: comment lines, empty lines, lines starting with blank.
                if (!line.startsWith("#") && !line.startsWith(" ")
                        && line.length() > 0) {
                    description = "";
                    tokenizer = new StringTokenizer(line, "\t");
                    pseudoOp = tokenizer.nextToken();
                    template = "";
                    firstTemplate = null;
                    while (tokenizer.hasMoreTokens()) {
                        token = tokenizer.nextToken();
                        if (token.startsWith("#")) {
                            // Optional description must be last token in the line.
                            description = token.substring(1);
                            break;
                        }
                        if (token.startsWith("COMPACT")) {
                            // has second template for Compact (16-bit) memory config -- added DPS 3 Aug 2009
                            firstTemplate = template;
                            template = "";
                            continue;
                        }
                        template = template + token;
                        if (tokenizer.hasMoreTokens()) {
                            template = template + "\n";
                        }
                    }
                    ExtendedInstruction inst = (firstTemplate == null)
                            ? new ExtendedInstruction(pseudoOp, template, description)
                            : new ExtendedInstruction(pseudoOp, firstTemplate, template, description);
                    instructionList.add(inst);
                    //if (firstTemplate != null) System.out.println("\npseudoOp: "+pseudoOp+"\ndefault template:\n"+firstTemplate+"\ncompact template:\n"+template);
                }
            }
            in.close();
        } catch (IOException ioe) {
            System.out.println(
                    "Internal Error: MIPS pseudo-instructions could not be loaded.");
            System.exit(0);
        } catch (Exception ioe) {
            System.out.println(
                    "Error: Invalid MIPS pseudo-instruction specification.");
            System.exit(0);
        }

    }

    /**
     * Given an operator mnemonic, will return the corresponding Instruction object(s)
     * from the instruction set.  Uses straight linear search technique.
     *
     * @param name operator mnemonic (e.g. addi, sw,...)
     * @return list of corresponding Instruction object(s), or null if not found.
     */
    public ArrayList<Instruction> matchOperator(String name) {
        ArrayList<Instruction> matchingInstructions = null;
        // Linear search for now....
        for (Instruction inst : instructionList) {
            if (inst.getName().equalsIgnoreCase(name)) {
                if (matchingInstructions == null)
                    matchingInstructions = new ArrayList<>();
                matchingInstructions.add(inst);
            }
        }
        return matchingInstructions;
    }


    /**
     * Given a string, will return the Instruction object(s) from the instruction
     * set whose operator mnemonic prefix matches it.  Case-insensitive.  For example
     * "s" will match "sw", "sh", "sb", etc.  Uses straight linear search technique.
     *
     * @param name a string
     * @return list of matching Instruction object(s), or null if none match.
     */
    public ArrayList<Instruction> prefixMatchOperator(String name) {
        ArrayList<Instruction> matchingInstructions = null;
        // Linear search for now....
        if (name != null) {
            for (Instruction inst : instructionList) {
                if (inst.getName().toLowerCase().startsWith(name.toLowerCase())) {
                    if (matchingInstructions == null)
                        matchingInstructions = new ArrayList<>();
                    matchingInstructions.add(inst);
                }
            }
        }
        return matchingInstructions;
    }

   	/*
        * Method to find and invoke a syscall given its service number.  Each syscall
   	 * function is represented by an object in an array list.  Each object is of
   	 * a class that implements Syscall or extends AbstractSyscall.
   	 */

    private void findAndSimulateSyscall(int number, ProgramStatement statement)
            throws ProcessingException {
        Syscall service = syscallLoader.findSyscall(number);
        if (service != null) {
            service.simulate(statement);
            return;
        }
        throw new ProcessingException(statement,
                "invalid or unimplemented syscall service: " +
                        number + " ", Exceptions.SYSCALL_EXCEPTION);
    }

   	/*
   	 * Method to process a successful branch condition.  DO NOT USE WITH JUMP
   	 * INSTRUCTIONS!  The branch operand is a relative displacement in words
   	 * whereas the jump operand is an absolute address in bytes.
   	 *
   	 * The parameter is displacement operand from instruction.
   	 *
   	 * Handles delayed branching if that setting is enabled.
   	 */
    // 4 January 2008 DPS:  The subtraction of 4 bytes (instruction length) after
    // the shift has been removed.  It is left in as commented-out code below.
    // This has the effect of always branching as if delayed branching is enabled,
    // even if it isn't.  This mod must work in conjunction with
    // ProgramStatement.java, buildBasicStatementFromBasicInstruction() method near
    // the bottom (currently line 194, heavily commented).

    private void processBranch(int displacement) {
        if (Globals.getSettings().getDelayedBranchingEnabled()) {
            // Register the branch target address (absolute byte address).
            DelayedBranch.register(RegisterFile.getProgramCounter() + (displacement << 2));
        } else {
            // Decrement needed because PC has already been incremented
            RegisterFile.setProgramCounter(
                    RegisterFile.getProgramCounter()
                            + (displacement << 2)); // - Instruction.INSTRUCTION_LENGTH);
        }
    }

   	/*
   	 * Method to process a jump.  DO NOT USE WITH BRANCH INSTRUCTIONS!
   	 * The branch operand is a relative displacement in words
   	 * whereas the jump operand is an absolute address in bytes.
   	 *
   	 * The parameter is jump target absolute byte address.
   	 *
   	 * Handles delayed branching if that setting is enabled.
   	 */

    private void processJump(int targetAddress) {
        if (Globals.getSettings().getDelayedBranchingEnabled()) {
            DelayedBranch.register(targetAddress);
        } else {
            RegisterFile.setProgramCounter(targetAddress);
        }
    }

   	/*
   	 * Method to process storing of a return address in the given
   	 * register.  This is used only by the "and link"
   	 * instructions: jal, jalr, bltzal, bgezal.  If delayed branching
   	 * setting is off, the return address is the address of the
   	 * next instruction (e.g. the current PC value).  If on, the
   	 * return address is the instruction following that, to skip over
   	 * the delay slot.
   	 *
   	 * The parameter is register number to receive the return address.
   	 */

    private void processReturnAddress(int register) {
        RegisterFile.updateRegister(register, RegisterFile.getProgramCounter() +
                ((Globals.getSettings().getDelayedBranchingEnabled()) ?
                        Instruction.INSTRUCTION_LENGTH : 0));
    }

    private static class MatchMap implements Comparable {
        private int mask;
        private int maskLength; // number of 1 bits in mask
        private HashMap<Integer, BasicInstruction> matchMap;

        public MatchMap(int mask, HashMap<Integer, BasicInstruction> matchMap) {
            this.mask = mask;
            this.matchMap = matchMap;

            int k = 0;
            int n = mask;
            while (n != 0) {
                k++;
                n &= n - 1;
            }
            this.maskLength = k;
        }

        public boolean equals(Object o) {
            return o instanceof MatchMap && mask == ((MatchMap) o).mask;
        }

        public int compareTo(Object other) {
            MatchMap o = (MatchMap) other;
            int d = o.maskLength - this.maskLength;
            if (d == 0) d = this.mask - o.mask;
            return d;
        }

        public BasicInstruction find(int instr) {
            int match = instr & mask;
            return matchMap.get(match);
        }
    }
}

