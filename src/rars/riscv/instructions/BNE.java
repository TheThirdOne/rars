package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.hardware.RegisterFile;

public class BNE extends Branch {
    public BNE() {
        super("bne t1,t2,label", "Branch if not equal : Branch to statement at label's address if t1 and t2 are not equal", "001");
    }

    public boolean willBranch(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        return RegisterFile.getValueLong(operands[0]) != RegisterFile.getValueLong(operands[1]);
    }
}