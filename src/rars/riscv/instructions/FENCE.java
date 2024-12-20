package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class FENCE extends BasicInstruction {
    public FENCE() {
        super("fence 1, 1", "Ensure that IO and memory accesses before the fence happen before the following IO and memory accesses as viewed by a different thread",
                BasicInstructionFormat.I_FORMAT, "0000 ffff ssss 00000 000 00000 0001111");
    }

    public void simulate(ProgramStatement statement) {
        // Do nothing, currently there are no other threads so local consitency is enough
    }
}