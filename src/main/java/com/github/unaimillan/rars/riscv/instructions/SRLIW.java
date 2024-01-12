package com.github.unaimillan.rars.riscv.instructions;

import com.github.unaimillan.rars.ProgramStatement;
import com.github.unaimillan.rars.riscv.hardware.RegisterFile;
import com.github.unaimillan.rars.riscv.BasicInstruction;
import com.github.unaimillan.rars.riscv.BasicInstructionFormat;

public class SRLIW extends BasicInstruction {
    public SRLIW() {
        super("srliw t1,t2,10", "Shift right logical (32 bit): Set t1 to result of shifting t2 right by number of bits specified by immediate",
                BasicInstructionFormat.R_FORMAT, "0000000 ttttt sssss 101 fffff 0011011",true);
    }

    public void simulate(ProgramStatement statement) {
        // Use the code directly from SRLI
        int[] operands = statement.getOperands();
        RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]) >>> operands[2]);
    }
}
