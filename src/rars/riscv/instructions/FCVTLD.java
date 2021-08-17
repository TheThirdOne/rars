package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.operations.Conversions;
import jsoftfloat.types.Float64;
import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.hardware.RegisterFile;

public class FCVTLD extends BasicInstruction {
    public FCVTLD() {
        super("fcvt.l.d t1, f1, dyn", "Convert 64 bit integer from double: Assigns the value of f1 (rounded) to t1",
                BasicInstructionFormat.I_FORMAT, "1100001 00010 sssss ttt fffff 1010011",true);
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        int hart = statement.getCurrentHart();
        Environment e = new Environment();
        e.mode = Floating.getRoundingMode(operands[2],statement);
        Float64 in = new Float64((hart == -1)
                    ? FloatingPointRegisterFile.getValueLong(operands[1])
                    : FloatingPointRegisterFile.getValueLong(operands[1], hart));
        long out = Conversions.convertToLong(in,e,false);
        Floating.setfflags(e, hart);
        if (hart == -1)
            RegisterFile.updateRegister(operands[0], out);
        else
            RegisterFile.updateRegister(operands[0], out, hart);
    }
}
