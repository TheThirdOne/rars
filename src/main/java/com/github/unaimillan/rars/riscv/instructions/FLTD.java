package com.github.unaimillan.rars.riscv.instructions;

import com.github.unaimillan.jsoftfloat.Environment;
import com.github.unaimillan.jsoftfloat.types.Float64;
import com.github.unaimillan.rars.ProgramStatement;
import com.github.unaimillan.rars.riscv.BasicInstruction;
import com.github.unaimillan.rars.riscv.BasicInstructionFormat;
import com.github.unaimillan.rars.riscv.hardware.RegisterFile;

public class FLTD extends BasicInstruction {
    public FLTD() {
        super("flt.d t1, f1, f2", "Floating Less Than (64 bit): if f1 < f2, set t1 to 1, else set t1 to 0",
                BasicInstructionFormat.R_FORMAT, "1010001 ttttt sssss 001 fffff 1010011");
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        Float64 f1 = Double.getDouble(operands[1]), f2 = Double.getDouble(operands[2]);
        Environment e = new Environment();
        boolean result = com.github.unaimillan.jsoftfloat.operations.Comparisons.compareSignalingLessThan(f1,f2,e);
        Floating.setfflags(e);
        RegisterFile.updateRegister(operands[0], result ? 1 : 0);
    }
}