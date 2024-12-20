package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.WaitException;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class WFI extends BasicInstruction {
    public WFI() {
        super("wfi", "Wait for Interrupt",
                BasicInstructionFormat.I_FORMAT, "000100000101 00000 000 00000 1110011");
    }

    public void simulate(ProgramStatement statement) throws WaitException {
        throw new WaitException();
    }
}