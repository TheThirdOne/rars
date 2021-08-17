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

public class FCVTDLU extends BasicInstruction {
    public FCVTDLU() {
        super("fcvt.d.lu f1, t1, dyn", "Convert double from unsigned long: Assigns the value of t1 to f1",
                BasicInstructionFormat.I_FORMAT, "1101001 00011 sssss ttt fffff 1010011",true);
    }

    public void simulate(ProgramStatement statement) throws SimulationException {
        int[] operands = statement.getOperands();
        int hart = statement.getCurrentHart();
        Environment e = new Environment();
        e.mode = Floating.getRoundingMode(operands[2],statement);
        Float64 tmp = new Float64(0);
        long value = (hart == -1)
                ? RegisterFile.getValueLong(operands[1])
                : RegisterFile.getValueLong(operands[1], hart);
        BigInteger unsigned = BigInteger.valueOf(value);
        if (value < 0) {
            unsigned = unsigned.add(BigInteger.ONE.shiftLeft(64));
        }
        Float64 converted = Conversions.convertFromInt(unsigned,e,tmp);
        Floating.setfflags(e, hart);
        if (hart == -1)
            FloatingPointRegisterFile.updateRegisterLong(operands[0], converted.bits);
        else
            FloatingPointRegisterFile.updateRegisterLong(operands[0], converted.bits, hart);
    }
}
