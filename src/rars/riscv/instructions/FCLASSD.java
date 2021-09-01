package rars.riscv.instructions;

import jsoftfloat.types.Float64;
import rars.ProgramStatement;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;
import rars.riscv.hardware.FloatingPointRegisterFile;

public class FCLASSD extends BasicInstruction {
    public FCLASSD() {
        super("fclass.d t1, f1", "Classify a floating point number (64 bit)",
                BasicInstructionFormat.I_FORMAT, "1110001 00000 sssss 001 fffff 1010011");
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        int hart = statement.getCurrentHart();
        Float64 in = (hart == -1)
                ? new Float64(FloatingPointRegisterFile.getValueLong(operands[1]))
                : new Float64(FloatingPointRegisterFile.getValueLong(operands[1], hart));
        if (hart == -1)
            FCLASSS.fclass(in, operands[0]);
        else
            FCLASSS.fclass(in, operands[0], hart);
    }
}
