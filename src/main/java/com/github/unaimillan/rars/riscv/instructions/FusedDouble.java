package com.github.unaimillan.rars.riscv.instructions;

import com.github.unaimillan.jsoftfloat.Environment;
import com.github.unaimillan.jsoftfloat.types.Float64;
import com.github.unaimillan.rars.ProgramStatement;
import com.github.unaimillan.rars.SimulationException;
import com.github.unaimillan.rars.riscv.BasicInstruction;
import com.github.unaimillan.rars.riscv.BasicInstructionFormat;
import com.github.unaimillan.rars.riscv.hardware.FloatingPointRegisterFile;

/**
 * Helper class for 4 argument floating point instructions
 */
public abstract class FusedDouble extends BasicInstruction {
    public FusedDouble(String usage, String description, String op) {
        super(usage+", dyn", description, BasicInstructionFormat.R4_FORMAT,
                "qqqqq 01 ttttt sssss " + "ppp" + " fffff 100" + op + "11");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        Environment e = new Environment();
        e.mode = Floating.getRoundingMode(operands[4],statement);
        Float64 result = compute(new Float64(FloatingPointRegisterFile.getValueLong(operands[1])),
                new Float64(FloatingPointRegisterFile.getValueLong(operands[2])),
                new Float64(FloatingPointRegisterFile.getValueLong(operands[3])),e);
        Floating.setfflags(e);
        FloatingPointRegisterFile.updateRegisterLong(operands[0],result.bits);
    }

    /**
     * @param r1 The first register
     * @param r2 The second register
     * @param r3 The third register
     * @return The value to store to the destination
     */
    protected abstract Float64 compute(Float64 r1, Float64 r2, Float64 r3,Environment e);
}
