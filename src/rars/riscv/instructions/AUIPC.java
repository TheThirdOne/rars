package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class AUIPC extends BasicInstruction {
    public AUIPC() {
        super("auipc t1,100000", "Add upper immediate to pc: set t1 to (pc plus an upper 20-bit immediate)",
                BasicInstructionFormat.U_FORMAT, "ssssssssssssssssssss fffff 0010111");
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        RegisterFile.updateRegister(operands[0], RegisterFile.getProgramCounter() - INSTRUCTION_LENGTH + (operands[1] << 12));
    }
}
