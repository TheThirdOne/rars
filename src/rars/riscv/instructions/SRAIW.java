package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.InstructionSet;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

// TODO: description
public class SRAIW extends BasicInstruction {
    public SRAIW() {
        super("sraiw t1,t2,10", "Shift right arithmetic : Set t1 to result of sign-extended shifting t2 right by number of bits specified by immediate",
                BasicInstructionFormat.R_FORMAT, "0100000 ttttt sssss 101 fffff 0011011",true);
    }

    public void simulate(ProgramStatement statement) {
        // Use the code directly from SRAI
        int[] operands = statement.getOperands();
        RegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]) >> operands[2]);
    }
}
