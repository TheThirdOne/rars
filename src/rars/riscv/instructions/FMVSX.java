package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class FMVSX extends BasicInstruction {
    public FMVSX() {
        super("fmv.s.x f1, t1", "Move float: move bits representing a float from an integer register",
                BasicInstructionFormat.I_FORMAT, "1111000 00000 sssss 000 fffff 1010011");
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        FloatingPointRegisterFile.updateRegister(operands[0], RegisterFile.getValue(operands[1]));
    }
}