package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.hardware.RegisterFile;

public class BGEU extends Branch {
    public BGEU() {
        super("bgeu t1,t2,label", "Branch if greater than or equal to (unsigned): Branch to statement at label's address if t1 is greater than or equal to t2 (with an unsigned interpretation)", "111");
    }

    public boolean willBranch(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        return Long.compareUnsigned(RegisterFile.getValueLong(operands[0]), RegisterFile.getValueLong(operands[1])) >= 0;
    }
}