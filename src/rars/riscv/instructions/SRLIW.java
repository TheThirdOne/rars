package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class SRLIW extends BasicInstruction {
    public SRLIW() {
        super("srliw t1,t2,10", "Shift right logical : Set t1 to result of shifting t2 right by number of bits specified by immediate",
                BasicInstructionFormat.R_FORMAT, "0000000 ttttt sssss 101 fffff 0011011",true);
    }

    public void simulate(ProgramStatement statement) {
        // Use the code directly from SRLI
        int[] operands = statement.getOperands();
        RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]) >>> operands[2]);
    }
}
