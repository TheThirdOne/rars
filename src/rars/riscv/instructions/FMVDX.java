package rars.riscv.instructions;

import rars.ProgramStatement;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class FMVDX extends BasicInstruction {
    public FMVDX() {
        super("fmv.d.x f1, t1", "Move float: move bits representing a double from an 64 bit integer register",
                BasicInstructionFormat.I_FORMAT, "1111001 00000 sssss 000 fffff 1010011",true);
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        int hart = statement.getCurrentHart();
        if (hart == -1)
            FloatingPointRegisterFile.updateRegisterLong(operands[0],
                    RegisterFile.getValueLong(operands[1]));
        else
            FloatingPointRegisterFile.updateRegisterLong(operands[0],
                    RegisterFile.getValueLong(operands[1], hart), hart);
    }
}
