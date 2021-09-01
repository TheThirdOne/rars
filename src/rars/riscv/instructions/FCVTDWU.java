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

import java.math.BigInteger;

public class FCVTDWU extends BasicInstruction {
    public FCVTDWU() {
        super("fcvt.d.wu f1, t1, dyn", "Convert double from unsigned integer: Assigns the value of t1 to f1",
                BasicInstructionFormat.I_FORMAT, "1101001 00001 sssss ttt fffff 1010011");
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        int hart = statement.getCurrentHart();
        Environment e = new Environment();
        e.mode = Floating.getRoundingMode(operands[2],statement);
        Float64 tmp = new Float64(0);
        Float64 converted = Conversions
                .convertFromInt(BigInteger.valueOf(((hart == -1)
                                ? RegisterFile.getValue(operands[1])
                                : RegisterFile.getValue(operands[1]))
                        & 0xFFFFFFFFL), e, tmp);
        Floating.setfflags(e, hart);
        if (hart == -1)
            FloatingPointRegisterFile.updateRegisterLong(operands[0], converted.bits);
        else
            FloatingPointRegisterFile.updateRegisterLong(operands[0], converted.bits, hart);
    }
}
