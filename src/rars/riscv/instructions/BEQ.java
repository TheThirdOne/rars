package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.hardware.RegisterFile;

public class BEQ extends Branch {
    public BEQ() {
        super("beq t1,t2,label", "Branch if equal : Branch to statement at label's address if t1 and t2 are equal", "000");
    }

    public boolean willBranch(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        return RegisterFile.getValueLong(operands[0]) == RegisterFile.getValueLong(operands[1]);
    }
}