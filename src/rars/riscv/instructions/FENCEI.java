package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class FENCEI extends BasicInstruction {
    public FENCEI() {
        super("fence.i", "Ensure that stores to instruction memory are visible to instruction fetches",
                BasicInstructionFormat.I_FORMAT, "0000 0000 0000 00000 001 00000 0001111");
    }

    public void simulate(ProgramStatement statement) {
        // Do nothing, currently all stores are immediately available to instruction fetches
    }
}