package rars.riscv.instructions;

import jsoftfloat.Environment;
import jsoftfloat.types.Float64;
import rars.ProgramStatement;
import rars.SimulationException;
import rars.riscv.BasicInstruction;
import rars.riscv.BasicInstructionFormat;
import rars.riscv.hardware.FloatingPointRegisterFile;
import rars.riscv.hardware.RegisterFile;

import java.math.BigInteger;

public class FCVTDL extends BasicInstruction {
    public FCVTDL() {
        super("fcvt.d.l f1, t1, dyn", "Convert double from long: Assigns the value of t1 to f1",
                BasicInstructionFormat.I_FORMAT, "1101001 00010 sssss ttt fffff 1010011",true);
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        Environment e = new Environment();
        e.mode = Floating.getRoundingMode(operands[2],statement);
        Float64 tmp = new Float64(0);
        Float64 converted = jsoftfloat.operations.Conversions.convertFromInt(BigInteger.valueOf(RegisterFile.getValueLong(operands[1])),e,tmp);
        Floating.setfflags(e);
        FloatingPointRegisterFile.updateRegisterLong(operands[0],converted.bits);
    }
}

