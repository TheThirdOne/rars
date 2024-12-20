package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class FMVXS extends BasicInstruction {
    public FMVXS() {
        super("fmv.x.s t1, f1", "Move float: move bits representing a float to an integer register",
                BasicInstructionFormat.I_FORMAT, "1110000 00000 sssss 000 fffff 1010011");
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        RegisterFile.updateRegister(operands[0], (int)FloatingPointRegisterFile.getValueLong(operands[1]));
    }
}