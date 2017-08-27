package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class NOP extends BasicInstruction {
    public NOP() {
        super("nop", "Null operation : machine code is all zeroes",
                BasicInstructionFormat.R_FORMAT, "000000 00000 00000 00000 00000 000000");
    }

    public void simulate(ProgramStatement statement) {
        // Hey I like this so far!
    }
}
