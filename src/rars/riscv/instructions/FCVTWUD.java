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

public class FCVTWUD extends BasicInstruction {
    public FCVTWUD() {
        super("fcvt.wu.d t1, f1, dyn", "Convert unsinged integer from double: Assigns the value of f1 (rounded) to t1",
                BasicInstructionFormat.I_FORMAT, "1100001 00001 sssss ttt fffff 1010011");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        int hart = statement.getCurrentHart();
        Environment e = new Environment();
        e.mode = Floating.getRoundingMode(operands[2],statement);
        Float64 in = new Float64((hart == -1)
                ? FloatingPointRegisterFile.getValueLong(operands[1])
                : FloatingPointRegisterFile.getValueLong(operands[1], hart));
        int out = Conversions.convertToUnsignedInt(in,e,false);
        Floating.setfflags(e, hart);
        if (hart == -1)
            RegisterFile.updateRegister(operands[0], out);
        else
            RegisterFile.updateRegister(operands[0], out, hart);
    }
}
