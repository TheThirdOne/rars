package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.types.Float32;
import rars.ProgramStatement;
import rars.riscv.hardware.ControlAndStatusRegisterFile;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.hardware.RegisterFile;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;

public class FLES extends BasicInstruction {
    public FLES() {
        super("fle.s t1, f1, f2", "Floating Less than or Equals: if f1 <= f2, set t1 to 1, else set t1 to 0",
                BasicInstructionFormat.R_FORMAT, "1010000 ttttt sssss 000 fffff 1010011");
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        Float32 f1 = Floating.getFloat(operands[1]), f2 = Floating.getFloat(operands[2]);
        Environment e = new Environment();
        boolean result = jsoftfloat.operations.Comparisons.compareSignalingLessThanEqual(f1,f2,e);
        Floating.setfflags(e);
        RegisterFile.updateRegister(operands[0], result ? 1 : 0);
    }
}