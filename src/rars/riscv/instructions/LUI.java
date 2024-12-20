package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class LUI extends BasicInstruction {
    public LUI() {
        super("lui t1,100000", "Load upper immediate: set t1 to 20-bit followed by 12 0s",
                BasicInstructionFormat.U_FORMAT, "ssssssssssssssssssss fffff 0110111");
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        RegisterFile.updateRegister(operands[0], operands[1] << 12);
    }
}
