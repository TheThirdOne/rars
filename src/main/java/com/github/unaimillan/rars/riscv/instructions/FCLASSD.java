package com.github.unaimillan.rars.riscv.instructions;

import com.github.unaimillan.jsoftfloat.types.Float64;
import com.github.unaimillan.rars.ProgramStatement;
import com.github.unaimillan.rars.riscv.BasicInstruction;
import com.github.unaimillan.rars.riscv.BasicInstructionFormat;
import com.github.unaimillan.rars.riscv.hardware.FloatingPointRegisterFile;

public class FCLASSD extends BasicInstruction {
    public FCLASSD() {
        super("fclass.d t1, f1", "Classify a floating point number (64 bit)",
                BasicInstructionFormat.I_FORMAT, "1110001 00000 sssss 001 fffff 1010011");
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        Float64 in = new Float64(FloatingPointRegisterFile.getValueLong(operands[1]));
        FCLASSS.fclass(in,operands[0]);
    }
}