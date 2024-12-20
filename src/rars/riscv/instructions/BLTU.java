package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.hardware.RegisterFile;

public class BLTU extends Branch {
    public BLTU() {
        super("bltu t1,t2,label", "Branch if less than (unsigned): Branch to statement at label's address if t1 is less than t2 (with an unsigned interpretation)", "110");
    }

    public boolean willBranch(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        return Long.compareUnsigned(RegisterFile.getValueLong(operands[0]), RegisterFile.getValueLong(operands[1])) < 0;
    }
}