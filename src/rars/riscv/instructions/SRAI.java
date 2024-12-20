package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class SRAI extends BasicInstruction {
    public SRAI() {
        super("srai t1,t2,10", "Shift right arithmetic : Set t1 to result of sign-extended shifting t2 right by number of bits specified by immediate",
                BasicInstructionFormat.R_FORMAT, "0100000 ttttt sssss 101 fffff 0010011",false);
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        // Uses >> because sign fill
        RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]) >> operands[2]);
    }
}
