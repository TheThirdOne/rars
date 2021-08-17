package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.types.Float32;
import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.hardware.RegisterFile;

public class FCVTLS extends BasicInstruction {
    public FCVTLS() {
        super("fcvt.l.s t1, f1, dyn", "Convert 64 bit integer from float: Assigns the value of f1 (rounded) to t1",
                BasicInstructionFormat.I_FORMAT, "1100000 00010 sssss ttt fffff 1010011",true);
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        int hart = statement.getCurrentHart();
        Environment e = new Environment();
        e.mode = Floating.getRoundingMode(operands[2],statement);
        Float32 in = new Float32((hart == -1)
                ? FloatingPointRegisterFile.getValue(operands[1])
                : FloatingPointRegisterFile.getValue(operands[1], hart));
        long out = jsoftfloat.operations.Conversions.convertToLong(in,e,false);
        Floating.setfflags(e, hart);
        if (hart == -1)
            RegisterFile.updateRegister(operands[0], out);
        else
            RegisterFile.updateRegister(operands[1], out, hart);
    }
}
