package com.github.unaimillan.rars.riscv.instructions;

import com.github.unaimillan.rars.ProgramStatement;
import com.github.unaimillan.rars.riscv.hardware.FloatingPointRegisterFile;
import com.github.unaimillan.rars.riscv.hardware.RegisterFile;
import com.github.unaimillan.rars.riscv.BasicInstruction;
import com.github.unaimillan.rars.riscv.BasicInstructionFormat;

public class FMVDX extends BasicInstruction {
    public FMVDX() {
        super("fmv.d.x f1, t1", "Move float: move bits representing a double from an 64 bit integer register",
                BasicInstructionFormat.I_FORMAT, "1111001 00000 sssss 000 fffff 1010011",true);
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        FloatingPointRegisterFile.updateRegisterLong(operands[0], RegisterFile.getValueLong(operands[1]));
    }
}