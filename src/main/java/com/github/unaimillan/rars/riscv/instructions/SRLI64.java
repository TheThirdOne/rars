package com.github.unaimillan.rars.riscv.instructions;

import com.github.unaimillan.rars.ProgramStatement;
import com.github.unaimillan.rars.riscv.hardware.RegisterFile;
import com.github.unaimillan.rars.riscv.BasicInstruction;
import com.github.unaimillan.rars.riscv.BasicInstructionFormat;

public class SRLI64 extends BasicInstruction {
    public SRLI64(){
        super("srli t1,t2,33", "Shift right logical : Set t1 to result of shifting t2 right by number of bits specified by immediate",
                BasicInstructionFormat.R_FORMAT, "000000 tttttt sssss 101 fffff 0010011",true);
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        RegisterFile.updateRegister(operands[0], RegisterFile.getValueLong(operands[1]) >>> operands[2]);
    }
}
