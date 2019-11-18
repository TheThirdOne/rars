package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.types.Float64;
import rars.ProgramStatement;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;
import rars.riscv.hardware.RegisterFile;

public class FEQD extends BasicInstruction {
    public FEQD() {
        super("feq.d t1, f1, f2", "Floating EQuals (64 bit): if f1 = f2, set t1 to 1, else set t1 to 0",
                BasicInstructionFormat.R_FORMAT, "1010001 ttttt sssss 010 fffff 1010011");
    }

    public void simulate(ProgramStatement statement) {
        int[] operands = statement.getOperands();
        Float64 f1 = Double.getDouble(operands[1]), f2 = Double.getDouble(operands[2]);
        Environment e = new Environment();
        boolean result = jsoftfloat.operations.Comparisons.compareQuietEqual(f1,f2,e);
        Floating.setfflags(e);
        RegisterFile.updateRegister(operands[0], result ? 1 : 0);
    }
}