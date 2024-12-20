package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.hardware.RegisterFile;

public class BLT extends Branch {
    public BLT() {
        super("blt t1,t2,label", "Branch if less than: Branch to statement at label's address if t1 is less than t2", "100");
    }

    public boolean willBranch(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        return RegisterFile.getValueLong(operands[0]) < RegisterFile.getValueLong(operands[1]);
    }
}