package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

// TODO: description
public class SLLIW extends BasicInstruction {
    public SLLIW() {
        super("slliw t1,t2,10", "Shift left logical : Set t1 to result of shifting t2 left by number of bits specified by immediate",
                BasicInstructionFormat.R_FORMAT, "0000000 ttttt sssss 001 fffff 0011011",true);
    }

    public void simulate(ProgramStatement statement) {
        // Copy from SLLI
        int[] operands = statement.getOperands();
        RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]) << operands[2]);
    }
}
