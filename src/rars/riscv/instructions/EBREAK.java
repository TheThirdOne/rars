package rars.riscv.instructions;

import rars.BreakpointException;
import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class EBREAK extends BasicInstruction {
    public EBREAK() {
        super("ebreak", "Pause execution",
                BasicInstructionFormat.I_FORMAT, "000000000001 00000 000 00000 1110011");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        throw new BreakpointException();
    }
}