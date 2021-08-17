package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.operations.Conversions;
import jsoftfloat.types.Float32;
import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.hardware.RegisterFile;

import java.math.BigInteger;

public class FCVTSL extends BasicInstruction {
    public FCVTSL() {
        super("fcvt.s.l f1, t1, dyn", "Convert float from long: Assigns the value of t1 to f1",
                BasicInstructionFormat.I_FORMAT, "1101000 00010 sssss ttt fffff 1010011",true);
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        int hart = statement.getCurrentHart();
        Environment e = new Environment();
        e.mode = Floating.getRoundingMode(operands[2],statement);
        Float32 tmp = new Float32(0);
        Float32 converted = Conversions.convertFromInt(BigInteger.valueOf((hart == -1)
                ? RegisterFile.getValueLong(operands[1])
                : RegisterFile.getValueLong(operands[1], hart)
                ), e, tmp);
                Floating.setfflags(e, hart);
        if (hart == -1)
            FloatingPointRegisterFile.updateRegister(operands[0], converted.bits);
        else
            FloatingPointRegisterFile.updateRegister(operands[0], converted.bits, hart);
    }
}
